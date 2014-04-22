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

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.SuoritustapaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.HashSet;
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
    private KoulutusRepository koulutusRepository;
    
    @Autowired
    private SuoritustapaService suoritustapaService;

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto save(PerusteprojektiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti = checkIfKoulutuksetAlreadyExists(perusteprojekti);
        Set<Suoritustapa> temp = new HashSet<>();
        for (Suoritustapa suoritustapa : perusteprojekti.getPeruste().getSuoritustavat()) {
            temp.add(suoritustapaService.createSuoritustapaWithSisaltoRoot(suoritustapa.getSuoritustapakoodi()));
        }
        perusteprojekti.getPeruste().setSuoritustavat(temp);
        perusteprojekti = repository.save(perusteprojekti);
        kayttajaprofiiliService.addPerusteprojekti(perusteprojekti.getId());
        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        if (!repository.exists(id)) {
            throw new EntityNotFoundException("Objektia ei löytynyt id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti = checkIfKoulutuksetAlreadyExists(perusteprojekti);
        perusteprojekti = repository.save(perusteprojekti);
        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }
    
    private Perusteprojekti checkIfKoulutuksetAlreadyExists(Perusteprojekti projekti) {
             
        Set<Koulutus> koulutukset = new HashSet<>();
        Koulutus koulutusTemp;
        
        if (projekti != null && projekti.getPeruste() != null && projekti.getPeruste().getKoulutukset() != null) {
            for (Koulutus koulutus : projekti.getPeruste().getKoulutukset()) {
                koulutusTemp = koulutusRepository.findOneByKoulutuskoodi(koulutus.getKoulutuskoodi());
                if (koulutusTemp != null) {
                    koulutukset.add(koulutusTemp);
                } else {
                    koulutukset.add(koulutus);
                }
            }
            projekti.getPeruste().setKoulutukset(koulutukset);
        }
        return projekti;
    }
}
