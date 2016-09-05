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
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.repository.PerusteRepositoryCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    private TypedQuery<Tuple> getQuery(PerusteQuery pquery) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery);
        query.distinct(true);
        final Expression<String> n = cb.lower(teksti.get(LokalisoituTeksti_.teksti));

        final List<Order> order = new ArrayList<>();
        if ( "muokattu".equals(pquery.getJarjestys()) ) {
            order.add(cb.desc(root.get(Peruste_.muokattu)));
        }
        order.add(cb.asc(n));
        order.add(cb.asc(root.get(Peruste_.id)));
        query.multiselect(root, n).where(pred).orderBy(order);
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
        Root<Peruste> root, Join<TekstiPalanen, LokalisoituTeksti> teksti, CriteriaBuilder cb, PerusteQuery pq) {
        final Expression<Date> siirtymaPaattyy = root.get(Peruste_.siirtymaPaattyy);
        final Expression<Date> voimassaoloLoppuu = root.get(Peruste_.voimassaoloLoppuu);
        final Kieli kieli = Kieli.of(pq.getKieli());

        Predicate pred = cb.equal(teksti.get(LokalisoituTeksti_.kieli), kieli);

        if (pq.getNimi() != null) {
            pred = cb.and(pred, cb.like(cb.lower(teksti.get(LokalisoituTeksti_.teksti)), cb.literal(RepositoryUtil.kuten(pq.getNimi()))));
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
            pred = cb.and(pred, cb.or(cb.equal(koulutukset.get(Koulutus_.koulutuskoodiUri), pq.getKoulutuskoodi()), cb.equal(koulutukset
                                      .get(Koulutus_.koulutuskoodiArvo), pq.getKoulutuskoodi())));
        }

        if (pq.isSiirtyma()) {
            pred = cb.and(pred, cb.and(cb.isNotNull(siirtymaPaattyy), cb.greaterThan(siirtymaPaattyy, cb.currentDate())));
        }

        if (pq.isVoimassaolo()) {
            pred = cb.and(pred, cb.and(cb.isNotNull(voimassaoloLoppuu), cb.greaterThan(voimassaoloLoppuu, cb.currentDate())));
        }

        if (!Strings.isNullOrEmpty(pq.getTila())) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.tila), PerusteTila.of(pq.getTila())));
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

        return pred;
    }

    private static boolean empty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static final Function<Tuple, Peruste> EXTRACT_PERUSTE = new Function<Tuple, Peruste>() {
        @Override
        public Peruste apply(Tuple f) {
            return f.get(0, Peruste.class);
        }
    };

}
