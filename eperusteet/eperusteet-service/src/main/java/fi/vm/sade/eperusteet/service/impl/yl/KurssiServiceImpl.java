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

import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OppiaineLukiokurssi;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.LukiokurssiRepository;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
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

    @Autowired
    private LokalisointiService lokalisointiService;

    @Autowired
    private OppiaineService oppiaineService;


    @Override
    @Transactional(readOnly = true)
    public List<LukiokurssiListausDto> findLukiokurssitByPerusteId(long perusteId) {
        List<LukiokurssiListausDto> kurssit = lukiokurssiRepository.findLukiokurssitByPerusteId(perusteId);
        return lokalisointiService.lokalisoi(kurssitWithOppiaineet(perusteId, kurssit));
    }

    private List<LukiokurssiListausDto> kurssitWithOppiaineet(long perusteId, List<LukiokurssiListausDto> kurssit) {
        Map<Long,LukiokurssiListausDto> kurssitById = kurssit.stream()
                .collect(toMap(LukiokurssiListausDto::getId, k -> k));
        lukiokurssiRepository.findKurssiOppaineRelationsByPerusteId(perusteId).stream()
                .forEachOrdered(oak -> kurssitById.get(oak.getKurssiId()).getOppiaineet().add(
                        new KurssinOppiaineNimettyDto(oak.getOppiaineId(), oak.getJarjestys(),
                                oak.getOppiaineNimiId())));
        return kurssit;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LukiokurssiListausDto> findLukiokurssitByOppiaineId(long perusteId, long oppiaineId) {
        List<LukiokurssiListausDto> kurssit = lukiokurssiRepository.findLukiokurssitByPerusteAndOppiaineId(perusteId, oppiaineId);
        return lokalisointiService.lokalisoi(kurssit);
    }

    @Override
    @Transactional(readOnly = true)
    public LukiokurssiTarkasteleDto getLukiokurssiTarkasteleDtoById(long perusteId, long kurssiId) throws NotExistsException {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        LukiokurssiTarkasteleDto dto = mapper.map(kurssi, new LukiokurssiTarkasteleDto());
        dto.setOppiaineet(kurssi.getOppiaineet().stream().map(this::oppaineTarkasteluDto)
                .sorted(comparing(KurssinOppiaineDto::getOppiaineId)).collect(toList()));
        return lokalisointiService.lokalisoi(dto);
    }

    private KurssinOppiaineTarkasteluDto oppaineTarkasteluDto(OppiaineLukiokurssi oa) {
        return new KurssinOppiaineTarkasteluDto(oa.getOppiaine().getId(), oa.getJarjestys(),
                oa.getOppiaine().getNimi().getId(), vanhempi(oa.getOppiaine()));
    }

    private OppiaineVanhempiDto vanhempi(Oppiaine oppiaine) {
        return oppiaine != null ? new OppiaineVanhempiDto(oppiaine.getId(), oppiaine.getNimi().getId(),
                vanhempi(oppiaine.getOppiaine())) : null;
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

    private<T extends KurssinOppiaineDto> void mergeOppiaineet(long perusteId, Lukiokurssi kurssi, List<T> from) {
        Set<OppiaineLukiokurssi> to = kurssi.getOppiaineet();
        Map<Long, T> newByOppiaine = from.stream().collect(toMap(KurssinOppiaineDto::getOppiaineId, olk -> olk));
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

    @Override
    @Transactional
    public void muokkaaLukiokurssinOppiaineliitoksia(long perusteId, LukiokurssiOppaineMuokkausDto muokkausDto)
            throws NotExistsException {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(muokkausDto.getId()), inPeruste(perusteId));
        lukiokurssiRepository.lock(kurssi, false);
        mergeOppiaineet(perusteId, kurssi, muokkausDto.getOppiaineet());
    }

    @Override
    @Transactional
    public void poistaLukiokurssi(long perusteId, long kurssiId) {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        lukiokurssiRepository.delete(kurssi);
    }

    @Override
    @Transactional
    public void updateTreeStructure(long perusteId, OppaineKurssiTreeStructureDto structure) {
        oppiaineService.jarjestaLukioOppiaineet(perusteId, structure.getOppiaineet());
        structure.getKurssit().forEach(kurssi -> muokkaaLukiokurssinOppiaineliitoksia(perusteId, kurssi));
    }
}
