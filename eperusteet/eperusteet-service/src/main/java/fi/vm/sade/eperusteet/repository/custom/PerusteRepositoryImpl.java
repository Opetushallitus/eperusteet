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
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Koulutus_;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti_;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste_;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapa_;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TekstiPalanen_;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.repository.PerusteRepositoryCustom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(PerusteRepositoryImpl.class);

    /**
     * Etsi Peruste määritellyillä hakuehdoilla (sivutettu kysely)
     *
     * @param page sivumääritys
     * @param pquery hakuparametrit
     *
     * @return Yhden hakusivun verran vastauksia
     */
    @Override
    public Page<Peruste> findBy(PageRequest page, PerusteQuery pquery) {

        TypedQuery<Long> countQuery = getCountQuery(pquery);
        TypedQuery<Tuple> query = getQuery(pquery);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }
        return new PageImpl<>(Lists.transform(query.getResultList(), EXTRACT_PERUSTE), page, countQuery.getSingleResult());
    }

    @Override
    public Peruste findById(Long id) {
        EntityGraph<Peruste> eg = em.createEntityGraph(Peruste.class);
        eg.addSubgraph(Peruste_.koulutukset);
        eg.addSubgraph(Peruste_.suoritustavat);
        HashMap<String, Object> props = new HashMap<>();
        props.put(QueryHints.FETCHGRAPH, eg);
        Peruste p = em.find(Peruste.class, id, props);
        return p;
    }

    private TypedQuery<Tuple> getQuery(PerusteQuery pquery) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery);
        query.distinct(true);
        final Expression<String> n = cb.lower(teksti.get(LokalisoituTeksti_.teksti));
        query.multiselect(root, n).where(pred).orderBy(cb.asc(n));
        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(PerusteQuery pquery) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(
        Root<Peruste> root, Join<TekstiPalanen, LokalisoituTeksti> teksti, CriteriaBuilder cb, PerusteQuery pquery) {

        final Kieli kieli = Kieli.of(pquery.getKieli());
        final String nimi = pquery.getNimi();
        final String st = pquery.getSuoritustapa();
        final List<String> koulutusala = pquery.getKoulutusala();
        final List<String> tyyppi = pquery.getTyyppi();
        final List<String> opintoala = pquery.getOpintoala();
        final boolean siirtyma = pquery.isSiirtyma();
        final String tilaStr = pquery.getTila();
        final String perusteTyyppiStr = pquery.getPerusteTyyppi();
        final Expression<Date> siirtymaAlkaa = root.get(Peruste_.siirtymaAlkaa);
        final Expression<Date> voimassaoloLoppuu = root.get(Peruste_.voimassaoloLoppuu);

        Predicate pred = cb.equal(teksti.get(LokalisoituTeksti_.kieli), kieli);

        if (nimi != null) {
            pred = cb.and(pred, cb.like(cb.lower(teksti.get(LokalisoituTeksti_.teksti)), cb.literal(RepositoryUtil.kuten(nimi))));
        }

        if (st != null) {
            Suoritustapakoodi suoritustapakoodi = Suoritustapakoodi.of(st);
            Join<Peruste, Suoritustapa> suoritustapa = root.join(Peruste_.suoritustavat);
            pred = cb.and(pred, cb.equal(suoritustapa.get(Suoritustapa_.suoritustapakoodi), suoritustapakoodi));
        }

        if (tyyppi != null && !tyyppi.isEmpty()) {
            pred = cb.and(pred, root.get(Peruste_.koulutustyyppi).in(tyyppi));
        }

        Join<Peruste, Koulutus> koulutukset = null;
        if (koulutusala != null && !koulutusala.isEmpty()) {
            koulutukset = root.join(Peruste_.koulutukset);
            pred = cb.and(pred, koulutukset.get(Koulutus_.koulutusalakoodi).in(koulutusala));
        }

        if (opintoala != null && !opintoala.isEmpty()) {
            koulutukset = (koulutukset == null ) ? root.join(Peruste_.koulutukset) : koulutukset;
            pred = cb.and(pred, koulutukset.get(Koulutus_.opintoalakoodi).in(opintoala));
        }

        if (!Strings.isNullOrEmpty(pquery.getKoodiArvo())) {
            koulutukset = (koulutukset == null ) ? root.join(Peruste_.koulutukset) : koulutukset;
            pred = cb.and(pred, cb.equal(koulutukset.get(Koulutus_.koulutuskoodiArvo), pquery.getKoodiArvo()));
        }

        if (siirtyma) {
            pred = cb.and(pred, cb.or(cb.isNull(voimassaoloLoppuu), cb.greaterThan(voimassaoloLoppuu, cb.currentDate())));
        } else {
            pred = cb.and(pred, cb.and(cb.or(cb.isNull(siirtymaAlkaa), cb.greaterThan(siirtymaAlkaa, cb.currentDate())),
                                       cb.or(cb.isNull(voimassaoloLoppuu), cb.greaterThan(voimassaoloLoppuu, cb.currentDate()))));
        }

        if (!Strings.isNullOrEmpty(tilaStr)) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.tila), PerusteTila.of(tilaStr)));
        }
        
        if (!Strings.isNullOrEmpty(perusteTyyppiStr)) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.tyyppi), PerusteTyyppi.of(perusteTyyppiStr)));
        }

        return pred;
    }

    private static final Function<Tuple, Peruste> EXTRACT_PERUSTE = new Function<Tuple, Peruste>() {
        @Override
        public Peruste apply(Tuple f) {
            return f.get(0, Peruste.class);
        }
    };

}
