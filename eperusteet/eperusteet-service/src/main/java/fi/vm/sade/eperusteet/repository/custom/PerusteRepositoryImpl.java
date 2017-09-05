/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.repository.custom;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa_;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite_;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.repository.PerusteRepositoryCustom;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 *
 * @author jhyoty
 */
public class PerusteRepositoryImpl implements PerusteRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Peruste> findBy(PageRequest page, PerusteQuery pquery) {
        return findBy(page, pquery, new HashSet<>());
    }

    /**
     * Etsi Peruste määritellyillä hakuehdoilla (sivutettu kysely)
     *
     * @param page sivumääritys
     * @param pquery hakuparametrit
     * @param koodistostaHaetut
     *
     * @return Yhden hakusivun verran vastauksia
     */
    @Override
    public Page<Peruste> findBy(PageRequest page, PerusteQuery pquery, Set<Long> koodistostaHaetut) {
        TypedQuery<Long> countQuery = getCountQuery(pquery, koodistostaHaetut);
        TypedQuery<Tuple> query = getQuery(pquery, koodistostaHaetut);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }
        return new PageImpl<>(Lists.transform(query.getResultList(), EXTRACT_PERUSTE), page, countQuery.getSingleResult());
    }

    private TypedQuery<Tuple> getQuery(PerusteQuery pquery, Set<Long> koodistostaHaetut) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery, koodistostaHaetut);
        query.distinct(true);
        final Expression<String> n = cb.lower(teksti.get(LokalisoituTeksti_.teksti));

        final List<Order> order = new ArrayList<>();
        if ("muokattu".equals(pquery.getJarjestys())) {
            order.add(cb.desc(root.get(Peruste_.muokattu)));
        }
        order.add(cb.asc(n));
        order.add(cb.asc(root.get(Peruste_.id)));
        query.multiselect(root, n).where(pred).orderBy(order);

        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(PerusteQuery pquery, Set<Long> koodistostaHaetut) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery, koodistostaHaetut);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(
        Root<Peruste> root,
        Join<TekstiPalanen, LokalisoituTeksti> teksti,
        CriteriaBuilder cb,
        PerusteQuery pq,
        Set<Long> koodistostaHaetut
    ) {
        final Expression<Date> voimassaoloAlkaa = root.get(Peruste_.voimassaoloAlkaa);
        final Expression<Date> voimassaoloLoppuu = root.get(Peruste_.voimassaoloLoppuu);
        final Expression<Date> siirtymaPaattyy = root.get(Peruste_.siirtymaPaattyy);
        final Kieli kieli = Kieli.of(pq.getKieli());

        Expression<java.sql.Date> currentDate = cb.literal(new java.sql.Date(pq.getNykyinenAika()));

        Predicate pred = cb.equal(teksti.get(LokalisoituTeksti_.kieli), kieli);

        if (pq.getNimi() != null) {
            Expression<String> nimiLit = cb.literal(RepositoryUtil.kuten(pq.getNimi()));
            Predicate nimessa = cb.like(cb.lower(teksti.get(LokalisoituTeksti_.teksti)), nimiLit);
            List<Predicate> preds = new ArrayList<>();
            preds.add(nimessa);

            if (koodistostaHaetut.size() > 0) {
                Predicate haettuKoodistosta = root.get(Peruste_.id).in(koodistostaHaetut);
                preds.add(haettuKoodistosta);
            }

            if (pq.isTutkinnonosat()) {
                Join<TutkinnonOsaViite, TutkinnonOsa> tutkinnonOsa = root
                        .join(Peruste_.suoritustavat)
                        .join(Suoritustapa_.tutkinnonOsat)
                        .join(TutkinnonOsaViite_.tutkinnonOsa);
                Join<TekstiPalanen, LokalisoituTeksti> tutkinnonOsanNimi = tutkinnonOsa
                        .join(TutkinnonOsa_.nimi)
                        .join(TekstiPalanen_.teksti);
                Predicate tutkinnonOsaJulkaistu = cb.equal(tutkinnonOsa.get(TutkinnonOsa_.tila), PerusteTila.VALMIS);
                Predicate tosanKoodiArvossa = cb.like(tutkinnonOsa.get(TutkinnonOsa_.koodiArvo), nimiLit);
                Predicate tosanNimessa = cb.like(cb.lower(tutkinnonOsanNimi.get(LokalisoituTeksti_.teksti)), nimiLit);
                preds.add(cb.and(tutkinnonOsaJulkaistu, cb.or(tosanKoodiArvossa, tosanNimessa)));
            }

            if (preds.size() < 2) {
                pred = cb.and(pred, preds.get(0));
            } else {
                pred = cb.and(pred, cb.or(preds.toArray(new Predicate[preds.size()])));
            }
        }

        if (pq.isKoulutusvienti()) {
            pred = cb.and(pred, cb.isTrue(root.get(Peruste_.koulutusvienti)));
        }
        else {
            pred = cb.and(pred, cb.isFalse(root.get(Peruste_.koulutusvienti)));
        }

        if (pq.getDiaarinumero() != null) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.diaarinumero), cb.literal(new Diaarinumero(pq.getDiaarinumero()))));
        }

        if (pq.getSuoritustapa() != null) {
            Suoritustapakoodi suoritustapakoodi = Suoritustapakoodi.of(pq.getSuoritustapa());
            Join<Peruste, Suoritustapa> suoritustapa = root.join(Peruste_.suoritustavat);
            pred = cb.and(pred, cb.equal(suoritustapa.get(Suoritustapa_.suoritustapakoodi), suoritustapakoodi));
        }
        if (!empty(pq.getKoulutustyyppi())) {
            pred = cb.and(pred, root.get(Peruste_.koulutustyyppi).in(pq.getKoulutustyyppi()));
        }

        Join<Peruste, Koulutus> koulutukset = null;
        if (!empty(pq.getKoulutusala())) {
            koulutukset = root.join(Peruste_.koulutukset);
            pred = cb.and(pred, koulutukset.get(Koulutus_.koulutusalakoodi).in(pq.getKoulutusala()));
        }

        if (!empty(pq.getOpintoala())) {
            koulutukset = (koulutukset == null) ? root.join(Peruste_.koulutukset) : koulutukset;
            pred = cb.and(pred, koulutukset.get(Koulutus_.opintoalakoodi).in(pq.getOpintoala()));
        }

        if (!Strings.isNullOrEmpty(pq.getKoulutuskoodi())) {
            koulutukset = (koulutukset == null) ? root.join(Peruste_.koulutukset) : koulutukset;
            pred = cb.and(pred, cb.or(cb.equal(koulutukset.get(Koulutus_.koulutuskoodiUri), pq.getKoulutuskoodi()),
                    cb.equal(koulutukset.get(Koulutus_.koulutuskoodiArvo), pq.getKoulutuskoodi())));
        }

        Predicate tilat = cb.disjunction();

        if (pq.isTuleva()) {
            tilat = cb.or(tilat, cb.and(cb.isNotNull(voimassaoloAlkaa), cb.lessThan(currentDate, voimassaoloAlkaa)));
        }

        if (pq.isKoulutusvienti()) {
            tilat = cb.or(tilat, cb.and(cb.isNotNull(voimassaoloAlkaa), cb.lessThan(currentDate, voimassaoloAlkaa)));
        }

        if (pq.isVoimassaolo()) {
            Predicate alkaa = cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa));
            Predicate loppuu = cb.and(cb.isNotNull(voimassaoloLoppuu), cb.lessThanOrEqualTo(currentDate, voimassaoloLoppuu));
            tilat = cb.or(tilat, cb.and(alkaa, loppuu));

            // Voimassaolon loppumista ei ole määritelty
            tilat = cb.or(tilat, cb.and(cb.isNull(voimassaoloLoppuu),
                    cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa))));

            // Voimassaolon alkamista ei ole määritelty
            tilat = cb.or(tilat, cb.and(cb.isNull(voimassaoloAlkaa),
                    cb.and(cb.isNotNull(voimassaoloLoppuu), cb.lessThanOrEqualTo(currentDate, voimassaoloLoppuu))));

            // Voimassaolon alkamista tai loppumista ei ole määritelty
            tilat = cb.or(tilat, cb.and(cb.isNull(voimassaoloAlkaa), cb.isNull(voimassaoloLoppuu)));
        }

        if (pq.isSiirtyma()) {
            Predicate alkaa = cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu));
            Predicate loppuu = cb.and(cb.isNotNull(siirtymaPaattyy), cb.lessThanOrEqualTo(currentDate, siirtymaPaattyy));
            tilat = cb.or(tilat, cb.and(alkaa, loppuu));
        }

        if (pq.isPoistunut()) {
            tilat = cb.or(tilat, cb.and(cb.isNotNull(siirtymaPaattyy), cb.greaterThan(currentDate, siirtymaPaattyy)));

            // Siirtymän loppumista ei ole määritelty
            tilat = cb.or(tilat, cb.and(cb.isNull(siirtymaPaattyy),
                    cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu))));
        }

        pred = cb.and(pred, tilat);

        if (!empty(pq.getTila())) {
            Set<PerusteTila> perusteTilat = pq.getTila().stream()
                    .map(PerusteTila::of)
                    .collect(Collectors.toSet());
            pred = cb.and(pred, root.get(Peruste_.tila).in(perusteTilat));
        }

        if (!Strings.isNullOrEmpty(pq.getPerusteTyyppi())) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.tyyppi), PerusteTyyppi.of(pq.getPerusteTyyppi())));
        }

        if (pq.getMuokattu() != null) {
            pred = cb.and(pred, cb.greaterThan(root.get(Peruste_.muokattu), cb.literal(new Date(pq.getMuokattu()))));
        }

        if (pq.getKieli() != null) {
            //Hibernate bug (?), isMember ei toimi (bindaus ei mene enumina)
            SetJoin<Peruste, Kieli> kielet = root.join(Peruste_.kielet);
            pred = cb.and(pred, cb.equal(kielet, kieli));
        }

        // Tutkinnon osien tuontia varten
        if (pq.getEsikatseltavissa() != null) {
            Join<Peruste, Perusteprojekti> perusteprojekti = root.join(Peruste_.perusteprojekti);

            // Jos peruste on esikatseltavissa tai/ja julkaistu
            Predicate esikatseltavissaTaiJulkaistu= cb.disjunction();
            esikatseltavissaTaiJulkaistu = cb.or(esikatseltavissaTaiJulkaistu,
                    cb.isTrue(perusteprojekti.get(Perusteprojekti_.esikatseltavissa)));
            esikatseltavissaTaiJulkaistu = cb.or(esikatseltavissaTaiJulkaistu,
                    cb.isTrue(cb.equal(root.get(Peruste_.tila), PerusteTila.VALMIS)));

            pred = cb.and(pred, esikatseltavissaTaiJulkaistu);
        }

        return pred;
    }

    private static boolean empty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static final Function<Tuple, Peruste> EXTRACT_PERUSTE = f -> f.get(0, Peruste.class);

}
