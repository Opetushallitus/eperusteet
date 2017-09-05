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
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepositoryCustom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

/**
 *
 * @author nkala
 */
@Slf4j
public class PerusteprojektiRepositoryImpl implements PerusteprojektiRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final Function<Tuple, Perusteprojekti> EXTRACT_PERUSTEPROJEKTI = f -> f.get(0, Perusteprojekti.class);

    @Override
    public Page<Perusteprojekti> findBy(PageRequest page, PerusteprojektiQueryDto pquery) {
        TypedQuery<Long> countQuery = getCountQuery(pquery);
        TypedQuery<Tuple> query = getQuery(pquery);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }

        // SQL query
        //log.debug(query.unwrap(Query.class).getQueryString());

        List<Tuple> resultList = query.getResultList();
        Long singleResult = countQuery.getSingleResult();

        return new PageImpl<>(Lists.transform(resultList, EXTRACT_PERUSTEPROJEKTI), page, singleResult);
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
                default:
                    addOrderExpression(cb, order, nimi, false);
                    break;
            }
        } else {
            order.add(cb.asc(nimi));
        }
        order.add(cb.asc(root.get(Perusteprojekti_.id)));

        query.multiselect(root, nimi, perusteVersion).where(pred).orderBy(order);

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
        Expression<ProjektiTila> targetTila = root.get(Perusteprojekti_.tila);

        Expression<String> haku = cb.literal(RepositoryUtil.kuten(pq.getNimi()));
        Expression<Diaarinumero> diaarihaku = cb.literal(new Diaarinumero(pq.getNimi()));

        Predicate nimessa = cb.like(targetName, haku);
        Predicate diaarissa = cb.equal(targetDiaari, diaarihaku);
        Predicate nimihaku = cb.or(nimessa, diaarissa);

        if (!empty(pq.getKoulutustyyppi())) {
            Join<Perusteprojekti, Peruste> peruste = root.join(Perusteprojekti_.peruste);
            nimihaku = cb.and(nimihaku, peruste.get(Peruste_.koulutustyyppi).in(pq.getKoulutustyyppi()));
        }

        if (!empty(pq.getTila())) {
            return cb.and(nimihaku, root.get(Perusteprojekti_.tila).in(pq.getTila()));
        } else {
            return nimihaku;
        }
    }

    private static boolean empty(Collection<?> c) {
        return c == null || c.isEmpty();
    }
}
