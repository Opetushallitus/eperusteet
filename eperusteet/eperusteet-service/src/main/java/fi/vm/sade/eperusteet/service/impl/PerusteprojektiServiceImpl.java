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
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
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

}
