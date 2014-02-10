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

import java.util.Date;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutusala_;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti_;
import fi.vm.sade.eperusteet.domain.Opintoala;
import fi.vm.sade.eperusteet.domain.Opintoala_;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Peruste_;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TekstiPalanen_;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.repository.PerusteRepositoryCustom;
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
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
     * @param koulutusala Lista koulutusalakoodeja. Voi olla null.
     * @param tyyppi Lista perusteen tyyppejä. Voi olla null.
     * @param page Sivutusmääritys.
     * @param opintoala Lista opintoalakoodeja. Voi olla null.
     * @param siirtyma Näyttää myös siirtymäajalla olevat.
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
        eg.addSubgraph(Peruste_.rakenne);
        eg.addSubgraph(Peruste_.opintoalat);
        HashMap<String, Object> props = new HashMap<>();
        props.put(QueryHints.FETCHGRAPH, eg);
        Peruste p = em.find(Peruste.class, id, props);
        //p.getRakenne().getPerusteenOsa().getOtsikko();
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

        Kieli kieli = Kieli.of(pquery.getKieli());

        Predicate pred = cb.equal(teksti.get(LokalisoituTeksti_.kieli), kieli);
        if (pquery.getNimi() != null) {
            pred = cb.and(pred, cb.like(cb.lower(teksti.get(LokalisoituTeksti_.teksti)), cb.literal(RepositoryUtil.kuten(pquery.getNimi()))));
        }
        if (pquery.getKoulutusala() != null && !pquery.getKoulutusala().isEmpty()) {
            pred = cb.and(pred, root.get(Peruste_.koulutusala).get(Koulutusala_.koodi).in(pquery.getKoulutusala()));
        }
        if (pquery.getTyyppi() != null && !pquery.getTyyppi().isEmpty()) {
            pred = cb.and(pred, root.get(Peruste_.tutkintokoodi).in(pquery.getTyyppi()));
        }
        if (pquery.getOpintoala() != null && !pquery.getOpintoala().isEmpty()) {
            ListJoin<Peruste, Opintoala> ala = root.join(Peruste_.opintoalat);
            pred = cb.and(pred, ala.get(Opintoala_.koodi).in(pquery.getOpintoala()));
        }
        if (!pquery.isSiirtyma()) {
            Expression<Date> rsiirtyma = root.get(Peruste_.siirtyma);
            pred = cb.and(pred, cb.or(cb.isNull(rsiirtyma),
                                      cb.greaterThan(rsiirtyma, cb.currentDate())));
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
