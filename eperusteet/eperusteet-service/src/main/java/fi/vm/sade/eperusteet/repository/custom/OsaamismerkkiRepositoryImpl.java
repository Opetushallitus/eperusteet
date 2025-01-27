package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti_;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TekstiPalanen_;
import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiTila;
import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki_;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class OsaamismerkkiRepositoryImpl implements OsaamismerkkiRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Osaamismerkki> findBy(PageRequest page, OsaamismerkkiQuery oquery) {
        TypedQuery<Long> countQuery = getCountQuery(oquery);
        TypedQuery<Tuple> query = getQuery(oquery);
        query.setFirstResult(Long.valueOf(page.getOffset()).intValue());
        query.setMaxResults(page.getPageSize());

        List<Osaamismerkki> result = query.getResultList().stream()
                .map(t -> t.get(0, Osaamismerkki.class))
                .collect(Collectors.toList());

        return new PageImpl<>(result, page, countQuery.getSingleResult());
    }

    private TypedQuery<Long> getCountQuery(OsaamismerkkiQuery oquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Osaamismerkki> root = query.from(Osaamismerkki.class);

        Predicate pred = buildPredicate(root, cb, oquery);
        query.select(cb.countDistinct(root)).where(pred);

        return em.createQuery(query);
    }

    private TypedQuery<Tuple> getQuery(OsaamismerkkiQuery oquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Osaamismerkki> root = query.from(Osaamismerkki.class);
        Predicate pred = buildPredicate(root, cb, oquery);

        // sortataan vain nimen perusteella
        Join<TekstiPalanen, LokalisoituTeksti> nimi = root.join(Osaamismerkki_.nimi).join(TekstiPalanen_.teksti);
        Expression<String> n = cb.lower(nimi
                .on(cb.equal(nimi.get(LokalisoituTeksti_.kieli), Kieli.of(oquery.getKieli())))
                .get(LokalisoituTeksti_.teksti));
        query = query.multiselect(root, n);

        query.where(pred)
                .orderBy(cb.asc(n))
                .distinct(true);

        return em.createQuery(query);
    }

    private Predicate buildPredicate(Root<Osaamismerkki> root,
                                     CriteriaBuilder cb,
                                     OsaamismerkkiQuery tq) {
        Predicate pred = cb.conjunction();

        if (!ObjectUtils.isEmpty(tq.getNimi())) {
            Expression<String> nimiLit = cb.literal(RepositoryUtil.kuten(tq.getNimi()));
            Join<TekstiPalanen, LokalisoituTeksti> nimi = root.join(Osaamismerkki_.nimi).join(TekstiPalanen_.teksti);
            Predicate nimessa = cb.like(cb.lower(nimi.get(LokalisoituTeksti_.teksti)), nimiLit);
            pred = cb.and(pred, nimessa);
        }

        if (!ObjectUtils.isEmpty(tq.getTila())) {
            Set<OsaamismerkkiTila> osaamismerkkiTilat = tq.getTila().stream()
                    .map(OsaamismerkkiTila::of)
                    .collect(Collectors.toSet());
            pred = cb.and(pred, root.get(Osaamismerkki_.tila).in(osaamismerkkiTilat));
        }

        if (!ObjectUtils.isEmpty(tq.getKoodit())) {
            Set<String> koodit = tq.getKoodit().stream()
                    .map(koodi -> KoodistoUriArvo.OSAAMISMERKIT + "_" + koodi)
                    .collect(Collectors.toSet());
            pred = cb.and(pred, root.get(Osaamismerkki_.koodiUri).in(koodit));
        }

        if (!ObjectUtils.isEmpty(tq.getKategoria())) {
            pred = cb.and(pred, cb.equal(root.get(Osaamismerkki_.kategoria), tq.getKategoria()));
        }

        final Expression<Date> voimassaoloAlkaa = root.get(Osaamismerkki_.voimassaoloAlkaa);
        final Expression<Date> voimassaoloLoppuu = root.get(Osaamismerkki_.voimassaoloLoppuu);
        Expression<java.sql.Date> currentDate = cb.literal(new java.sql.Date(new Date().getTime()));

        if (tq.isTuleva()) {
            pred = cb.and(pred, cb.isNotNull(voimassaoloAlkaa), cb.lessThan(currentDate, voimassaoloAlkaa));
        } else if (tq.isVoimassa()) {
            Predicate alkaa = cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa));
            Predicate loppuu = cb.and(cb.isNotNull(voimassaoloLoppuu), cb.lessThanOrEqualTo(currentDate, voimassaoloLoppuu));
            Predicate pr1 = cb.and(alkaa, loppuu);

            // Voimassaolon loppumista ei ole määritelty
            Predicate pr2 = cb.and(cb.isNull(voimassaoloLoppuu), cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa)));

            if (tq.isPoistunut()) {
                // myös poistuneet
                Predicate pr3 = cb.and(pred, cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu)));
                pred = cb.and(pred, cb.or(pr1, pr2, pr3));
            }
            else {
                pred = cb.and(pred, cb.or(pr1, pr2));
            }
        } else if (tq.isPoistunut()) {
            pred = cb.and(pred, cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu)));
        }
        return pred;
    }
}
