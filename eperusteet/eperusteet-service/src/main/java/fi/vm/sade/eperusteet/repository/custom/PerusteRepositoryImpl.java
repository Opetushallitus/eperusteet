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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutusala_;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti_;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Peruste_;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TekstiPalanen_;
import fi.vm.sade.eperusteet.repository.PerusteRepositoryCustom;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
     * @param kieli {@link Kieli}. Ei voi olla null
     * @param nimi Perusteen nimi. Voi olla null.
     * @param ala Lista koulutusalakoodeja. Voi olla null.
     * @param tyyppi Lista perusteen tyyppejä. Voi olla null.
     * @param page Sivutusmääritys.
     * @return Yhden hakusivun verran vastauksia
     */
    @Override
    public Page<Peruste> findBy(Kieli kieli, String nimi, List<String> ala, List<String> tyyppi, Pageable page) {

        TypedQuery<Long> countQuery = getCountQuery(kieli, nimi, ala, tyyppi);
        TypedQuery<Peruste> query = getQuery(kieli, nimi, ala, tyyppi);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }

        return new PageImpl<>(query.getResultList(), page, countQuery.getSingleResult());
    }

    @Override
    public Peruste findById(Long id) {
        EntityGraph<Peruste> eg = em.createEntityGraph(Peruste.class);
        eg.addSubgraph(Peruste_.rakenne);
        HashMap<String, Object> props = new HashMap<>();
        props.put(QueryHints.FETCHGRAPH, eg);
        Peruste p = em.find(Peruste.class, id, props);
        //p.getRakenne().getPerusteenOsa().getOtsikko();
        return p;
    }

    private TypedQuery<Peruste> getQuery(Kieli kieli, String nimi, List<String> ala, List<String> tyyppi) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Peruste> query = cb.createQuery(Peruste.class);
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, kieli, nimi, ala, tyyppi);
        query.select(root).where(pred).orderBy(cb.asc(cb.lower(teksti.get(LokalisoituTeksti_.teksti))));

        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(Kieli kieli, String nimi, List<String> ala, List<String> tyyppi) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, kieli, nimi, ala, tyyppi);
        query.select(cb.count(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(
            Root<Peruste> root, Join<TekstiPalanen, LokalisoituTeksti> teksti, CriteriaBuilder cb,
            Kieli kieli, String nimi, List<String> ala, List<String> tyyppi) {

        Predicate pred = cb.equal(teksti.get(LokalisoituTeksti_.kieli), kieli);
        if (nimi != null) {
            pred = cb.and(pred, cb.like(cb.lower(teksti.get(LokalisoituTeksti_.teksti)), cb.literal(RepositoryUtil.kuten(nimi))));
        }
        if (ala != null && !ala.isEmpty()) {
            pred = cb.and(pred, root.get(Peruste_.koulutusala).get(Koulutusala_.koodi).in(ala));
        }
        if (tyyppi != null && !tyyppi.isEmpty()) {
            pred = cb.and(pred, root.get(Peruste_.tutkintokoodi).in(tyyppi));
        }
        return pred;
    }

}
