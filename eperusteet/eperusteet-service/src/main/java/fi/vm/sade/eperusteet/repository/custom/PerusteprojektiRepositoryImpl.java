package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepositoryCustom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
public class PerusteprojektiRepositoryImpl implements PerusteprojektiRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Perusteprojekti> findBy(PageRequest page, PerusteprojektiQueryDto pquery) {
        TypedQuery<Long> countQuery = getCountQuery(pquery);
        TypedQuery<Tuple> query = getQuery(pquery);
        if (page != null) {
            query.setFirstResult(Long.valueOf(page.getOffset()).intValue());
            query.setMaxResults(page.getPageSize());
        }

        // SQL query
        // log.debug(query.unwrap(Query.class).getQueryString());

        List<Perusteprojekti> result = query.getResultList().stream()
                .map(t -> t.get(0, Perusteprojekti.class))
                .collect(Collectors.toList());

        return new PageImpl<>(result, page, countQuery.getSingleResult());
    }

    private TypedQuery<Long> getCountQuery(PerusteprojektiQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        Predicate pred = buildPredicate(root, cb, pquery);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private TypedQuery<Tuple> getQuery(PerusteprojektiQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        Predicate pred = buildPredicate(root, cb, pquery);
        query.distinct(true);

        final List<Order> order = new ArrayList<>();
        final Expression<String> nimi = cb.lower(root.get(Perusteprojekti_.nimi));
        final Path<Date> perusteVersion = root.join(Perusteprojekti_.peruste)
                .join(Peruste_.globalVersion)
                .get(PerusteVersion_.aikaleima);
        final Path<Date> voimassaoloAlkaa = root.join(Perusteprojekti_.peruste).get(Peruste_.voimassaoloAlkaa);
        final Path<Date> voimassaoloLoppuu = root.join(Perusteprojekti_.peruste).get(Peruste_.voimassaoloLoppuu);
        final Path<String> koulutustyyppi = root.join(Perusteprojekti_.peruste).get(Peruste_.koulutustyyppi);
        final Path<Date> paatospvm = root.join(Perusteprojekti_.peruste).get(Peruste_.paatospvm);

        if (!StringUtils.isEmpty(pquery.getJarjestysTapa())) {
            Boolean jarjestysOrder = pquery.getJarjestysOrder();
            switch (JarjestysTapa.of(pquery.getJarjestysTapa())) {
                case NIMI:
                    addOrderExpression(cb, order, nimi, jarjestysOrder);
                    break;
                case TILA:
                    addOrderExpression(cb, order, root.get(Perusteprojekti_.tila), jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case LUOTU:
                    addOrderExpression(cb, order, root.get(Perusteprojekti_.luotu), jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case MUOKATTU:
                    addOrderExpression(cb, order, perusteVersion, jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case PERUSTE_VOIMASSAOLO_ALKAA:
                    addOrderExpression(cb, order, voimassaoloAlkaa, jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case PERUSTE_VOIMASSAOLO_LOPPUU:
                    addOrderExpression(cb, order, voimassaoloLoppuu, jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case PERUSTE_PAATOSPVM:
                    addOrderExpression(cb, order, paatospvm, jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                case KOULUTUSTYYPPI:
                    addOrderExpression(cb, order, koulutustyyppi, jarjestysOrder);
                    addOrderExpression(cb, order, nimi, false);
                    break;
                default:
                    addOrderExpression(cb, order, nimi, false);
                    break;
            }
        } else {
            order.add(cb.asc(nimi));
        }

        order.add(cb.asc(root.get(Perusteprojekti_.id)));
        query.multiselect(root, nimi, perusteVersion, voimassaoloAlkaa, voimassaoloLoppuu, koulutustyyppi, paatospvm).where(pred).orderBy(order);
        return em.createQuery(query);
    }

    private void addOrderExpression(CriteriaBuilder cb, List<Order> order, Expression<?> ex, Boolean jarjestysOrder) {
        if (jarjestysOrder == null || !jarjestysOrder) {
            order.add(cb.asc(ex));
        } else {
            order.add(cb.desc(ex));
        }
    }

    private Predicate buildPredicate(
            Root<Perusteprojekti> root,
            CriteriaBuilder cb,
            PerusteprojektiQueryDto pq
    ) {
        Expression<String> targetName = cb.lower(root.get(Perusteprojekti_.nimi));
        Expression<Diaarinumero> targetDiaari = root.get(Perusteprojekti_.diaarinumero);
        Join<Perusteprojekti, Peruste> joined = root.join(Perusteprojekti_.peruste);
        Path<PerusteTyyppi> tyyppi = joined.get(Peruste_.tyyppi);

        Expression<String> haku = cb.literal(RepositoryUtil.kuten(pq.getNimi()));
        Expression<Diaarinumero> diaarihaku = cb.literal(new Diaarinumero(pq.getNimi()));

        Predicate nimessa = cb.like(targetName, haku);
        Predicate diaarissa = cb.equal(targetDiaari, diaarihaku);
        Predicate result = cb.or(nimessa, diaarissa);

        if (CollectionUtils.isEmpty(pq.getTyyppi())) {
            result = cb.and(result, cb.notEqual(tyyppi, PerusteTyyppi.OPAS));
        } else {
            result = cb.and(result, tyyppi.in(pq.getTyyppi()));
        }

        if (!ObjectUtils.isEmpty(pq.getKoulutustyyppi())) {
            if (pq.getTyyppi().contains(PerusteTyyppi.OPAS)) {
                SetJoin<Peruste, KoulutusTyyppi> koulutustyypit = joined.join(Peruste_.oppaanKoulutustyypit, JoinType.LEFT);

                result = cb.and(result, cb.or(
                        joined.get(Peruste_.koulutustyyppi).in(pq.getKoulutustyyppi()),
                        pq.getKoulutustyyppi().stream()
                                .map((koulutustyyppi) -> cb.equal(koulutustyypit, KoulutusTyyppi.of(koulutustyyppi)))
                                .reduce(cb::or)
                                .get()));

            } else {
                Join<Perusteprojekti, Peruste> peruste = root.join(Perusteprojekti_.peruste);
                result = cb.and(result, peruste.get(Peruste_.koulutustyyppi).in(pq.getKoulutustyyppi()));
            }
        }

        if (pq.getTyyppi().contains(PerusteTyyppi.OPAS) && !CollectionUtils.isEmpty(pq.getPerusteet())) {
            SetJoin<Peruste, Peruste> perusteet = joined.join(Peruste_.oppaanPerusteet);
            result = cb.and(result, perusteet.get(Peruste_.id).in(pq.getPerusteet()));
        }


        Join<Perusteprojekti, Peruste> peruste = root.join(Perusteprojekti_.peruste);

        if (!ObjectUtils.isEmpty(pq.getTila())) {
            result = cb.and(result, root.get(Perusteprojekti_.tila).in(pq.getTila()));
        }

        final Expression<Date> voimassaoloAlkaa = peruste.get(Peruste_.voimassaoloAlkaa);
        final Expression<Date> voimassaoloLoppuu = peruste.get(Peruste_.voimassaoloLoppuu);
        final Expression<Date> siirtymaPaattyy = peruste.get(Peruste_.siirtymaPaattyy);
        Expression<java.sql.Date> currentDate = cb.literal(new java.sql.Date(new Date().getTime()));

        if (pq.isTuleva()) {
            result = cb.and(result, cb.and(cb.isNotNull(voimassaoloAlkaa), cb.lessThan(currentDate, voimassaoloAlkaa)));
        } else if (pq.isVoimassaolo()) {
            Predicate alkaa = cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa));
            Predicate loppuu = cb.and(cb.isNotNull(voimassaoloLoppuu), cb.lessThanOrEqualTo(currentDate, voimassaoloLoppuu));
            Predicate pr1 = cb.and(alkaa, loppuu);

            // Voimassaolon loppumista ei ole määritelty
            Predicate pr2 = cb.and(cb.isNull(voimassaoloLoppuu),
                    cb.and(cb.isNotNull(voimassaoloAlkaa), cb.greaterThanOrEqualTo(currentDate, voimassaoloAlkaa)));

            // Voimassaolon alkamista ei ole määritelty
            Predicate pr3 = cb.and(cb.isNull(voimassaoloAlkaa),
                    cb.and(cb.isNotNull(voimassaoloLoppuu), cb.lessThanOrEqualTo(currentDate, voimassaoloLoppuu)));

            // Voimassaolon alkamista tai loppumista ei ole määritelty
            Predicate pr4 = cb.and(cb.isNull(voimassaoloAlkaa), cb.isNull(voimassaoloLoppuu));

            result = cb.and(result, cb.or(pr1, pr2, pr3, pr4));

        } else if (pq.isSiirtyma()) {
            Predicate alkaa = cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu));
            Predicate loppuu = cb.and(cb.isNotNull(siirtymaPaattyy), cb.lessThanOrEqualTo(currentDate, siirtymaPaattyy));
            result = cb.and(result, cb.and(alkaa, loppuu));
        } else if (pq.isPoistunut()) {
            Predicate siirtymapoistunut = cb.and(cb.isNotNull(siirtymaPaattyy), cb.greaterThan(currentDate, siirtymaPaattyy));

            // Siirtymän loppumista ei ole määritelty
            Predicate poistunut = cb.and(cb.isNull(siirtymaPaattyy),
                    cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(currentDate, voimassaoloLoppuu)));

            result = cb.and(result, cb.or(siirtymapoistunut, poistunut));
        }

        return result;
    }
}
