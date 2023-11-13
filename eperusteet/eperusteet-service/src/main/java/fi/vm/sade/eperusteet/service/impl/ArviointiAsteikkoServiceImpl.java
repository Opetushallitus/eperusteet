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

import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.repository.ArviointiAsteikkoRepository;
import fi.vm.sade.eperusteet.repository.OsaamistasoRepository;
import fi.vm.sade.eperusteet.service.ArviointiAsteikkoService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jhyoty
 */
@Service
@Transactional
public class ArviointiAsteikkoServiceImpl implements ArviointiAsteikkoService {

    @Autowired
    private ArviointiAsteikkoRepository repository;

    @Autowired
    private OsaamistasoRepository osaamistasoRepository;

    @Autowired
    private ArviointiAsteikkoService self;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ArviointiAsteikkoDto> getAll() {
        return mapper.mapAsList(repository.findAll(), ArviointiAsteikkoDto.class);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(readOnly = true)
    public ArviointiAsteikkoDto get(Long id) {
        return mapper.map(repository.findById(id).orElse(null), ArviointiAsteikkoDto.class);
    }

    @Override
    public ArviointiAsteikkoDto update(ArviointiAsteikkoDto arviointiAsteikkoDto) {

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        if (arviointiAsteikkoDto.getId() != null) {
            arviointiasteikko = repository.findById(arviointiAsteikkoDto.getId()).orElse(null);
            if (arviointiasteikko == null) {
                throw new BusinessRuleViolationException("arviointiasteikko-ei-olemassa");
            }

            List<Long> osaamistasoIds = arviointiAsteikkoDto.getOsaamistasot().stream().map(OsaamistasoDto::getId).collect(Collectors.toList());
            arviointiasteikko.getOsaamistasot().stream()
                    .filter(osaamistaso -> !osaamistasoIds.contains(osaamistaso.getId()))
                    .forEach(osaamistaso -> osaamistasoRepository.deleteById(osaamistaso.getId()));
        }

        mapper.map(arviointiAsteikkoDto, arviointiasteikko);
        arviointiasteikko = repository.save(arviointiasteikko);

        return mapper.map(arviointiasteikko, ArviointiAsteikkoDto.class);
    }

    @Override
    public ArviointiAsteikkoDto insert(ArviointiAsteikkoDto arviointiAsteikkoDto) {
        return update(arviointiAsteikkoDto);
    }

    @Override
    public void delete(ArviointiAsteikkoDto arviointiAsteikkoDto) {
        arviointiAsteikkoDto.getOsaamistasot().forEach(osaamistaso -> osaamistasoRepository.deleteById(osaamistaso.getId()));
        repository.deleteById(arviointiAsteikkoDto.getId());
    }

    @Override
    public List<ArviointiAsteikkoDto> update(List<ArviointiAsteikkoDto> arviointiAsteikkoDtos) {

        arviointiAsteikkoDtos.forEach(arviointiAsteikkoDto -> arviointiAsteikkoDto.getOsaamistasot().forEach(osaamistaso -> {
            if (osaamistaso.getKoodi() == null) {
                throw new BusinessRuleViolationException("osaamistaso-koodi-puuttuu");
            }
        }));

        List<ArviointiAsteikko> arviointiasteikot = repository.findAll();

        arviointiasteikot.forEach(arviointiasteikko -> {
            if (!arviointiAsteikkoDtos.stream().map(ArviointiAsteikkoDto::getId).collect(Collectors.toList()).contains(arviointiasteikko.getId())) {
                self.delete(mapper.map(arviointiasteikko, ArviointiAsteikkoDto.class));
            }
        });

        return arviointiAsteikkoDtos.stream().map(arviointiAsteikkoDto -> {
            if (arviointiAsteikkoDto.getId() != null) {
                return update(arviointiAsteikkoDto);
            }

            return self.insert(arviointiAsteikkoDto);
        }).collect(Collectors.toList());
    }

    @Override
    public void remove(Long id) {
        repository.deleteById(id);
    }
}
