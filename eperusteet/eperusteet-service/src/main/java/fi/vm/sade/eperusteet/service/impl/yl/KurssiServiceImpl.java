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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OppiaineLukiokurssi;
import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.repository.LukioOpetussuunnitelmaRakenneRepository;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.LukiokurssiRepository;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.KurssiLockContext;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.LukioOpetussuunnitelmaRakenneLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static fi.vm.sade.eperusteet.domain.yl.Oppiaine.inLukioPeruste;
import static fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi.inPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.03
 */
@Service
public class KurssiServiceImpl implements KurssiService {
    private static final Logger logger = LoggerFactory.getLogger(KurssiServiceImpl.class);

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

    @Autowired
    @LockCtx(KurssiLockContext.class)
    private LockService<KurssiLockContext> lukioKurssiLockService;

    @Autowired
    @LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
    private LockService<LukioOpetussuunnitelmaRakenneLockContext> lukioRakenneLockService;
    
    @Autowired
    private LukioOpetussuunnitelmaRakenneRepository rakenneRepository;

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
    public List<LukiokurssiListausDto> findLukiokurssitByRakenneRevision(long perusteId, long rakenneId, int revision) {
        LukioOpetussuunnitelmaRakenne rakenne = found(rakenneRepository.findRevision(rakenneId, revision),
                LukioOpetussuunnitelmaRakenne.inPeruste(perusteId));
        List<LukiokurssiListausDto> kurssit = rakenne.getKurssit().stream()
                .sorted(comparing(k -> fromNullable(k.getKoodiArvo()).or("")))
                .map(kurssi -> withOppiaineet(kurssi, new LukiokurssiListausDto(kurssi.getId(), kurssi.getTyyppi(),
                        kurssi.getKoodiArvo(), kurssi.getNimi().getId(),
                        fromNullable(kurssi.getKuvaus()).transform(TekstiPalanen::getId).orNull(),
                        kurssi.getMuokattu()))).collect(toList());
        return lokalisointiService.lokalisoi(kurssit);
    }

    private LukiokurssiListausDto withOppiaineet(Lukiokurssi kurssi, LukiokurssiListausDto dto) {
        dto.getOppiaineet().addAll(kurssi.getOppiaineet().stream()
                .sorted(comparing(OppiaineLukiokurssi::getJarjestys))
                .map(oa -> new KurssinOppiaineNimettyDto(oa.getOppiaine().getId(), oa.getJarjestys(),
                        oa.getOppiaine().getNimi().getId()))
                .collect(toList()));
        return dto;
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
        return toTarkasteluDto(found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId)));
    }

    private LukiokurssiTarkasteleDto toTarkasteluDto(Lukiokurssi kurssi) {
        return toTarkasteluDto(kurssi, kurssi);
    }
    private LukiokurssiTarkasteleDto toTarkasteluDto(Lukiokurssi kurssi, Lukiokurssi relationsKurssi) {
        LukiokurssiTarkasteleDto dto = mapper.map(kurssi, new LukiokurssiTarkasteleDto());
        dto.setOppiaineet(relationsKurssi.getOppiaineet().stream().map(this::oppaineTarkasteluDto)
                .sorted(comparing(KurssinOppiaineDto::getOppiaineId)).collect(toList()));
        return lokalisointiService.lokalisoi(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public LukiokurssiTarkasteleDto getLukiokurssiTarkasteleDtoByIdAndVersion(long perusteId,
                                                  long kurssiId, int version) throws NotExistsException {
        Lukiokurssi currentKurssi = found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        return toTarkasteluDto(found(lukiokurssiRepository.findRevision(kurssiId, version)), currentKurssi);
    }

    @Override
    @Transactional
    public LukiokurssiTarkasteleDto revertLukiokurssiTarkasteleDtoByIdAndVersion(long perusteId,
                                                                 long kurssiId, int version) {
        found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        Lukiokurssi kurssi = lukiokurssiRepository.findRevision(kurssiId, version);
        LukiokurssiMuokkausDto dto = mapper.map(kurssi, new LukiokurssiMuokkausDto());
        updateLukiokurssi(perusteId, dto);
        return getLukiokurssiTarkasteleDtoById(perusteId, kurssiId);
    }

    private KurssinOppiaineTarkasteluDto oppaineTarkasteluDto(OppiaineLukiokurssi oa) {
        return new KurssinOppiaineTarkasteluDto(oa.getOppiaine().getId(), oa.getJarjestys(),
                oa.getOppiaine().getNimi().getId(), parent(oa.getOppiaine()),
                kurssiTyypinKuvaus(oa));
    }

    private Long kurssiTyypinKuvaus(OppiaineLukiokurssi oa) {
        Oppiaine oppiaine = oa.getOppiaine();
        switch (oa.getKurssi().getTyyppi()) {
            case PAKOLLINEN: return id(oppiaine.getPakollinenKurssiKuvaus());
            case VALTAKUNNALLINEN_SOVELTAVA: return id(oppiaine.getSoveltavaKurssiKuvaus());
            case VALTAKUNNALLINEN_SYVENTAVA: return id(oppiaine.getSyventavaKurssiKuvaus());
            default:return null;
        }
    }

    private Long id(TekstiPalanen palanen) {
        return fromNullable(palanen).transform(TekstiPalanen::getId).orNull();
    }

    private OppiaineVanhempiDto parent(Oppiaine oppiaine) {
        return oppiaine != null ? new OppiaineVanhempiDto(oppiaine.getId(), oppiaine.getNimi().getId(),
                parent(oppiaine.getOppiaine())) : null;
    }

    @Override
    @Transactional
    public long createLukiokurssi(long perusteId, LukioKurssiLuontiDto kurssiDto) throws BusinessRuleViolationException {
        LukiokoulutuksenPerusteenSisalto sisalto = found(lukioSisaltoRepository.findByPerusteId(perusteId),
                () -> new BusinessRuleViolationException("Perustetta ei ole."));
        lukioSisaltoRepository.lock(sisalto, false);

        Lukiokurssi kurssi = mapper.map(kurssiDto, new Lukiokurssi());
        kurssi.setOpetussuunnitelma(sisalto.getOpetussuunnitelma());
        lukiokurssiRepository.saveAndFlush(kurssi);
        mergeOppiaineet(perusteId, kurssi, kurssiDto.getOppiaineet());
        lukiokurssiRepository.setRevisioKommentti(kurssiDto.getKommentti());
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
    public void updateLukiokurssi(long perusteId, LukiokurssiMuokkausDto muokkausDto) throws NotExistsException {
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(muokkausDto.getId()), inPeruste(perusteId));
        lukioKurssiLockService.assertLock(new KurssiLockContext(perusteId, kurssi.getId()));
        lukiokurssiRepository.lock(kurssi, false);
        mapper.map(muokkausDto, kurssi);
        lukiokurssiRepository.setRevisioKommentti(muokkausDto.getKommentti());
    }

    @Override
    @Transactional
    public void updateLukiokurssiOppiaineRelations(long perusteId, LukiokurssiOppaineMuokkausDto muokkausDto)
            throws NotExistsException {
        lukioRakenneLockService.assertLock(new LukioOpetussuunnitelmaRakenneLockContext(perusteId));
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(muokkausDto.getId()), inPeruste(perusteId));
        lukiokurssiRepository.lock(kurssi, false);
        mergeOppiaineet(perusteId, kurssi, muokkausDto.getOppiaineet());
    }

    @Override
    @Transactional
    public void deleteLukiokurssi(long perusteId, long kurssiId) {
        lukioRakenneLockService.assertLock(new LukioOpetussuunnitelmaRakenneLockContext(perusteId));
        lukioKurssiLockService.assertLock(new KurssiLockContext(perusteId, kurssiId));
        Lukiokurssi kurssi = found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        lukiokurssiRepository.lock(kurssi, false);
        lukiokurssiRepository.delete(kurssi);
    }

    @Override
    @Transactional
    public void updateTreeStructure(long perusteId, OppaineKurssiTreeStructureDto structure, Integer tryRestoreFromRevision) {
        lukioRakenneLockService.assertLock(new LukioOpetussuunnitelmaRakenneLockContext(perusteId));
        LukiokoulutuksenPerusteenSisalto sisalto = lukioSisaltoRepository.findByPerusteId(perusteId);
        LukioOpetussuunnitelmaRakenne rakenne = sisalto.getOpetussuunnitelma();
        rakenneRepository.lock(rakenne);
        oppiaineService.reArrangeLukioOppiaineet(perusteId, structure.getOppiaineet(), tryRestoreFromRevision);
        Map<Long, Lukiokurssi> kurssitById = lukiokurssiRepository.findAll(structure.getKurssit()
                .stream().map(LukiokurssiOppaineMuokkausDto::getId).collect(toSet()))
                .stream().collect(toMap(Lukiokurssi::getId, k -> k));
        structure.getKurssit().forEach(kurssiDto -> {
            if (tryRestoreFromRevision != null && kurssitById.get(kurssiDto.getId()) == null) {
                Lukiokurssi oldKurssi = found(lukiokurssiRepository.findRevision(kurssiDto.getId(),
                            tryRestoreFromRevision), inPeruste(perusteId),
                            () -> new NotExistsException("No Lukiokurssi "+kurssiDto.getId()
                                    +" in lukioperuste at revision "+tryRestoreFromRevision));
                long id = createLukiokurssi(perusteId, mapper.map(oldKurssi, new LukioKurssiLuontiDto()));
                logger.info("Restored Lukiokurssi {} to id={}", oldKurssi.getId(), id);
                Lukiokurssi newKurssi = lukiokurssiRepository.findOne(id);
                kurssitById.put(id, newKurssi);
                kurssitById.put(kurssiDto.getId(), newKurssi); // new id differs
            }
            Lukiokurssi kurssi = found(kurssitById.get(kurssiDto.getId()), inPeruste(perusteId),
                    () -> new NotExistsException("No Lukiokurssi with id=" + kurssiDto.getId()));
            lukiokurssiRepository.lock(kurssi, false);
            mergeOppiaineet(perusteId, kurssi, kurssiDto.getOppiaineet());
        });
        rakenneRepository.setRevisioKommentti(structure.getKommentti());
        rakenne.setMuokattu(new Date());
        rakenneRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> listKurssiVersions(long perusteId, long kurssiId) {
        found(lukiokurssiRepository.findOne(kurssiId), inPeruste(perusteId));
        return lukiokurssiRepository.getRevisions(kurssiId);
    }
}
