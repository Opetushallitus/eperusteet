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

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepositoryCustom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author nkala
 */
@Slf4j
public class PerusteprojektiRepositoryImpl implements PerusteprojektiRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Perusteprojekti> findBy(PageRequest page, PerusteprojektiQueryDto pquery) {
        TypedQuery<Long> countQuery = getCountQuery(pquery);
        TypedQuery<Tuple> query = getQuery(pquery);
        if (page != null) {
            query.setFirstResult(page.getOffset());
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
        query.multiselect(root, nimi, perusteVersion, voimassaoloAlkaa, voimassaoloLoppuu, koulutustyyppi).where(pred).orderBy(order);
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

        if (pq.getTyyppi() == null) {
            result = cb.and(result, cb.notEqual(tyyppi, PerusteTyyppi.OPAS));
        }
        else {
            result = cb.and(result, cb.equal(tyyppi, pq.getTyyppi()));
        }

        if (!ObjectUtils.isEmpty(pq.getKoulutustyyppi())) {
            if (PerusteTyyppi.OPAS.equals(pq.getTyyppi())) {
                SetJoin<Peruste, KoulutusTyyppi> koulutustyypit = joined.join(Peruste_.oppaanKoulutustyypit);

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

        if (PerusteTyyppi.OPAS.equals(pq.getTyyppi()) && !CollectionUtils.isEmpty(pq.getPerusteet())) {
            SetJoin<Peruste, Peruste> perusteet = joined.join(Peruste_.oppaanPerusteet);
            result = cb.and(result, perusteet.get(Peruste_.id).in(pq.getPerusteet()));
        }


        if (!ObjectUtils.isEmpty(pq.getTila())) {
            return cb.and(result, root.get(Perusteprojekti_.tila).in(pq.getTila()));
        } else {
            return result;
        }
    }
}
