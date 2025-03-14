package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.OpasTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.julkinen.AmosaaKoulutustoimijaDto;
import fi.vm.sade.eperusteet.dto.julkinen.JotpaTyyppi;
import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuDto;
import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuTyyppi;
import fi.vm.sade.eperusteet.dto.julkinen.TietoaPalvelustaDto;
import fi.vm.sade.eperusteet.dto.util.CacheArvot;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.JulkinenService;
import fi.vm.sade.eperusteet.service.YlopsClient;

import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import javassist.NotFoundException;
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

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Page<JulkiEtusivuDto> haeEtusivu(String nimi, String kieli, Integer sivu, Integer sivukoko) {
        List<JulkiEtusivuDto> julkiEtusivuDtos = self.getJulkisivuDatat();

        julkiEtusivuDtos = julkiEtusivuDtos.stream()
                .filter(dto -> dto.getKielet() != null && dto.getKielet().contains(Kieli.of(kieli)))
                .filter(dto -> nimi.isEmpty()
                        || (dto.getNimi().get(kieli) != null && dto.getNimi().get(kieli).toLowerCase().contains(nimi.toLowerCase()))
                        || Optional.ofNullable(dto.getKoulutustoimija()).map(AmosaaKoulutustoimijaDto::getNimi).map(n -> n.get(kieli)).map(n -> n.toLowerCase().contains(nimi.toLowerCase())).orElse(false)
                        || Optional.ofNullable(dto.getOrganisaatiot()).map(organisaatiot -> organisaatiot.stream()
                            .anyMatch(organisaatio -> organisaatio.getNimi() != null && organisaatio.getNimi().containsKey(kieli) && organisaatio.getNimi().get(kieli).toLowerCase().contains(nimi.toLowerCase()))).orElse(false))
                .sorted(Comparator.comparing(dto -> dto.getNimi() != null && dto.getNimi().get(kieli) != null ? dto.getNimi().get(kieli) : ""))
                .sorted(Comparator.comparing(dto -> JulkiEtusivuTyyppi.PERUSTE.equals(dto.getEtusivuTyyppi()) ? 0 : JulkiEtusivuTyyppi.OPAS.equals(dto.getEtusivuTyyppi()) ? 1 : 2))
                .collect(Collectors.toList());

        int startIdx = sivu * sivukoko;
        int endIdx = Math.min(startIdx + sivukoko, julkiEtusivuDtos.size());
        List<JulkiEtusivuDto> currentPage = endIdx < startIdx ? Collections.emptyList() : julkiEtusivuDtos.subList(startIdx, endIdx);

        return new PageImpl<>(currentPage, PageRequest.of(sivu, sivukoko), julkiEtusivuDtos.size());
    }

    @Override
    @Cacheable(CacheArvot.JULKINEN_ETUSIVU)
    public List<JulkiEtusivuDto> getJulkisivuDatat() {
        return Stream.of(getPerusteet(), getAmosaaOpetussuunnitelmat(), getYlopsOpetussuunnitelmat())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public TietoaPalvelustaDto getTietoaPalvelusta() {
        return perusteRepository.findByOpasTyyppi(OpasTyyppi.TIETOAPALVELUSTA).stream()
                .filter(peruste -> peruste.getTila().equals(PerusteTila.VALMIS))
                .map(peruste -> mapper.map(peruste, TietoaPalvelustaDto.class))
                .findFirst()
                .orElseThrow(NotExistsException::new);
    }

    private List<JulkiEtusivuDto> getPerusteet() {
        return julkaisutService.getKaikkiPerusteet().stream()
                .map(peruste -> {
            JulkiEtusivuDto dto = mapper.map(peruste, JulkiEtusivuDto.class);
            dto.setEtusivuTyyppi(JulkiEtusivuTyyppi.PERUSTE);

            if (PerusteTyyppi.of(peruste.getTyyppi()).equals(PerusteTyyppi.OPAS)) {
                dto.setEtusivuTyyppi(JulkiEtusivuTyyppi.OPAS);
            }

            if (PerusteTyyppi.of(peruste.getTyyppi()).equals(PerusteTyyppi.DIGITAALINEN_OSAAMINEN)) {
                dto.setEtusivuTyyppi(JulkiEtusivuTyyppi.DIGITAALINEN_OSAAMINEN);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    private List<JulkiEtusivuDto> getAmosaaOpetussuunnitelmat() {
        return amosaaClient.getOpetussuunnitelmatEtusivu().stream().map(opetussuunnitelma -> {
            JulkiEtusivuDto dto = mapper.map(opetussuunnitelma, JulkiEtusivuDto.class);
            dto.setVoimassaoloAlkaa(opetussuunnitelma.getVoimaantulo());
            if (opetussuunnitelma.getKoulutustyyppi() != null) {
                dto.setEtusivuTyyppi(opetussuunnitelma.getKoulutustyyppi().isAmmatillinen() ? JulkiEtusivuTyyppi.TOTEUTUSSUUNNITELMA : JulkiEtusivuTyyppi.OPETUSSUUNNITELMA);
            }

            if (JotpaTyyppi.MUU.equals(dto.getJotpatyyppi())) {
                dto.setKoulutustyyppi(KoulutusTyyppi.MUU_KOULUTUS.toString());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    private List<JulkiEtusivuDto> getYlopsOpetussuunnitelmat() {
        return ylopsClient.getOpetussuunnitelmatEtusivu().stream().map(opetussuunnitelma -> {
            JulkiEtusivuDto dto = mapper.map(opetussuunnitelma, JulkiEtusivuDto.class);
            dto.setEtusivuTyyppi(JulkiEtusivuTyyppi.OPETUSSUUNNITELMA);
            return dto;
        }).collect(Collectors.toList());
    }
}
