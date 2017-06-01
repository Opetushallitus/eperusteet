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
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Perusteprojekti_;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepositoryCustom;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 *
 * @author nkala
 */
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
        return new PageImpl<>(Lists.transform(query.getResultList(), EXTRACT_PERUSTEPROJEKTI), page, countQuery.getSingleResult());
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
        final Expression<String> n = cb.lower(root.get(Perusteprojekti_.nimi));

        final List<Order> order = new ArrayList<>();
        order.add(cb.asc(n));
        order.add(cb.asc(root.get(Perusteprojekti_.id)));
        query.multiselect(root, n).where(pred).orderBy(order);

        return em.createQuery(query);
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

        if (pq.getTila() != null) {
            return cb.and(nimihaku, cb.equal(targetTila, pq.getTila()));
        }
        else {
            return nimihaku;
        }
    }
}
