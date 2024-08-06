package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.DokumenttiVirhe;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.service.dokumentti.ExternalPdfService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Profile("!test")
public class DokumenttiServiceImpl implements DokumenttiService {

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private DokumenttiStateService dokumenttiStateService;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private ExternalPdfService externalPdfService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto createDtoFor(
            long id,
            Kieli kieli,
            Suoritustapakoodi suoritustapakoodi,
            GeneratorVersion version
    ) {
        String name = SecurityUtil.getAuthenticatedPrincipal().getName();
        Dokumentti dokumentti = new Dokumentti();
        dokumentti.setTila(DokumenttiTila.EI_OLE);
        dokumentti.setKieli(kieli);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(name);
        dokumentti.setPerusteId(id);
        dokumentti.setSuoritustapakoodi(suoritustapakoodi);
        dokumentti.setGeneratorVersion(version);

        Peruste peruste = perusteRepository.findById(id).orElse(null);
        if (peruste != null) {
            Dokumentti saved = dokumenttiRepository.save(dokumentti);
            return mapper.map(saved, DokumenttiDto.class);
        } else {
            dokumentti.setTila(DokumenttiTila.EPAONNISTUI);
            dokumentti.setVirhekoodi(DokumenttiVirhe.PERUSTETTA_EI_LOYTYNYT);
            return mapper.map(dokumentti, DokumenttiDto.class);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi) {
        return findLatest(id, kieli, suoritustapakoodi, null);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion version) {
        Dokumentti dokumentti;

        // Kvliite ei riipu suoritustavasta
        if (GeneratorVersion.KVLIITE.equals(version)) {
            dokumentti = dokumenttiRepository.findFirstByPerusteIdAndKieliAndGeneratorVersionOrderByAloitusaikaDesc(
                    id, kieli, version);
        } else {
            dokumentti = dokumenttiRepository.findFirstByPerusteIdAndKieliAndSuoritustapakoodiAndGeneratorVersionOrderByAloitusaikaDesc(
                    id, kieli, suoritustapakoodi,
                    version != null ? version : GeneratorVersion.UUSI);
        }

        if (dokumentti != null) {
            DokumenttiDto dokumenttiDto = mapper.map(dokumentti, DokumenttiDto.class);
            DokumenttiDto julkaisuDokumentti = getJulkaistuDokumentti(id, kieli, null);
            if (julkaisuDokumentti != null && dokumenttiDto.getId().equals(julkaisuDokumentti.getId())) {
                dokumenttiDto.setJulkaisuDokumentti(true);
            }
            return dokumenttiDto;
        } else {
            DokumenttiDto dto = new DokumenttiDto();
            dto.setPerusteId(id);
            dto.setKieli(kieli);
            dto.setTila(DokumenttiTila.EI_OLE);
            return dto;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto getJulkaistuDokumentti(Long perusteId, Kieli kieli, Integer revision) {
        Peruste peruste = perusteRepository.findOne(perusteId);

        if (peruste == null) {
            return null;
        }

        JulkaistuPeruste julkaisu;
        if (revision != null) {
            julkaisu = julkaisutRepository.findFirstByPerusteAndRevisionOrderByIdDesc(peruste, revision);
        } else {
            julkaisu = julkaisutRepository.findFirstByPerusteIdOrderByRevisionDesc(peruste.getId());
        }

        if (julkaisu != null && CollectionUtils.isNotEmpty(julkaisu.getDokumentit())) {
            Dokumentti dokumentti = dokumenttiRepository.findByIdInAndKieli(julkaisu.getDokumentit(), kieli);
            if (dokumentti != null) {
                DokumenttiDto dokumenttiDto = mapper.map(dokumentti, DokumenttiDto.class);
                dokumenttiDto.setJulkaisuDokumentti(true);
                return dokumenttiDto;
            }
        }
        return null;
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class, propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    public void generateWithDto(DokumenttiDto dto) throws DokumenttiException {
        generateWithDto(dto, null);
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class, propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    public void generateWithDto(DokumenttiDto dto, PerusteKaikkiDto perusteDto) throws DokumenttiException {
        dto.setTila(DokumenttiTila.LUODAAN);
        dokumenttiStateService.save(dto);

        try {
            externalPdfService.generatePdf(dto, perusteDto);
        } catch (Exception ex) {
            dto.setTila(DokumenttiTila.EPAONNISTUI);
            dto.setVirhekoodi(DokumenttiVirhe.TUNTEMATON);
            dto.setValmistumisaika(new Date());
            dokumenttiStateService.save(dto);
            throw new DokumenttiException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public byte[] get(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id).orElse(null);

        if (dokumentti != null) {
            Peruste peruste = perusteRepository.findOne(dokumentti.getPerusteId());
            if (peruste == null) {
                return null;
            }

            String name = SecurityUtil.getAuthenticatedPrincipal().getName();
            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS) && julkaisutRepository.findAllByPeruste(peruste).isEmpty()) {
                return null;
            }

            return dokumentti.getData();
        }

        return null;
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public Long getDokumenttiId(Long perusteId, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion generatorVersion) {
        Sort sort = Sort.by(Sort.Direction.DESC, "valmistumisaika");

        Peruste peruste = perusteRepository.findOne(perusteId);
        if (peruste == null) {
            return null;
        }

        Set<Suoritustapa> suoritustavat = peruste.getSuoritustavat();
        List<Dokumentti> documents;
        if (suoritustavat.isEmpty()) {
            documents = dokumenttiRepository
                    .findByPerusteIdAndKieliAndTilaAndGeneratorVersion(
                            perusteId, kieli, DokumenttiTila.VALMIS, generatorVersion, sort);
        } else {
            documents = dokumenttiRepository
                    .findByPerusteIdAndKieliAndTilaAndSuoritustapakoodiAndGeneratorVersion(
                            perusteId, kieli, DokumenttiTila.VALMIS, suoritustapakoodi, generatorVersion, sort);
        }

        if (!documents.isEmpty()) {
            return documents.get(0).getId();
        } else {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    public void setStarted(DokumenttiDto dto) {
        dto.setAloitusaika(new Date());
        dto.setLuoja(SecurityUtil.getAuthenticatedPrincipal().getName());
        dto.setTila(DokumenttiTila.JONOSSA);
        dokumenttiStateService.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto query(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id).orElse(null);
        DokumenttiDto dto = mapper.map(dokumentti, DokumenttiDto.class);

        if (dokumentti != null) {
            Peruste peruste = perusteRepository.findOne(dokumentti.getPerusteId());
            String name = SecurityUtil.getAuthenticatedPrincipal().getName();
            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS) && julkaisutRepository.findAllByPeruste(peruste).isEmpty()) {
                return null;
            }

            if (DokumenttiUtils.isTimePass(dokumentti)) {
                log.error("dokumentin valmistus kesti yli {} minuuttia, perusteella {}", DokumenttiUtils.MAX_TIME_IN_MINUTES, dto.getPerusteId());
                dto.setTila(DokumenttiTila.EPAONNISTUI);
                dto.setVirhekoodi(DokumenttiVirhe.TUNTEMATON);
                dto.setValmistumisaika(new Date());
                dokumenttiStateService.save(dto);
            }
        }
        return dto;
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public void updateDokumenttiPdfData(byte[] data, Long dokumenttiId) {
        Optional<Dokumentti> optionalDokumentti = dokumenttiRepository.findById(dokumenttiId);
        if (optionalDokumentti.isPresent()) {
            Dokumentti dokumentti = optionalDokumentti.get();
            dokumentti.setData(data);
            dokumentti.setTila(DokumenttiTila.VALMIS);
            dokumentti.setValmistumisaika(new Date());
            dokumenttiRepository.save(dokumentti);
        }
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public void updateDokumenttiTila(DokumenttiTila tila, Long dokumenttiId) {
        Optional<Dokumentti> optionalDokumentti = dokumenttiRepository.findById(dokumenttiId);
        if (optionalDokumentti.isPresent()) {
            Dokumentti dokumentti = optionalDokumentti.get();
            dokumentti.setTila(tila);
            dokumenttiRepository.save(dokumentti);
        }
    }
}
