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
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.service.dokumentti.ExternalPdfService;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.internal.PdfService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import fi.vm.sade.eperusteet.utils.dto.dokumentti.DokumenttiMetaDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.pdfbox.preflight.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Profile("default")
public class DokumenttiServiceImpl implements DokumenttiService {

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private DokumenttiNewBuilderService newBuilder;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private KVLiiteBuilderService kvLiiteBuilderService;

    @Autowired
    private DokumenttiStateService dokumenttiStateService;

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private PlatformTransactionManager tm;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private ExternalPdfService externalPdfService;

    @Value("classpath:docgen/fop.xconf")
    private Resource fopConfig;

    // FIXME: Tämä service pitää mockata
    @Value("${spring.profiles.active:normal}")
    private String activeProfile;

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

        Peruste peruste = perusteRepository.findOne(id);
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
        Sort sort = new Sort(Sort.Direction.DESC, "valmistumisaika");

        List<Dokumentti> documents;

        // Kvliite ei riipu suoritustavasta
        if (GeneratorVersion.KVLIITE.equals(version)) {
            documents = dokumenttiRepository.findByPerusteIdAndKieliAndGeneratorVersionAndValmistumisaikaIsNotNull(
                    id, kieli, version, sort);
        } else {
            documents = dokumenttiRepository.findByPerusteIdAndKieliAndSuoritustapakoodiAndGeneratorVersionAndValmistumisaikaIsNotNull(
                    id, kieli, suoritustapakoodi,
                    version != null ? version : GeneratorVersion.UUSI, sort);
        }

        if (documents.size() > 0) {
            DokumenttiDto dokumentti = mapper.map(documents.get(0), DokumenttiDto.class);
            DokumenttiDto julkaisuDokumentti = getJulkaistuDokumentti(id, kieli, null);
            if (julkaisuDokumentti != null && dokumentti.getId().equals(julkaisuDokumentti.getId())) {
                dokumentti.setJulkaisuDokumentti(true);
            }
            return dokumentti;
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
    @Async(value = "docTaskExecutor")
    public void generateWithDto(DokumenttiDto dto) throws DokumenttiException {
        dto.setTila(DokumenttiTila.LUODAAN);
        dokumenttiStateService.save(dto);

        try {
            externalPdfService.generatePdf(dto);
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
        Dokumentti dokumentti = dokumenttiRepository.findById(id);

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
        Sort sort = new Sort(Sort.Direction.DESC, "valmistumisaika");

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
        Dokumentti dokumentti = dokumenttiRepository.findById(id);
        if (dokumentti != null) {
            Peruste peruste = perusteRepository.findOne(dokumentti.getPerusteId());
            String name = SecurityUtil.getAuthenticatedPrincipal().getName();
            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS) && julkaisutRepository.findAllByPeruste(peruste).isEmpty()) {
                return null;
            }
        }
        return mapper.map(dokumentti, DokumenttiDto.class);
    }

    private byte[] generateFor(DokumenttiDto dto)
            throws IOException,TransformerException, ParserConfigurationException, SAXException {

        Peruste peruste = perusteRepository.findOne(dto.getPerusteId());
        Kieli kieli = dto.getKieli();
        Dokumentti dokumentti = mapper.map(dto, Dokumentti.class);
        byte[] toReturn = null;
        ValidationResult result;
        GeneratorVersion version = dto.getGeneratorVersion();

        DokumenttiMetaDto meta = DokumenttiMetaDto.builder()
                .title(DokumenttiUtils.getTextString(dokumentti.getKieli(), peruste.getNimi()))
                .build();

        log.info("Luodaan dokumenttia (" + dto.getPerusteId() + ", " + dto.getSuoritustapakoodi() + ", "
                + kieli + ", " + version + ") perusteelle.");
        switch (version) {
            case VANHA:
                throw new BusinessRuleViolationException("vanha-generointi-poistettu-kaytosta");
            case UUSI:
                Document doc = newBuilder.generateXML(peruste, dokumentti);

                meta.setSubject(messages.translate("docgen.meta.subject.peruste", kieli));
                toReturn = pdfService.xhtml2pdf(doc, meta);

                /*
                // Validoidaan dokumnetti
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    log.debug("Dokumentti (" + dto.getPerusteId() + ", "
                            + dto.getSuoritustapakoodi() + ", " + kieli + ") on PDF/A-1b mukainen.");
                } else {
                    log.debug("Dokumentti (" + dto.getPerusteId() + ", " + dto.getSuoritustapakoodi() + ", "
                            + kieli + ") ei ole PDF/A-1b mukainen. Dokumentti sisältää virheen/virheet:");
                    result.getErrorsList().forEach(error -> log
                            .debug("  - " + error.getDetails() + " (" + error.getErrorCode() + ")"));
                }
                */

                break;
            case KVLIITE:
                doc = kvLiiteBuilderService.generateXML(peruste, kieli);

                meta.setSubject(messages.translate("docgen.meta.subject.kvliite", kieli));
                toReturn = pdfService.xhtml2pdf(doc, version, meta);

                /*
                // Validoi kvliite
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    log.debug("Dokumentti (" + dto.getPerusteId() + ", " + kieli + ") on PDF/A-1b mukainen.");
                } else {
                    log.debug("Dokumentti (" + dto.getId() + ", " + kieli
                            + ") ei ole PDF/A-1b mukainen. Dokumentti sisältää virheen/virheet:");
                    result.getErrorsList().forEach(error -> log
                            .debug("  - " + error.getDetails() + " (" + error.getErrorCode() + ")"));
                }
                */

                break;
            default:
                break;
        }
        return toReturn;
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public void updateDokumenttiPdfData(String data, Long dokumenttiId) {
        Dokumentti dokumentti = dokumenttiRepository.findById(dokumenttiId);
        dokumentti.setData(Base64.getDecoder().decode(data));
        dokumentti.setTila(DokumenttiTila.VALMIS);
        dokumentti.setValmistumisaika(new Date());
        dokumenttiRepository.save(dokumentti);
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public void updateDokumenttiTila(DokumenttiTila tila, Long dokumenttiId) {
        Dokumentti dokumentti = dokumenttiRepository.findById(dokumenttiId);
        dokumentti.setTila(tila);
        dokumenttiRepository.save(dokumentti);
    }

}
