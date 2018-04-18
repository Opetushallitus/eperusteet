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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
     * @param koodistostaHaetut koodistosta haetut
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

        List<Peruste> result = query.getResultList().stream()
                .map(t -> t.get(0, Peruste.class))
                .collect(Collectors.toList());

        return new PageImpl<>(result, page, countQuery.getSingleResult());
    }

    private TypedQuery<Tuple> getQuery(PerusteQuery pquery, Set<Long> koodistostaHaetut) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery, koodistostaHaetut);

        final List<Order> order = new ArrayList<>();
        if ("muokattu".equals(pquery.getJarjestys())) {
            order.add(cb.desc(root.get(Peruste_.muokattu)));
        }

        if (pquery.getKieli() != null && pquery.getKieli().size() == 1) {
            Expression<String> n = cb.lower(teksti
                    .on(cb.equal(teksti.get(LokalisoituTeksti_.kieli), Kieli.of(pquery.getKieli().iterator().next())))
                    .get(LokalisoituTeksti_.teksti));
            order.add(cb.asc(n));
            query = query.multiselect(root, n);
        }
        else {
            query = query.multiselect(root);
        }

        order.add(cb.asc(root.get(Peruste_.id)));

        query
                .where(pred)
                .orderBy(order)
                .distinct(true);

        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(PerusteQuery pquery, Set<Long> koodistostaHaetut) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Peruste> root = query.from(Peruste.class);
        Join<TekstiPalanen, LokalisoituTeksti> teksti = root.join(Peruste_.nimi).join(TekstiPalanen_.teksti);
        Predicate pred = buildPredicate(root, teksti, cb, pquery, koodistostaHaetut);

        query
                .select(cb.countDistinct(root))
                .where(pred)
                .distinct(true);

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
        Expression<java.sql.Date> currentDate = cb.literal(new java.sql.Date(pq.getNykyinenAika()));
        final Set<Kieli> kieli;
        if (ObjectUtils.isEmpty(pq.getKieli())) {
            kieli = new HashSet<>();
            kieli.addAll(Arrays.asList(Kieli.values()));
        } else {
            kieli = pq.getKieli().stream()
                    .map(Kieli::of)
                    .collect(Collectors.toSet());
        }

        Predicate pred = cb.conjunction();

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

            pred = cb.and(pred, cb.or(preds.toArray(new Predicate[0])));
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
        if (!ObjectUtils.isEmpty(pq.getKoulutustyyppi())) {
            pred = cb.and(pred, root.get(Peruste_.koulutustyyppi).in(pq.getKoulutustyyppi()));
        }

        Join<Peruste, Koulutus> koulutukset = null;
        if (!ObjectUtils.isEmpty(pq.getKoulutusala())) {
            koulutukset = root.join(Peruste_.koulutukset);
            pred = cb.and(pred, koulutukset.get(Koulutus_.koulutusalakoodi).in(pq.getKoulutusala()));
        }

        if (!ObjectUtils.isEmpty(pq.getOpintoala())) {
            koulutukset = (koulutukset == null) ? root.join(Peruste_.koulutukset) : koulutukset;
            pred = cb.and(pred, koulutukset.get(Koulutus_.opintoalakoodi).in(pq.getOpintoala()));
        }

        if (!StringUtils.isEmpty(pq.getKoulutuskoodi())) {
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

        if (!ObjectUtils.isEmpty(pq.getTila())) {
            Set<PerusteTila> perusteTilat = pq.getTila().stream()
                    .map(PerusteTila::of)
                    .collect(Collectors.toSet());
            pred = cb.and(pred, root.get(Peruste_.tila).in(perusteTilat));
        }

        if (!StringUtils.isEmpty(pq.getPerusteTyyppi())) {
            pred = cb.and(pred, cb.equal(root.get(Peruste_.tyyppi), PerusteTyyppi.of(pq.getPerusteTyyppi())));
        }

        if (pq.getMuokattu() != null) {
            pred = cb.and(pred, cb.greaterThan(root.join(Peruste_.globalVersion)
                    .get(PerusteVersion_.aikaleima), cb.literal(new Date(pq.getMuokattu()))));
        }

        if (!ObjectUtils.isEmpty(pq.getKieli())) {
            SetJoin<Peruste, Kieli> kielet = root.join(Peruste_.kielet);
            Optional<Predicate> kieliPred = kieli.stream()
                    .map((lang) -> cb.equal(kielet, lang))
                    .reduce(cb::or);

            if (kieliPred.isPresent()) {
                pred = cb.and(pred, kieliPred.get());
            }
        }

        // Tutkinnon osien tuontia varten
        if (pq.getEsikatseltavissa() != null && pq.getEsikatseltavissa()) {
            Join<Peruste, Perusteprojekti> perusteprojekti = root.join(Peruste_.perusteprojekti);

            // Jos peruste on esikatseltavissa tai/ja julkaistu
            Predicate esikatseltavissaTaiJulkaistu = cb.disjunction();
            esikatseltavissaTaiJulkaistu = cb.or(esikatseltavissaTaiJulkaistu,
                    cb.isTrue(perusteprojekti.get(Perusteprojekti_.esikatseltavissa)));
            esikatseltavissaTaiJulkaistu = cb.or(esikatseltavissaTaiJulkaistu,
                    cb.isTrue(cb.equal(root.get(Peruste_.tila), PerusteTila.VALMIS)));

            pred = cb.and(pred, esikatseltavissaTaiJulkaistu);
        }

        return pred;
    }
}
