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

import fi.vm.sade.eperusteet.domain.OpasSisalto;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.LAADINTA;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.OpasService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Service
public class OpasServiceImpl implements OpasService {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public OpasDto get(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public OpasDto save(OpasLuontiDto opasDto) {
        Perusteprojekti perusteprojekti = mapper.map(opasDto, Perusteprojekti.class);
        if (opasDto.getRyhmaOid() == null) {
            throw new BusinessRuleViolationException("Opastyöryhmää ei ole asetettu");
        }

        perusteprojekti.setTila(LAADINTA);
        perusteprojekti.setRyhmaOid(opasDto.getRyhmaOid());

        Peruste peruste = new Peruste();
        peruste.setTyyppi(PerusteTyyppi.OPAS);

        peruste.setSisalto(new OpasSisalto());

        perusteRepository.save(peruste);

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.saveAndFlush(perusteprojekti);

        return mapper.map(perusteprojekti, OpasDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> findBy(PageRequest page, PerusteQuery pquery) {
        pquery.setTila(PerusteTila.VALMIS.toString());
        pquery.setPerusteTyyppi(PerusteTyyppi.OPAS.toString());
        Page<Peruste> result = perusteet.findBy(page, pquery);
        PageDto<Peruste, PerusteHakuDto> resultDto = new PageDto<>(result, PerusteHakuDto.class, page, mapper);
        return resultDto;
    }

    @Override
    public Page<PerusteprojektiKevytDto> findProjektiBy(PageRequest p, PerusteprojektiQueryDto pquery) {
        pquery.setTyyppi(PerusteTyyppi.OPAS);
        Page<Perusteprojekti> projektit = repository.findBy(p, pquery);
        Page<PerusteprojektiKevytDto> result = projektit.map(pp -> {
            PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
            return ppk;
        });
        return result;
    }

}
