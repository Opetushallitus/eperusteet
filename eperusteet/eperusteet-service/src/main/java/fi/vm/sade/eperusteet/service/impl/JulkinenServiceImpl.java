package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.dto.julkinen.AmosaaKoulutustoimijaDto;
import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuDto;
import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuTyyppi;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.JulkinenService;
import fi.vm.sade.eperusteet.service.YlopsClient;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class JulkinenServiceImpl implements JulkinenService {

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private AmosaaClient amosaaClient;

    @Autowired
    private YlopsClient ylopsClient;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    @Lazy
    private JulkinenService self;

    @Override
    public Page<JulkiEtusivuDto> haeEtusivu(String nimi, String kieli, Integer sivu, Integer sivukoko) {
        List<JulkiEtusivuDto> julkiEtusivuDtos = self.getJulkisivuDatat();

        julkiEtusivuDtos = julkiEtusivuDtos.stream()
                .filter(dto -> nimi.isEmpty()
                        || (dto.getNimi().get(kieli) != null && dto.getNimi().get(kieli).toLowerCase().contains(nimi.toLowerCase()))
                        || Optional.ofNullable(dto.getKoulutustoimija()).map(AmosaaKoulutustoimijaDto::getNimi).map(n -> n.get(kieli)).map(n -> n.toLowerCase().contains(nimi.toLowerCase())).orElse(false)
                        || Optional.ofNullable(dto.getOrganisaatiot()).map(organisaatiot -> organisaatiot.stream()
                            .anyMatch(organisaatio -> organisaatio.getNimi() != null && organisaatio.getNimi().containsKey(kieli) && organisaatio.getNimi().get(kieli).toLowerCase().contains(nimi.toLowerCase()))).orElse(false))
                .sorted(Comparator.comparing(dto -> dto.getNimi().get(kieli)))
                .collect(Collectors.toList());

        int startIdx = sivu * sivukoko;
        int endIdx = Math.min(startIdx + sivukoko, julkiEtusivuDtos.size());
        List<JulkiEtusivuDto> currentPage = endIdx < startIdx ? Collections.emptyList() : julkiEtusivuDtos.subList(startIdx, endIdx);

        return new PageImpl<>(currentPage, new PageRequest(sivu, sivukoko), julkiEtusivuDtos.size());
    }

    @Override
    @Cacheable("julkinenEtusivu")
    public List<JulkiEtusivuDto> getJulkisivuDatat() {
        return Stream.of(getPerusteet(), getAmosaaOpetussuunnitelmat(), getYlopsOpetussuunnitelmat())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<JulkiEtusivuDto> getPerusteet() {
        return julkaisutService.getKaikkiPerusteet().stream()
                .map(peruste -> {
            JulkiEtusivuDto dto = mapper.map(peruste, JulkiEtusivuDto.class);
            dto.setTyyppi(JulkiEtusivuTyyppi.PERUSTE);
            return dto;
        }).collect(Collectors.toList());
    }

    private List<JulkiEtusivuDto> getAmosaaOpetussuunnitelmat() {
        return amosaaClient.getOpetussuunnitelmatEtusivu().stream().map(opetussuunnitelma -> {
            JulkiEtusivuDto dto = mapper.map(opetussuunnitelma, JulkiEtusivuDto.class);
            dto.setVoimassaoloAlkaa(opetussuunnitelma.getVoimaantulo());
            dto.setTyyppi(JulkiEtusivuTyyppi.TOTEUTUSSUUNNITELMA);
            return dto;
        }).collect(Collectors.toList());
    }

    private List<JulkiEtusivuDto> getYlopsOpetussuunnitelmat() {
        return ylopsClient.getOpetussuunnitelmatEtusivu().stream().map(opetussuunnitelma -> {
            JulkiEtusivuDto dto = mapper.map(opetussuunnitelma, JulkiEtusivuDto.class);
            dto.setTyyppi(JulkiEtusivuTyyppi.OPETUSSUUNNITELMA);
            return dto;
        }).collect(Collectors.toList());
    }
}
