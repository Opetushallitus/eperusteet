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

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiaineOppimaaraNodeDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokurssiJulkisetTiedotDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.33
 */
@Service
public class LukiokoulutuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<LukiokoulutuksenPerusteenSisalto>
        implements LukiokoulutuksenPerusteenSisaltoService {
    @Autowired
    private LukiokurssiRepository lukiokurssiRepository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private LokalisointiService lokalisointiService;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository sisaltoRepository;

    @Autowired
    private LukioOpetussuunnitelmaRakenneRepository lukioOpetussuunnitelmaRakenneRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private KurssiService kurssiService;


    @Override
    @Transactional(readOnly = true)
    public <T extends OppiaineBaseDto> List<T> getOppiaineetByRakenneRevision(long perusteId, int revision, Class<T> view) {
        return listOppiaineet(rakenneRevisionOppiaineet(
                getRakenneByPerusteIdAndRakenneRevision(perusteId, revision), revision), view);
    }

    @Override
    @Transactional
    public LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineSuppeaDto> revertukioRakenneByRevision(long perusteId, int revision) {
        LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineSuppeaDto> oldRakenne
                = getLukioRakenneByRevision(perusteId, revision, OppiaineSuppeaDto.class);
        OppaineKurssiTreeStructureDto structure = new OppaineKurssiTreeStructureDto();
        structure.getKurssit().addAll(oldRakenne.getKurssit().stream().map(this::kurssiToStructure).collect(toList()));
        structure.getOppiaineet().addAll(oldRakenne.getOppiaineet().stream().map(oa ->
            new OppiaineJarjestysDto(oa.getId(),
                    // On muuten pirun käteviä ja hyödyllisiä nämä Orikalta nulleina ulos tulevat Optionalit:
                    oa.getOppiaine() == null ? null : oa.getOppiaine().transform(EntityReference::getIdLong).orNull(),
                    oa.getJnro() == null ? null : oa.getJnro().orNull())).collect(toList()));
        kurssiService.updateTreeStructure(perusteId, structure, revision);
        return oldRakenne;
    }

    private LukiokurssiOppaineMuokkausDto kurssiToStructure(LukiokurssiListausDto kurssi) {
        LukiokurssiOppaineMuokkausDto dto = new LukiokurssiOppaineMuokkausDto();
        dto.setId(kurssi.getId());
        dto.getOppiaineet().addAll(kurssi.getOppiaineet().stream().map(oa
                -> new KurssinOppiaineDto(oa.getOppiaineId(), oa.getJarjestys())).collect(toList()));
        return dto;
    }

    protected LukioOpetussuunnitelmaRakenne getRakenneByPerusteIdAndRakenneRevision(long perusteId, int revision) {
        LukiokoulutuksenPerusteenSisalto sisalto = getByPerusteId(perusteId);
        return found(lukioOpetussuunnitelmaRakenneRepository.findRevision(
                    found(sisalto.getOpetussuunnitelma()).getId(), revision),
                () -> new NotExistsException("lukiokoulutuksen-rakennetta-ei-loydy-revisiolla",
                        new HashMap<String, Object>(){{ put("revision", revision); }}));
    }

    @Override
    protected LukiokoulutuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        LukiokoulutuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "sisaltoa-annetulle-perusteelle-ei-ole-olemassa");
        return sisalto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> listRakenneRevisions(long perusteId) {
        return lukioOpetussuunnitelmaRakenneRepository.getRevisions(
                found(getByPerusteId(perusteId).getOpetussuunnitelma()).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public <OppiaineType extends OppiaineBaseDto> LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType>
                getLukioRakenneByRevision(long perusteId, int revision, Class<OppiaineType> oppiaineClz) {
        LukioOpetussuunnitelmaRakenne rakenne = getRakenneByPerusteIdAndRakenneRevision(perusteId, revision);
        LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType> dto = new LukioOpetussuunnitelmaRakenneRevisionDto<>(perusteId, revision);
        dto.getOppiaineet().addAll(listOppiaineet(rakenneRevisionOppiaineet(rakenne, revision), oppiaineClz));
        dto.getKurssit().addAll(kurssiService.findLukiokurssitByRakenneRevision(perusteId, rakenne.getId(), revision));
        return dto;
    }

    private Stream<Oppiaine> rakenneRevisionOppiaineet(LukioOpetussuunnitelmaRakenne rakenne, int revision) {
        return rakenne.getOppiaineet().stream().map(
            oa -> oppiaineRepository.findRevision(oa.getId(), revision)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public <OppiaineType extends OppiaineBaseDto> LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType>
                    getLukioRakenne(long perusteId, Class<OppiaineType> oppiaineClz) {
        LukioOpetussuunnitelmaRakenne rakenne = getByPerusteId(perusteId).getOpetussuunnitelma();
        LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType> dto = new LukioOpetussuunnitelmaRakenneRevisionDto<>(perusteId,
                    lukioOpetussuunnitelmaRakenneRepository.getLatestRevisionId());
        dto.getOppiaineet().addAll(listOppiaineet(rakenne.getOppiaineet().stream(), oppiaineClz));
        dto.getKurssit().addAll(kurssiService.findLukiokurssitByPerusteId(perusteId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public LukioOppiainePuuDto getOppiaineTreeStructure(long perusteId) {
        found(sisaltoRepository.findLukioperusteenTilaByPerusteId(perusteId),
                t -> t != PerusteTila.POISTETTU);
        LukioOppiainePuuDto tree = new LukioOppiainePuuDto(perusteId);
        List<LukiokurssiJulkisetTiedotDto> kurssit = lukiokurssiRepository.findLukiokurssiJulkinenDtosByPerusteId(perusteId);
        List<LukioOppiaineOppimaaraNodeDto> oppiaineet = oppiaineRepository.findLukioOppiaineetJulkinenDtosByPerusteId(perusteId);
        oppiaineet.forEach(lukioOppiaineOppimaaraNodeDto -> lokalisointiService.lokalisoi(lukioOppiaineOppimaaraNodeDto.getTehtava()));
        kurssit.forEach(lukiokurssiJulkisetTiedotDto -> lokalisointiService.lokalisoi(lukiokurssiJulkisetTiedotDto.getTavoitteet()) );

        Map<Long, LukioOppiaineOppimaaraNodeDto> oppiaineById = oppiaineet.stream()
                .collect(toMap(LukioOppiaineOppimaaraNodeDto::getId, n -> n));
        oppiaineet.stream().filter(oa -> oa.getParentId() != null)
                .forEach(oa -> oppiaineById.get(oa.getParentId()).getOppimaarat().add(oa));
        kurssit.stream().forEach(k -> oppiaineById.get(k.getOppiaineId()).getKurssit().add(k));
        oppiaineet.stream().filter(oa -> oa.getParentId() == null).forEach(oa -> tree.getOppiaineet().add(oa));
        return lokalisointiService.lokalisoi(tree);
    }
}
