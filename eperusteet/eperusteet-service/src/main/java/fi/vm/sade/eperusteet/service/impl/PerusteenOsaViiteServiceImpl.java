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
package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.ArviointiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.security.PermissionChecker;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
@Transactional(readOnly = true)
public class PerusteenOsaViiteServiceImpl implements PerusteenOsaViiteService {

    @Autowired
    private ArviointiService arviointiService;

    @PersistenceContext
    private EntityManager em;
    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteenOsaViiteRepository repository;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(Long perusteId, Suoritustapakoodi tapa, Long id) {
        PerusteenOsaViite pov = findViite(perusteId, tapa, id);
        PerusteenOsa from = pov.getPerusteenOsa();
        permissionChecker.checkPermission(from, "LUKU");
        if (from instanceof TekstiKappale) {
            PerusteenOsa uusi = from.copy();
            uusi.setTila(PerusteTila.LUONNOS);
            pov.setPerusteenOsa(perusteenOsaRepository.save(uusi));
        }
        return mapper.map(pov, fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = false)
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(Long perusteId, Suoritustapakoodi tapa, Long id) {
        Suoritustapa suoritustapa = perusteet.findSuoritustapaByIdAndSuoritustapakoodi(perusteId, tapa);
        if (suoritustapa != null) {
            TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(id);
            if (suoritustapa.getTutkinnonOsat().contains(tov)) {
                TutkinnonOsa to = tov.getTutkinnonOsa();
                TutkinnonOsa uusi = new TutkinnonOsa(to);
                uusi.setTila(PerusteTila.LUONNOS);
                tov.setTutkinnonOsa(tutkinnonOsaRepository.save(uusi));
                return mapper.map(tov, TutkinnonOsaViiteDto.class);
            }
        }
        throw new BusinessRuleViolationException("virheellinen viite");
    }

    @Override
    @Transactional
    public void removeSisalto(Long perusteId, Suoritustapakoodi tapa, Long id) {
        PerusteenOsaViite viite = findViite(perusteId, tapa, id);
        if (viite == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }

        if (viite.getVanhempi() == null) {
            throw new BusinessRuleViolationException("Suoritustavan juurielementtiä ei voi poistaa");
        }

        if (viite.getLapset() != null && !viite.getLapset().isEmpty()) {
            throw new BusinessRuleViolationException("Sisällöllä on lapsia, ei voida poistaa");
        }

        if (viite.getPerusteenOsa() != null && viite.getPerusteenOsa().getTila().equals(PerusteTila.LUONNOS)) {
            PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
            perusteenOsaService.delete(perusteenOsa.getId());

            viite.setPerusteenOsa(null);
            viite.getVanhempi().getLapset().remove(viite);
            viite.setVanhempi(null);
            repository.delete(viite);
        }
    }

    @Override
    @Transactional
    public void reorderSubTree(Long perusteId, Suoritustapakoodi suoritustapa, Long id, PerusteenOsaViiteDto.Puu<?, ?> uusi) {
        PerusteenOsaViite viite = findViite(perusteId, suoritustapa, id);
        Set<PerusteenOsaViite> refs = Sets.newIdentityHashSet();
        refs.add(viite);
        clearChildren(viite, refs);
        PerusteenOsaViite parent = viite.getVanhempi();
        updateTraverse(parent, uusi, refs);
    }

    @Autowired
    private PerusteRepository perusteet;

    private PerusteenOsaViite findViite(Long perusteId, Suoritustapakoodi suoritusTapa, Long viiteId) {
        Suoritustapa suoritustapa = perusteet.findSuoritustapaByIdAndSuoritustapakoodi(perusteId, suoritusTapa);
        if (suoritustapa != null) {
            PerusteenOsaViite viite = repository.findOne(viiteId);
            PerusteenOsaViite root = viite;
            while (root != null && root.getVanhempi() != null) {
                root = root.getVanhempi();
            }
            if (root != null && root.equals(suoritustapa.getSisalto())) {
                return viite;
            }
        }
        throw new BusinessRuleViolationException("virheellinen viite");
    }

    private void clearChildren(PerusteenOsaViite pov, Set<PerusteenOsaViite> refs) {
        for (PerusteenOsaViite lapsi : pov.getLapset()) {
            refs.add(lapsi);
            clearChildren(lapsi, refs);
        }
        pov.setVanhempi(null);
        pov.getLapset().clear();
    }

    private PerusteenOsaViite updateTraverse(PerusteenOsaViite parent, PerusteenOsaViiteDto.Puu<?, ?> uusi, Set<PerusteenOsaViite> refs) {
        PerusteenOsaViite pov = repository.getOne(uusi.getId());
        if (!refs.remove(pov)) {
            throw new BusinessRuleViolationException("viitepuun päivitysvirhe");
        }
        pov.setVanhempi(parent);

        List<PerusteenOsaViite> lapset = pov.getLapset();
        lapset.clear();

        for (PerusteenOsaViiteDto.Puu<?, ?> x : uusi.getLapset()) {
            lapset.add(updateTraverse(pov, x, refs));
        }
        return repository.save(pov);
    }

}
