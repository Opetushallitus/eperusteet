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

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteprojektiServiceImpl.class);

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private KayttajaprofiiliService kayttajaprofiiliService;

    @Autowired
    private PerusteService perusteService;

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        String koulutustyyppi = perusteprojektiDto.getKoulutustyyppi();
        LaajuusYksikko yksikko = perusteprojektiDto.getYksikko();

        if (koulutustyyppi.equals("koulutustyyppi_1") && yksikko == null) {
            throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
        }

        Peruste peruste = perusteService.luoPerusteRunko(perusteprojektiDto.getKoulutustyyppi(), perusteprojektiDto.getYksikko());
        perusteprojekti.setPeruste(peruste);
        perusteprojekti.setTila(Tila.LAADINTA);
        perusteprojekti = repository.save(perusteprojekti);
        kayttajaprofiiliService.addPerusteprojekti(perusteprojekti.getId());

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        if (!repository.exists(id)) {
            throw new EntityNotFoundException("Objektia ei löytynyt id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti = repository.save(perusteprojekti);
        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Tila> getTilat(Long id) {
        Perusteprojekti p = repository.findOne(id);
        if (p == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }
        
        return p.getTila().mahdollisetTilat();
    }

    @Override
    @Transactional(readOnly = false)
    public TilaUpdateStatus updateTila(Long id, Tila tila) {
        TilaUpdateStatus status = new TilaUpdateStatus();
        status.setVaihtoOk(true);
        
        Perusteprojekti projekti = repository.findOne(id);
        
        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }
        status.setVaihtoOk(projekti.getTila().mahdollisetTilat().contains(tila));
        if ( !status.isVaihtoOk() ) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '" + tila.toString() + "' ei mahdollinen";
            status.addStatus(viesti, TilaUpdateStatus.Statuskoodi.VIRHE);
            return status;
        }

        if (projekti.getPeruste() != null && projekti.getPeruste().getSuoritustavat() != null
            && tila == Tila.VIIMEISTELY && projekti.getTila() == Tila.LAADINTA) {
            /*for (Suoritustapa suoritustapa : projekti.getPeruste().getSuoritustavat()) {
                Validointi validointi;
                if (suoritustapa.getRakenne() != null) {
                    validointi = PerusteenRakenne.validoiRyhma(suoritustapa.getRakenne());
                    if (!validointi.ongelmat.isEmpty()) {
                        status.addStatus("Rakenteen validointi virhe", TilaUpdateStatus.Statuskoodi.VIRHE, validointi);
                        status.setVaihtoOk(false);
                    }
                }
            } */    
        }
        
        if ( !status.isVaihtoOk() ) {
            return status;
        }
        
        if (tila == Tila.JULKAISTU && projekti.getTila() == Tila.VALMIS) {
            for (Suoritustapa suoritustapa : projekti.getPeruste().getSuoritustavat()) {
                setSisaltoValmis(suoritustapa.getSisalto());
                for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                    setOsatValmis(tutkinnonosaViite);  
                }
            }
            projekti.getPeruste().setTila(Tila.VALMIS);
        }

        projekti.setTila(tila);
        repository.save(projekti);   
        return status;
    }
    
    private PerusteenOsaViite setSisaltoValmis(PerusteenOsaViite sisaltoRoot) {
        if (sisaltoRoot.getPerusteenOsa() != null) {
            sisaltoRoot.getPerusteenOsa().setTila(Tila.VALMIS);
        }
        if (sisaltoRoot.getLapset() != null) {
            for (PerusteenOsaViite lapsi : sisaltoRoot.getLapset()) {
               setSisaltoValmis(lapsi);    
            }
        }
        return sisaltoRoot;
    }
    
    private TutkinnonOsaViite setOsatValmis(TutkinnonOsaViite osa) {
        if (osa.getTutkinnonOsa()!= null) {
            osa.getTutkinnonOsa().setTila(Tila.VALMIS);
        }
        return osa;
    }
    

}
