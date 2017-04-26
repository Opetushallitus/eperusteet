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

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.AIPEOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Service
@Transactional
public class AIPEOpetuksenPerusteenSisaltoServiceImpl implements AIPEOpetuksenPerusteenSisaltoService {

    @Autowired
    private EntityManager em;

    @Autowired
    private AIPEVaiheRepository vaiheRepository;

    @Autowired
    private AIPEOppiaineRepository oppiaineRepository;

    @Autowired
    private AIPEKurssiRepository kurssiRepository;

    @Autowired
    private LaajaalainenOsaaminenRepository laajaalainenOsaaminenRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Transactional
    private Peruste getPeruste(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-olemassa");
        }
        return peruste;
    }

    @Transactional
    private AIPEOpetuksenSisalto getPerusteSisalto(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-olemassa");
        }

        AIPEOpetuksenSisalto sisalto = peruste.getAipeOpetuksenPerusteenSisalto();
        if (sisalto == null) {
            throw new BusinessRuleViolationException("perusteella-ei-oikeaa-sisaltoa");
        }
        return sisalto;
    }

    private AIPEVaihe getVaiheImpl(Long perusteId, Long vaiheId) {
        Peruste peruste = getPeruste(perusteId);
        AIPEOpetuksenSisalto sisalto = peruste.getAipeOpetuksenPerusteenSisalto();
        Optional<AIPEVaihe> vaihe = sisalto.getVaihe(vaiheId);
        if (!vaihe.isPresent()) {
            throw new BusinessRuleViolationException("vaihetta-ei-olemassa");
        }
        return vaihe.get();
    }

    private AIPEOppiaine getOppiaineImpl(Long perusteID, Long vaiheId, Long oppiaineId) {
        AIPEVaihe vaihe = getVaiheImpl(perusteID, vaiheId);
        AIPEOppiaine oppiaine = vaihe.getOppiaine(oppiaineId);
        if (oppiaine == null) {
            throw new BusinessRuleViolationException("oppiainetta-ei-olemassa");
        }
        return oppiaine;
    }

    private LaajaalainenOsaaminen getLaajaalainenImpl(Long perusteId, Long laajalainenId) {
        AIPEOpetuksenSisalto sisalto = getPerusteSisalto(perusteId);
        Optional<LaajaalainenOsaaminen> optLo = sisalto.getLaajaalainenOsaaminen(laajalainenId);
        if (!optLo.isPresent()) {
            throw new BusinessRuleViolationException("laajaalainen-ei-olemassa");
        }
        return optLo.get();
    }

    private AIPEKurssi getKurssiImpl(Long perusteID, Long vaiheId, Long oppiaineId, Long kurssiId) {
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteID, vaiheId, oppiaineId);
        Optional<AIPEKurssi> kurssi = oppiaine.getKurssi(kurssiId);
        if (!kurssi.isPresent()) {
            throw new BusinessRuleViolationException("kurssia-ei-olemassa");
        }
        return kurssi.get();
    }

    @Override
    public List<AIPEOppiaineSuppeaDto> getOppimaarat(Long perusteId, Long vaiheId, Long oppiaineId) {
        AIPEOppiaine parent = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        return mapper.mapAsList(parent.getOppimaarat(), AIPEOppiaineSuppeaDto.class);
    }

    @Override
    public AIPEOppiaineDto addOppimaara(Long perusteId, Long vaiheId, Long oppiaineId, AIPEOppiaineDto oppiaineDto) {
        AIPEOppiaine parent = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        if (parent.getKurssit() != null && !parent.getKurssit().isEmpty()) {
            throw new BusinessRuleViolationException("oppimaaraa-ei-voi-lisata-jos-kursseja");
        }
        oppiaineDto.setId(null);
        AIPEOppiaine oppimaara = mapper.map(oppiaineDto, AIPEOppiaine.class);
        oppimaara = oppiaineRepository.save(oppimaara);
        parent.getOppimaarat().add(oppimaara);
        return mapper.map(oppimaara, AIPEOppiaineDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto getLaajaalainen(Long perusteId, Long laajalainenId) {
        return mapper.map(getLaajaalainenImpl(perusteId, laajalainenId), LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto addLaajaalainen(Long perusteId, LaajaalainenOsaaminenDto laajaalainenDto) {
        laajaalainenDto.setId(null);
        LaajaalainenOsaaminen lo = mapper.map(laajaalainenDto, LaajaalainenOsaaminen.class);
        lo = laajaalainenOsaaminenRepository.save(lo);
        getPerusteSisalto(perusteId).getLaajaalaisetosaamiset().add(lo);
        return mapper.map(lo, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto updateLaajaalainen(Long perusteId, Long laajalainenId, LaajaalainenOsaaminenDto laajaalainenDto) {
        LaajaalainenOsaaminen lo = getLaajaalainenImpl(perusteId, laajalainenId);
        mapper.map(laajaalainenDto, lo);
        return mapper.map(lo, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public void removeLaajaalainen(Long perusteId, Long laajaalainenId) {
        laajaalainenOsaaminenRepository.delete(laajaalainenId);
    }

    @Override
    public AIPEKurssiDto getKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId) {
        return mapper.map(getKurssiImpl(perusteId, vaiheId, oppiaineId, kurssiId), AIPEKurssiDto.class);
    }

    @Override
    public List<AIPEKurssiSuppeaDto> getKurssit(Long perusteId, Long vaiheId, Long oppiaineId) {
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        return mapper.mapAsList(oppiaine.getKurssit(), AIPEKurssiSuppeaDto.class);
    }

    @Override
    public AIPEKurssiDto addKurssi(Long perusteId, Long vaiheId, Long oppiaineId, AIPEKurssiDto kurssiDto) {
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        if (oppiaine.getOppimaarat() != null && !oppiaine.getOppimaarat().isEmpty()) {
            throw new BusinessRuleViolationException("kurssia-ei-voi-lisata-jos-oppiaineita");
        }
        kurssiDto.setId(null);
        AIPEKurssi kurssi = mapper.map(kurssiDto, AIPEKurssi.class);
        kurssi = kurssiRepository.save(kurssi);
        oppiaine.getKurssit().add(kurssi);
        return mapper.map(kurssi, AIPEKurssiDto.class);
    }

    @Override
    public AIPEKurssiDto updateKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId, AIPEKurssiDto kurssiDto) {
        AIPEKurssi kurssi = getKurssiImpl(perusteId, vaiheId, oppiaineId, kurssiId);
        kurssiDto.setId(kurssiId);
        kurssi = mapper.map(kurssiDto, kurssi);
        return mapper.map(kurssi, AIPEKurssiDto.class);
    }

    private void removeKurssiImpl(AIPEOppiaine oppiaine, AIPEKurssi kurssi) {
        oppiaine.getKurssit().remove(kurssi);
    }

    @Override
    public void removeKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId) {
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        AIPEKurssi kurssi = getKurssiImpl(perusteId, vaiheId, oppiaineId, kurssiId);
        removeKurssiImpl(oppiaine, kurssi);
    }

    @Override
    public AIPEOppiaineDto getOppiaine(Long perusteId, Long vaiheId, Long oppiaineId) {
        return mapper.map(getOppiaineImpl(perusteId, vaiheId, oppiaineId), AIPEOppiaineDto.class);
    }

    @Override
    public AIPEOppiaineDto updateOppiaine(Long perusteId, Long vaiheId, Long oppiaineId, AIPEOppiaineDto oppiaineDto) {
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
        oppiaineDto.setId(oppiaineId);
        oppiaine = mapper.map(oppiaineDto, oppiaine);
        List<OpetuksenTavoite> tavoitteet = oppiaine.getTavoitteet();
        List<OpetuksenTavoiteDto> tavoitteetDtos = mapper.mapAsList(tavoitteet, OpetuksenTavoiteDto.class);
        AIPEOppiaineDto dto = mapper.map(oppiaine, AIPEOppiaineDto.class);
        dto.setTavoitteet(tavoitteetDtos);
        return dto;
    }

    @Override
    public AIPEOppiaineDto addOppiaine(Long perusteId, Long vaiheId, AIPEOppiaineDto oppiaineDto) {
        AIPEVaihe vaihe = vaiheRepository.findOne(vaiheId);
        oppiaineDto.setId(null);
        AIPEOppiaine oa = mapper.map(oppiaineDto, AIPEOppiaine.class);
        oa = oppiaineRepository.save(oa);
        vaihe.getOppiaineet().add(oa);
        return mapper.map(oa, AIPEOppiaineDto.class);
    }

    @Override
    public void removeOppiaine(Long perusteId, Long vaiheId, Long oppiaineId) {
        AIPEVaihe vaihe = getVaiheImpl(perusteId, vaiheId);
        AIPEOppiaine oppiaine = getOppiaineImpl(perusteId, vaiheId, oppiaineId);
//        Set<AIPEOppiaine> oppiaineet = new HashSet<>();
        if (!vaihe.getOppiaineet().remove(oppiaine)) {
            // FIXME: JoinTable, mäppäys lapsioliosta parentiin ja envers
            for (AIPEOppiaine parent : vaihe.getOppiaineet()) {
                if (parent.getOppimaarat().remove(oppiaine)) {
                    break;
                }
            }
        }
    }

    @Override
    public List<OpetuksenKohdealueDto> getKohdealueet(Long perusteId, Long vaiheId) {
        return mapper.mapAsList(getVaiheImpl(perusteId, vaiheId).getOpetuksenKohdealueet(), OpetuksenKohdealueDto.class);
    }

    @Override
    public List<AIPEOppiaineSuppeaDto> getOppiaineet(Long perusteId, Long vaiheId) {
        AIPEVaihe vaihe = getVaiheImpl(perusteId, vaiheId);
        List<AIPEOppiaine> oppiaineet = vaihe.getOppiaineet();
        return mapper.mapAsList(oppiaineet, AIPEOppiaineSuppeaDto.class);
    }

    @Override
    public AIPEVaiheDto getVaihe(Long perusteId, Long vaiheId) {
        AIPEVaihe vaihe = vaiheRepository.findOne(vaiheId);
        AIPEVaiheDto dto = mapper.map(vaihe, AIPEVaiheDto.class);
        dto.setOpetuksenKohdealueet(mapper.mapAsList(vaihe.getOpetuksenKohdealueet(), OpetuksenKohdealueDto.class));
        return dto;
    }

    @Override
    public List<AIPEVaiheSuppeaDto> getVaiheet(Long perusteId) {
        List<AIPEVaihe> vaiheet = getPeruste(perusteId).getAipeOpetuksenPerusteenSisalto().getVaiheet();
        return mapper.mapAsList(vaiheet, AIPEVaiheSuppeaDto.class);
    }

    @Override
    public AIPEVaiheDto addVaihe(Long perusteId, AIPEVaiheDto vaiheDto) {
        vaiheDto.setId(null);
        Peruste peruste = getPeruste(perusteId);
        AIPEOpetuksenSisalto sisalto = peruste.getAipeOpetuksenPerusteenSisalto();
        AIPEVaihe vaihe = mapper.map(vaiheDto, AIPEVaihe.class);
        vaihe = vaiheRepository.save(vaihe);
        sisalto.getVaiheet().add(vaihe);
        return mapper.map(vaihe, AIPEVaiheDto.class);
    }

    @Override
    public AIPEVaiheDto updateVaihe(Long perusteId, Long vaiheId, AIPEVaiheDto vaiheDto) {
        AIPEVaihe vaihe = getVaiheImpl(perusteId, vaiheId);
        vaiheDto.setId(vaiheId);
        vaihe = mapper.map(vaiheDto, vaihe);
        return mapper.map(vaihe, AIPEVaiheDto.class);
    }

    @Override
    public void removeVaihe(Long perusteId, Long vaiheId) {
        AIPEVaihe vaihe = getVaiheImpl(perusteId, vaiheId);
        if (!vaihe.getOppiaineet().isEmpty()) {
            throw new BusinessRuleViolationException("vaiheella-oppiaineita");
        }
        AIPEOpetuksenSisalto perusteenSisalto = getPerusteSisalto(perusteId);
        perusteenSisalto.getVaiheet().remove(vaihe);
    }

    @Override
    public List<LaajaalainenOsaaminenDto> getLaajaalaiset(Long perusteId) {
        Peruste peruste = getPeruste(perusteId);
        List<LaajaalainenOsaaminen> laajaalaisetosaamiset = peruste.getAipeOpetuksenPerusteenSisalto().getLaajaalaisetosaamiset();
        return mapper.mapAsList(laajaalaisetosaamiset, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public void updateLaajaalainenOsaaminenJarjestys(Long perusteId, List<LaajaalainenOsaaminenDto> laajaalaiset) {
        AIPEOpetuksenSisalto sisalto = getPeruste(perusteId).getAipeOpetuksenPerusteenSisalto();
        if (laajaalaiset.size() != sisalto.getLaajaalaisetosaamiset().size()) {
            Set<Long> nykyiset = sisalto.getLaajaalaisetosaamiset().stream()
                    .map(LaajaalainenOsaaminen::getId)
                    .collect(Collectors.toSet());
            Set<Long> uudet = laajaalaiset.stream()
                    .map(LaajaalainenOsaaminenDto::getId)
                    .collect(Collectors.toSet());
            if (nykyiset.size() != uudet.size() || uudet.containsAll(nykyiset)) {
                throw new BusinessRuleViolationException("laajaa-alaisia-ei-voi-muuttaa");
            }
        }

        Map<Long, LaajaalainenOsaaminen> loMap = sisalto.getLaajaalaisetosaamiset().stream()
                .collect(Collectors.toMap(LaajaalainenOsaaminen::getId, lo -> lo));

        Integer idx = 0;
        for (LaajaalainenOsaaminenDto laajaalainen : laajaalaiset) {
            loMap.get(laajaalainen.getId()).setJarjestys(idx);
            idx += 1;
        }
    }
}
