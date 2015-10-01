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
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OppiaineLukiokurssi;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.LukiokurssiRepository;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.vm.sade.eperusteet.domain.yl.Oppiaine.inLukioPeruste;
import static fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi.inPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.03
 */
@Service
public class KurssiServiceImpl implements KurssiService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukioSisaltoRepository;

    @Autowired
    private LukiokurssiRepository lukiokurssiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LukiokurssiListausDto> findLukiokurssitByPerusteId(long perusteId, Kieli kieli) {
        List<LukiokurssiListausDto> kurssit = lukiokurssiRepository.findLukiokurssitByPerusteId(perusteId, kieli);
        Map<Long,LukiokurssiListausDto> kurssitById = kurssit.stream()
                .collect(toMap(LukiokurssiListausDto::getId, k -> k));
        lukiokurssiRepository.findKurssiOppaineRelationsByPerusteId(perusteId).stream()
                .forEachOrdered(oak -> kurssitById.get(oak.getKurssiId()).getOppiaineet().add(
                        new JarjestettyOppiaineDto(oak.getOppiaineId(), oak.getJarjestys())));
        return kurssit;
    }

    @Override
    @Transactional(readOnly = true)
    public LukiokurssiMuokkausDto getLukiokurssiMuokkausById(long perusteId, long kurssiId) throws NotExistsException {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        LukiokurssiMuokkausDto dto = mapper.map(kurssi, new LukiokurssiMuokkausDto());
        dto.setOppiaineet(kurssi.getOppiaineet().stream()
                .map(luoa -> new JarjestettyOppiaineDto(luoa.getOppiaine().getId(), luoa.getJarjestys()))
                .sorted(comparing(JarjestettyOppiaineDto::getOppiaineId)).collect(toList()));
        return dto;
    }

    @Override
    @Transactional
    public long luoLukiokurssi(long perusteId, LukioKurssiLuontiDto kurssiDto) throws BusinessRuleViolationException {
        LukiokoulutuksenPerusteenSisalto sisalto = found(lukioSisaltoRepository.findByPerusteId(perusteId),
                () -> new BusinessRuleViolationException("Perustetta ei ole."));
        lukioSisaltoRepository.lock(sisalto, false);

        Lukiokurssi kurssi = mapper.map(kurssiDto, new Lukiokurssi());
        kurssi.setPerusteenSisalto(sisalto);
        lukiokurssiRepository.saveAndFlush(kurssi);
        mergeOppiaineet(perusteId, kurssi, kurssiDto.getOppiaineet());
        return kurssi.getId();
    }

    private void mergeOppiaineet(long perusteId, Lukiokurssi kurssi, List<JarjestettyOppiaineDto> from) {
        Set<OppiaineLukiokurssi> to = kurssi.getOppiaineet();
        Map<Long, JarjestettyOppiaineDto> newByOppiaine = from.stream().collect(toMap(JarjestettyOppiaineDto::getOppiaineId, olk -> olk));
        to.removeIf(olk -> !newByOppiaine.containsKey(olk.getOppiaine().getId()));
        to.stream().forEach(existing -> {
            Long oppiaineId = existing.getOppiaine().getId();
            existing.setJarjestys(newByOppiaine.get(oppiaineId).getJarjestys());
            newByOppiaine.remove(oppiaineId);
        });
        newByOppiaine.values().stream().forEach(newOppiaine -> {
            OppiaineLukiokurssi oaKurssi = new OppiaineLukiokurssi();
            oaKurssi.setKurssi(kurssi);
            oaKurssi.setOppiaine(found(oppiaineRepository.findOne(newOppiaine.getOppiaineId()), inLukioPeruste(perusteId)));
            oaKurssi.setJarjestys(newOppiaine.getJarjestys());
            to.add(oaKurssi);
        });
    }

    @Override
    @Transactional
    public void muokkaaLukiokurssia(long perusteId, LukiokurssiMuokkausDto muokkausDto) throws NotExistsException {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(muokkausDto.getId()), inPeruste(perusteId));
        lukiokurssiRepository.lock(kurssi, false);
        mapper.map(muokkausDto, kurssi);
        mergeOppiaineet(perusteId, kurssi, muokkausDto.getOppiaineet());
    }

}
