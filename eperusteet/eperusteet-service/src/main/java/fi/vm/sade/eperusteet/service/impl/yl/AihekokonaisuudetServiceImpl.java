/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusMuokkausDto;
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuudetRepository;
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuudetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

/**
 * User: jsikio
 */
@Service
public class AihekokonaisuudetServiceImpl implements AihekokonaisuudetService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukioSisaltoRepository;

    @Autowired
    private LukioAihekokonaisuudetRepository lukioAihekokonaisuudetRepository;

    @Autowired
    private LukioAihekokonaisuusRepository lukioAihekokonaisuusRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private LokalisointiService lokalisointiService;


    @Override
    @Transactional( readOnly = true)
    public List<AihekokonaisuusListausDto> getAihekokonaisuudet(Long perusteId) {
        return lokalisointiService.lokalisoi(
                lukioAihekokonaisuusRepository.findAihekokonaisuudetByPerusteId(perusteId));
    }


    @Override
    @Transactional( readOnly = true )
    public AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvaus(Long perusteId) {
        Peruste peruste = perusteet.getOne(perusteId);
        Aihekokonaisuudet aihekokonaisuudet = peruste.getLukiokoulutuksenPerusteenSisalto().getAihekokonaisuudet();
        if( aihekokonaisuudet != null ) {
            return mapper.map(peruste.getLukiokoulutuksenPerusteenSisalto().getAihekokonaisuudet(), AihekokonaisuudetYleiskuvausDto.class);
        } else {
            return new AihekokonaisuudetYleiskuvausDto();
        }
    }

    @Override
    @Transactional( readOnly = true )
    public LukioAihekokonaisuusMuokkausDto getLukioAihekokobaisuusMuokkausById(long perusteId, long aihekokonaisuusId) throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository.findOne(aihekokonaisuusId), Aihekokonaisuus.inPeruste(perusteId));
        LukioAihekokonaisuusMuokkausDto dto = mapper.map(aihekokonaisuus, new LukioAihekokonaisuusMuokkausDto());
        return dto;
    }

    @Override
    @Transactional
    public long luoAihekokonaisuus(long perusteId, LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) throws BusinessRuleViolationException {

        LukiokoulutuksenPerusteenSisalto sisalto = found(lukioSisaltoRepository.findByPerusteId(perusteId),
                () -> new BusinessRuleViolationException("Perustetta ei ole."));
        lukioSisaltoRepository.lock(sisalto, false);

        Aihekokonaisuudet aihekokonaisuudet = sisalto.getAihekokonaisuudet();
        if( aihekokonaisuudet == null ) {
            aihekokonaisuudet = initAihekokonaisuudet(sisalto);
            lukioAihekokonaisuudetRepository.saveAndFlush(aihekokonaisuudet);
        }

        Aihekokonaisuus aihekokonaisuus = mapper.map(aihekokonaisuusLuontiDto, new Aihekokonaisuus());
        aihekokonaisuus.setAihekokonaisuudet(aihekokonaisuudet);
        lukioAihekokonaisuusRepository.saveAndFlush(aihekokonaisuus);
        return aihekokonaisuus.getId();
    }

    @Override
    @SuppressWarnings({"TransactionalAnnotations", "ServiceMethodEntity"})
    public Aihekokonaisuudet initAihekokonaisuudet(LukiokoulutuksenPerusteenSisalto sisalto) {
        Aihekokonaisuudet aihekokonaisuudet;
        aihekokonaisuudet = new Aihekokonaisuudet();
        //Asetetaan oletusotsikko
        HashMap<Kieli, String> hm = new HashMap<>();
        hm.put(Kieli.FI, "Aihekokonaisuudet");
        aihekokonaisuudet.setOtsikko(TekstiPalanen.of(hm));
        aihekokonaisuudet.setSisalto(sisalto);
        aihekokonaisuudet.setNimi(TekstiPalanen.of(Kieli.FI, "Aihekokonaisuudet"));
        aihekokonaisuudet.setTunniste(PerusteenOsaTunniste.NORMAALI);
        aihekokonaisuudet.getViite().setPerusteenOsa(aihekokonaisuudet);
        aihekokonaisuudet.getViite().setVanhempi(sisalto.getSisalto());
        sisalto.getSisalto().getLapset().add(aihekokonaisuudet.getViite());
        sisalto.setAihekokonaisuudet(aihekokonaisuudet);
        return aihekokonaisuudet;
    }

    @Override
    @Transactional
    public void muokkaaAihekokonaisuutta(long perusteId, LukioAihekokonaisuusMuokkausDto lukioAihekokonaisuusMuokkausDto) throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository.findOne(lukioAihekokonaisuusMuokkausDto.getId()), Aihekokonaisuus.inPeruste(perusteId));
        lukioAihekokonaisuusRepository.lock(aihekokonaisuus, false);
        mapper.map(lukioAihekokonaisuusMuokkausDto, aihekokonaisuus);
    }

    @Override
    @Transactional
    public void tallennaYleiskuvaus(Long perusteId, AihekokonaisuudetYleiskuvausDto aihekokonaisuudetYleiskuvausDto) {
        Peruste peruste = perusteet.getOne(perusteId);
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        Aihekokonaisuudet aihekokonaisuudet = sisalto.getAihekokonaisuudet();
        if (aihekokonaisuudet == null) {
            aihekokonaisuudet = initAihekokonaisuudet(sisalto);
        }
        mapper.map(aihekokonaisuudetYleiskuvausDto, aihekokonaisuudet);

        // Uusi, tallennetaan.
        if (aihekokonaisuudet.getId() == null) {
            aihekokonaisuudet.setSisalto(peruste.getLukiokoulutuksenPerusteenSisalto());
            peruste.getLukiokoulutuksenPerusteenSisalto().setAihekokonaisuudet(aihekokonaisuudet);
            lukioAihekokonaisuudetRepository.saveAndFlush(aihekokonaisuudet);
        }
    }

    @Override
    @Transactional
    public void poistaAihekokonaisuus(long perusteId, long aihekokonaisuusId) throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository.findOne(aihekokonaisuusId), Aihekokonaisuus.inPeruste(perusteId));
        lukioAihekokonaisuusRepository.delete(aihekokonaisuus);
    }
}
