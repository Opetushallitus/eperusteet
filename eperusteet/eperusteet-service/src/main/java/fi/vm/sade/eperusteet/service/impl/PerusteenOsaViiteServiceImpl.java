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
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.dto.SortableDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.PoistoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.security.PermissionChecker;
import fi.vm.sade.eperusteet.service.security.PermissionManager;

import java.util.*;

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

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    private PoistoService poistoService;

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(Long perusteId, Long id) {
        PerusteenOsaViite pov = findViite(perusteId, id);
        PerusteenOsa from = pov.getPerusteenOsa();
        permissionChecker.checkPermission(from, "LUKU");
        if (from instanceof TekstiKappale) {
            PerusteenOsa uusi = from.copy();
            uusi.asetaTila(PerusteTila.LUONNOS);
            pov.setPerusteenOsa(perusteenOsaRepository.save(uusi));
        }
        muokkausTietoService.addMuokkaustieto(perusteId, pov, MuokkausTapahtuma.KOPIOINTI);
        return mapper.map(pov, fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = false)
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(Long perusteId, Suoritustapakoodi tapa, Long id) {
        Suoritustapa suoritustapa = perusteet.findSuoritustapaByIdAndSuoritustapakoodi(perusteId, tapa);
        Peruste peruste = perusteet.findOne(perusteId);
        if (suoritustapa != null) {
            TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(id);
            if (suoritustapa.getTutkinnonOsat().contains(tov)) {
                TutkinnonOsa to = tov.getTutkinnonOsa();
                TutkinnonOsa uusi = new TutkinnonOsa(to);
                uusi.asetaAlkuperainenPeruste(peruste);
                uusi.asetaTila(PerusteTila.LUONNOS);
                tov.setTutkinnonOsa(tutkinnonOsaRepository.save(uusi));
                muokkausTietoService.addMuokkaustieto(perusteId, tov, MuokkausTapahtuma.KOPIOINTI);
                return mapper.map(tov, TutkinnonOsaViiteDto.class);
            }
        }
        throw new BusinessRuleViolationException("virheellinen viite");
    }

    @Override
    public <T extends PerusteenOsaViiteDto<?>> T getSisalto(Long perusteId, Long viiteId, Class<T> view) {
        return mapper.map(findViite(perusteId, viiteId), view);
    }

    @Override
    @Transactional
    public void removeSisalto(Long perusteId, Long id) {
        PerusteenOsaViite viite = findViite(perusteId, id);
        if (viite == null) {
            throw new NotExistsException("Perusteenosaviitettä ei ole olemassa");
        }

        if (viite.getVanhempi() == null) {
            throw new BusinessRuleViolationException("Sisällön juurielementtiä ei voi poistaa");
        }

        if (viite.getLapset() != null && !viite.getLapset().isEmpty()) {
            throw new BusinessRuleViolationException("Sisällöllä on lapsia, ei voida poistaa");
        }

        muokkausTietoService.addMuokkaustieto(perusteId, viite, MuokkausTapahtuma.POISTO);
        if (viite.getPerusteenOsa() != null && viite.getPerusteenOsa().getTila().equals(PerusteTila.LUONNOS)
                && findViitteet(perusteId, id).size() == 1) {
            PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
            poistoService.remove(perusteId, perusteenOsa);
            perusteenOsaService.delete(perusteenOsa.getId(), perusteId);
        }
        viite.setPerusteenOsa(null);
        viite.getVanhempi().getLapset().remove(viite);
        viite.setVanhempi(null);
        repository.delete(viite);
    }

    @Override
    @Transactional
    public void reorderSubTree(Long perusteId, Long rootViiteId, PerusteenOsaViiteDto.Puu<?, ?> uusi) {
        Peruste peruste = perusteet.findOne(perusteId);
        PerusteenOsaViite viite = findViite(perusteId, rootViiteId);
        repository.lock(viite.getRoot());
        Set<PerusteenOsaViite> refs = Sets.newIdentityHashSet();
        refs.add(viite);
        clearChildren(viite, refs);
        PerusteenOsaViite parent = viite.getVanhempi();
        updateTraverse(parent, uusi, refs);
        if (viite.getPerusteenOsa() != null) {
            viite.getPerusteenOsa().muokattu();
        }
        muokkausTietoService.addMuokkaustieto(perusteId, peruste, MuokkausTapahtuma.JARJESTETTY);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisaltoJulkaistuun(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto) {
        return addSisaltoImpl(perusteId, viiteId, viiteDto, true);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto) {
        return addSisaltoImpl(perusteId, viiteId, viiteDto, false);
    }

    @Transactional
    private PerusteenOsaViiteDto.Matala addSisaltoImpl(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto, boolean julkaistuun) {
        Peruste peruste = perusteet.findOne(perusteId);
        PerusteenOsaViite viite = repository.findOne(viiteId);

        if (peruste == null || !peruste.containsViite(viite)) {
            throw new BusinessRuleViolationException("Sisältö ei kuulu tähän perusteeseen");
        }

        PerusteenOsaViite uusiViite = new PerusteenOsaViite();
        repository.lock(viite.getRoot());

        uusiViite.setVanhempi(viite);
        List<PerusteenOsaViite> lapset = viite.getLapset();
        if (lapset == null) {
            lapset = new ArrayList<>();
            viite.setLapset(lapset);
        }
        lapset.add(uusiViite);
        uusiViite = repository.save(uusiViite);

        if (viiteDto == null || (viiteDto.getPerusteenOsaRef() == null && viiteDto.getPerusteenOsa() == null)) {
            TekstiKappale uusiKappale = new TekstiKappale();
            uusiKappale = perusteenOsaRepository.save(uusiKappale);
            uusiViite.setPerusteenOsa(uusiKappale);
        } else {
            PerusteenOsaViite viiteEntity = mapper.map(viiteDto, PerusteenOsaViite.class);
            uusiViite.setLapset(viiteEntity.getLapset());

            if (viiteDto.getPerusteenOsaRef() != null) {
                permissionChecker.checkPermission(viiteEntity.getPerusteenOsa().getId(), PermissionManager.Target.PERUSTEENOSA, PermissionManager.Permission.LUKU);
                uusiViite.setPerusteenOsa(viiteEntity.getPerusteenOsa());
            } else if (viiteDto.getPerusteenOsa() != null) {
                if (julkaistuun) {
                    perusteenOsaService.addJulkaistuun(uusiViite, viiteDto.getPerusteenOsa());
                }
                else {
                    perusteenOsaService.add(uusiViite, viiteDto.getPerusteenOsa());
                }
            }
        }
        repository.flush();
        muokkausTietoService.addMuokkaustieto(perusteId, uusiViite, MuokkausTapahtuma.LUONTI);
        return mapper.map(uusiViite, PerusteenOsaViiteDto.Matala.class);
    }

    @Transactional
    private List<PerusteenOsaViite> findViitteet(Long perusteId, Long viiteId) {
        PerusteenOsaViite viite = findViite(perusteId, viiteId);
        List<PerusteenOsaViite> viitteet = repository.findAllByPerusteenOsa(viite.getPerusteenOsa());
        return viitteet;
    }

    @Transactional
    private PerusteenOsaViite findViite(Long perusteId, Long viiteId) {
        Peruste peruste = perusteet.findOne(perusteId);
        PerusteenOsaViite viite = repository.findOne(viiteId);
        if (peruste != null && peruste.containsViite(viite)) {
            return viite;
        }
        throw new BusinessRuleViolationException("virheellinen viite");
    }

    @Transactional
    private void clearChildren(PerusteenOsaViite pov, Set<PerusteenOsaViite> refs) {
        for (PerusteenOsaViite lapsi : pov.getLapset()) {
            refs.add(lapsi);
            clearChildren(lapsi, refs);
        }
        pov.setVanhempi(null);
        pov.getLapset().clear();
    }

    @Transactional
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

    @Override
    public <T extends Sortable> List<T> sort(Long id, Suoritustapakoodi suoritustapakoodi, List<T> sorted) {
        Peruste peruste = perusteet.findOne(id);
        List<TutkinnonOsaViite> viitteet = tutkinnonOsaViiteRepository.findByPeruste(id, suoritustapakoodi);
        muokkausTietoService.addMuokkaustieto(id, peruste, MuokkausTapahtuma.JARJESTETTY);
        viitteet = GenericAlgorithms.sort(sorted, viitteet);
        tutkinnonOsaViiteRepository.save(viitteet);
        return sorted;
    }

}
