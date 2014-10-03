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

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import fi.vm.sade.eperusteet.domain.KayttajaprofiiliPreferenssi;
import fi.vm.sade.eperusteet.domain.Suosikki;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaprofiiliPreferenssiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import fi.vm.sade.eperusteet.repository.KayttajaprofiiliPreferenssiRepository;
import fi.vm.sade.eperusteet.repository.KayttajaprofiiliRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.SuosikkiRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class KayttajaprofiiliServiceImpl implements KayttajaprofiiliService {

    @Autowired
    KayttajaprofiiliRepository kayttajaprofiiliRepo;

    @Autowired
    KayttajaprofiiliPreferenssiRepository kayttajaprofiiliPreferenssiRepo;

    @Autowired
    PerusteRepository perusteRepo;

    @Autowired
    SuosikkiRepository suosikkiRepo;

    @Autowired
    PerusteprojektiRepository perusteprojektiRepo;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public KayttajaProfiiliDto get() {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        return mapper.map(kayttajaprofiiliRepo.findOneEager(oid), KayttajaProfiiliDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Kayttajaprofiili createOrGet() {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        Kayttajaprofiili kayttajaprofiili = kayttajaprofiiliRepo.findOneByOid(oid);
        if (kayttajaprofiili == null) {
            kayttajaprofiili = new Kayttajaprofiili();
            kayttajaprofiili.setOid(oid);
            kayttajaprofiili.setSuosikit(new ArrayList<Suosikki>());
            kayttajaprofiili.setPreferenssit(new ArrayList<KayttajaprofiiliPreferenssi>());
            kayttajaprofiili = kayttajaprofiiliRepo.save(kayttajaprofiili);
        }
        return kayttajaprofiili;
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KayttajaProfiiliDto setPreference(KayttajaprofiiliPreferenssiDto uusi) {
        Kayttajaprofiili kayttajaprofiili = createOrGet();
        KayttajaprofiiliPreferenssi preferenssi;
        preferenssi = kayttajaprofiiliPreferenssiRepo.findOneByKayttajaprofiiliAndAvain(kayttajaprofiili, uusi.getAvain());

        if (preferenssi == null) {
            kayttajaprofiiliPreferenssiRepo.save(new KayttajaprofiiliPreferenssi(kayttajaprofiili, uusi.getAvain(), uusi.getArvo()));
        }
        else {
            preferenssi.setArvo(uusi.getArvo());
            kayttajaprofiiliPreferenssiRepo.save(preferenssi);
        }

        return mapper.map(kayttajaprofiili, KayttajaProfiiliDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KayttajaProfiiliDto addSuosikki(final SuosikkiDto suosikkiDto) {
        Kayttajaprofiili kayttajaprofiili = createOrGet();
        Suosikki suosikki = new Suosikki();
        suosikki.setKayttajaprofiili(kayttajaprofiili);
        suosikki.setSisalto(suosikkiDto.getSisalto());
        suosikki.setLisatty(new Date());
        suosikki.setNimi(suosikkiDto.getNimi());
        suosikki = suosikkiRepo.save(suosikki);
        kayttajaprofiili.getSuosikit().add(suosikki);
        return mapper.map(kayttajaprofiili, KayttajaProfiiliDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KayttajaProfiiliDto deleteSuosikki(Long suosikkiId) throws IllegalArgumentException {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        Kayttajaprofiili kayttajaprofiili = kayttajaprofiiliRepo.findOneEager(oid);

        if (kayttajaprofiili != null) {
            Suosikki suosikki = suosikkiRepo.findOne(suosikkiId);
            kayttajaprofiili.getSuosikit().remove(suosikki);
        }

        return mapper.map(kayttajaprofiili, KayttajaProfiiliDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KayttajaProfiiliDto updateSuosikki(Long suosikkiId, SuosikkiDto suosikkiDto) throws IllegalArgumentException {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        Kayttajaprofiili kayttajaprofiili = kayttajaprofiiliRepo.findOneEager(oid);

        if (kayttajaprofiili != null) {
            Suosikki suosikki = suosikkiRepo.findOne(suosikkiId);
            suosikki.setNimi(suosikkiDto.getNimi());
        }

        return mapper.map(kayttajaprofiili, KayttajaProfiiliDto.class);
    }
}
