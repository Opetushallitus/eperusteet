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
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.repository.TiedoteRepositoryCustom;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TiedoteRepositoryImpl implements TiedoteRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Tiedote> findBy(PageRequest page, TiedoteQuery tquery) {
        TypedQuery<Long> countQuery = getCountQuery(tquery);
        TypedQuery<Tuple> query = getQuery(page, tquery);
        query.setFirstResult(page.getOffset());
        query.setMaxResults(page.getPageSize());

        log.debug(query.unwrap(Query.class).getQueryString());

        List<Tiedote> result = query.getResultList().stream()
                .map(t -> t.get(0, Tiedote.class))
                .collect(Collectors.toList());

        return new PageImpl<>(result, page, countQuery.getSingleResult());
    }

    private TypedQuery<Long> getCountQuery(TiedoteQuery tquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Tiedote> root = query.from(Tiedote.class);

        Predicate pred = buildPredicate(root, cb, tquery);
        query.select(cb.countDistinct(root)).where(pred);

        return em.createQuery(query);
    }

    private TypedQuery<Tuple> getQuery(PageRequest page, TiedoteQuery tquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Tiedote> root = query.from(Tiedote.class);

        Predicate pred = buildPredicate(root, cb, tquery);

        if (!ObjectUtils.isEmpty(tquery.getKieli())) {
            Join<TekstiPalanen, LokalisoituTeksti> otsikko = root.join(Tiedote_.otsikko).join(TekstiPalanen_.teksti);
            Expression<String> otsikkoExpression = cb.lower(otsikko
                    .on(cb.equal(otsikko.get(LokalisoituTeksti_.kieli), tquery.getKieli().iterator().next()))
                    .get(LokalisoituTeksti_.teksti));

            query = query.multiselect(root, otsikkoExpression);
        } else {
            query = query.multiselect(root);
        }

        List<Order> orders = new ArrayList<>();
        Sort sort = page.getSort();
        sort.forEach(order -> {
            if (order.getDirection().equals(Sort.Direction.ASC)) {
                orders.add(cb.asc(root.get(order.getProperty())));
            } else {
                orders.add(cb.desc(root.get(order.getProperty())));
            }
        });

        query
                .where(pred)
                .orderBy(orders)
                .distinct(true);

        return em.createQuery(query);
    }

    private Predicate buildPredicate(
            Root<Tiedote> root,
            CriteriaBuilder cb,
            TiedoteQuery tq
    ) {
        Predicate pred = cb.conjunction();

        if (!ObjectUtils.isEmpty(tq.getKieli())) {
            Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Tiedote_.otsikko).join(TekstiPalanen_.teksti);
            Optional<Predicate> kieliPred = tq.getKieli().stream()
                    .map((lang) -> cb.equal(teksti.get(LokalisoituTeksti_.kieli), lang))
                    .reduce(cb::or);
            if (kieliPred.isPresent()) {
                pred = cb.and(pred, kieliPred.get());
            }
        }

        if (!ObjectUtils.isEmpty(tq.getNimi())) {
            Expression<String> nimiLit = cb.literal(RepositoryUtil.kuten(tq.getNimi()));
            Join<TekstiPalanen, LokalisoituTeksti> otsikko = root.join(Tiedote_.otsikko).join(TekstiPalanen_.teksti);
            Predicate nimessa = cb.like(cb.lower(otsikko.get(LokalisoituTeksti_.teksti)), nimiLit);
            pred = cb.and(pred, nimessa);
        }

        Join<Perusteprojekti, Peruste> peruste = null;
        if (!ObjectUtils.isEmpty(tq.getPerusteeton())) {
            peruste = root.join(Tiedote_.perusteprojekti).join(Perusteprojekti_.peruste);
            pred = cb.and(pred, cb.isNull(peruste));
        }


        if (!ObjectUtils.isEmpty(tq.getPerusteId())) {
            if (peruste == null) {
                peruste = root.join(Tiedote_.perusteprojekti).join(Perusteprojekti_.peruste);
            }
            pred = cb.and(pred, cb.equal(peruste.get(Peruste_.id), tq.getPerusteId()));
        }

        if (!ObjectUtils.isEmpty(tq.getJulkinen())) {
            pred = cb.and(pred, cb.equal(root.get(Tiedote_.julkinen), tq.getJulkinen()));
        }

        if (!ObjectUtils.isEmpty(tq.getYleinen())) {
            pred = cb.and(pred, cb.equal(root.get(Tiedote_.yleinen), tq.getYleinen()));
        }

        if (!ObjectUtils.isEmpty(tq.getTiedoteJulkaisuPaikka())) {
            SetJoin<Tiedote, TiedoteJulkaisuPaikka> julkaisupaikat = root.join(Tiedote_.julkaisupaikat);
            Optional<Predicate> kieliPred = tq.getTiedoteJulkaisuPaikka().stream()
                    .map((julkaisupaikka) -> cb.equal(julkaisupaikat, TiedoteJulkaisuPaikka.of(julkaisupaikka)))
                    .reduce(cb::or);

            if (kieliPred.isPresent()) {
                pred = cb.and(pred, kieliPred.get());
            }
        }

        if (!ObjectUtils.isEmpty(tq.getKoulutusTyyppi())) {
            SetJoin<Tiedote, KoulutusTyyppi> koulutustyypit = root.join(Tiedote_.koulutustyypit);
            Optional<Predicate> koulutustyyppiPred = tq.getKoulutusTyyppi().stream()
                    .map((koulutustyyppi) -> cb.equal(koulutustyypit, KoulutusTyyppi.of(koulutustyyppi)))
                    .reduce(cb::or);

            if (koulutustyyppiPred.isPresent()) {
                pred = cb.and(pred, koulutustyyppiPred.get());
            }
        }

        if (!ObjectUtils.isEmpty(tq.getPerusteIds())) {
            Join<Tiedote, Peruste> perusteet = root.join(Tiedote_.perusteet);
            pred = cb.and(pred, perusteet.get(Peruste_.id).in(tq.getPerusteIds()));
        }

        return pred;
    }
}
