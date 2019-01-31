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
package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.internal.PdfService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import fi.vm.sade.eperusteet.utils.dto.dokumentti.DokumenttiMetaDto;
import org.apache.pdfbox.preflight.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.JULKAISTU;

/**
 *
 * @author jussini
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiServiceImpl.class);

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private DokumenttiBuilderService builder;

    @Autowired
    private DokumenttiNewBuilderService newBuilder;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private KVLiiteBuilderService kvLiiteBuilderService;

    @Autowired
    private DokumenttiStateService dokumenttiStateService;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private PlatformTransactionManager tm;

    @Value("classpath:docgen/fop.xconf")
    private Resource fopConfig;

    // FIXME: Tämä service pitää mockata
    @Value("${spring.profiles.active:normal}")
    private String activeProfile;

    @Override
    @Transactional
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
            documents = dokumenttiRepository.findByPerusteIdAndKieliAndTilaAndGeneratorVersion(
                    id, kieli, DokumenttiTila.VALMIS, version, sort);
        } else {
            documents = dokumenttiRepository.findByPerusteIdAndKieliAndTilaAndSuoritustapakoodiAndGeneratorVersion(
                    id, kieli, DokumenttiTila.VALMIS, suoritustapakoodi,
                    version != null ? version : GeneratorVersion.UUSI, sort);
        }

        if (documents.size() > 0) {
            return mapper.map(documents.get(0), DokumenttiDto.class);
        } else {
            DokumenttiDto dto = new DokumenttiDto();
            dto.setPerusteId(id);
            dto.setKieli(kieli);
            dto.setTila(DokumenttiTila.EI_OLE);
            return dto;
        }
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class)
    @IgnorePerusteUpdateCheck
    @Async(value = "docTaskExecutor")
    public void generateWithDto(DokumenttiDto dto) throws DokumenttiException {
        dto.setTila(DokumenttiTila.LUODAAN);
        dokumenttiStateService.save(dto);

        Dokumentti dokumentti = dokumenttiRepository.findById(dto.getId());
        if (dokumentti == null) {
            dokumentti = mapper.map(dto, Dokumentti.class);
        }

        try {
            dokumentti.setData(generateFor(dto));
            dokumentti.setTila(DokumenttiTila.VALMIS);
            dokumentti.setValmistumisaika(new Date());
            dokumenttiRepository.save(dokumentti);
        } catch (Exception ex) {
            dto.setTila(DokumenttiTila.EPAONNISTUI);
            dto.setVirhekoodi(DokumenttiVirhe.TUNTEMATON);
            dto.setValmistumisaika(new Date());
            dokumenttiStateService.save(dto);

            throw new DokumenttiException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class)
    @IgnorePerusteUpdateCheck
    public void generateWithDtoSynchronous(DokumenttiDto dto) throws DokumenttiException {
        dto.setTila(DokumenttiTila.LUODAAN);
        dokumenttiStateService.save(dto);

        Dokumentti dokumentti = dokumenttiRepository.findById(dto.getId());
        if (dokumentti == null) {
            dokumentti = mapper.map(dto, Dokumentti.class);
        }

        try {
            dokumentti.setData(generateFor(dto));
            dokumentti.setTila(DokumenttiTila.VALMIS);
            dokumentti.setValmistumisaika(new Date());
            dokumenttiRepository.save(dokumentti);
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
            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS)) {
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
    @Transactional
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
            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS)) {
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

        LOG.info("Luodaan dokumenttia (" + dto.getPerusteId() + ", " + dto.getSuoritustapakoodi() + ", "
                + kieli + ", " + version + ") perusteelle.");
        switch (version) {
            case VANHA:
                throw new BusinessRuleViolationException("vanha-generointi-poistettu-kaytosta");
            case UUSI:
                Document doc = newBuilder.generateXML(peruste, dokumentti);

                meta.setSubject(messages.translate("docgen.meta.subject.peruste", kieli));
                toReturn = pdfService.xhtml2pdf(doc, meta);

                // Validoidaan dokumnetti
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    LOG.debug("Dokumentti (" + dto.getPerusteId() + ", "
                            + dto.getSuoritustapakoodi() + ", " + kieli + ") on PDF/A-1b mukainen.");
                } else {
                    LOG.debug("Dokumentti (" + dto.getPerusteId() + ", " + dto.getSuoritustapakoodi() + ", "
                            + kieli + ") ei ole PDF/A-1b mukainen. Dokumentti sisältää virheen/virheet:");
                    result.getErrorsList().forEach(error -> LOG
                            .debug("  - " + error.getDetails() + " (" + error.getErrorCode() + ")"));
                }

                break;
            case KVLIITE:
                doc = kvLiiteBuilderService.generateXML(peruste, kieli);

                meta.setSubject(messages.translate("docgen.meta.subject.kvliite", kieli));
                toReturn = pdfService.xhtml2pdf(doc, version, meta);

                // Validoi kvliite
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    LOG.debug("Dokumentti (" + dto.getPerusteId() + ", " + kieli + ") on PDF/A-1b mukainen.");
                } else {
                    LOG.debug("Dokumentti (" + dto.getId() + ", " + kieli
                            + ") ei ole PDF/A-1b mukainen. Dokumentti sisältää virheen/virheet:");
                    result.getErrorsList().forEach(error -> LOG
                            .debug("  - " + error.getDetails() + " (" + error.getErrorCode() + ")"));
                }

                break;
            default:
                break;
        }
        return toReturn;
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void paivitaDokumentit() {
        LOG.debug("Luodaan uudet PDF-dokumentit.");


        TransactionTemplate template = new TransactionTemplate(tm);
        List<PerusteprojektiDokumenttiDto> perusteprojektit = template.execute(status -> mapper
                .mapAsList(perusteprojektiRepository.findAll().stream()
                        .filter(projekti -> projekti.getTila().equals(JULKAISTU))
                        .collect(Collectors.toList()), PerusteprojektiDokumenttiDto.class));


        int counter = 1;

        for (PerusteprojektiDokumenttiDto pp : perusteprojektit) {

            PerusteDokumenttiDto p = pp.getPeruste();
            if (p.getTyyppi() != PerusteTyyppi.NORMAALI) {
                continue;
            }

            for (Kieli kieli : p.getKielet()) {
                for (SuoritustapaDto st : p.getSuoritustavat()) {
                    // Luodaan perusteen dokumentit
                    try {
                        paivitaDokumentti(p.getId(), kieli, st.getSuoritustapakoodi(), counter);
                    } catch (RuntimeException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            for (Kieli kieli : Kieli.kvliiteKielet()) {
                if (!ObjectUtils.isEmpty(p.getSuoritustavat())) {
                    // Luodaan kvliitteet
                    try {
                        paivitaKVliite(p.getId(), kieli, counter);
                    } catch (RuntimeException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            counter++;
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    private void paivitaDokumentti(Long perusteId, Kieli kieli, Suoritustapakoodi koodi, int counter) {

        TransactionTemplate template = new TransactionTemplate(tm);

        template.execute(status -> {

            // Haetaan peruste
            Peruste p = perusteRepository.findOne(perusteId);

            // Haetaan uusin dokumentti
            DokumenttiDto latest = findLatest(p.getId(), kieli, koodi,
                    GeneratorVersion.UUSI);

            // Jos uusin dokumentti on "vanhentunut" luodaan uusi tilalle.
            if (latest == null || latest.getAloitusaika() == null
                    || latest.getAloitusaika().before(p.getGlobalVersion().getAikaleima())) {
                LOG.debug(String.format("%04d", counter)
                        + " Aloitetaan perusteelle " + "(" + p.getId() + ", " + koodi
                        + ", " + kieli + ")" + " uuden dokumentin luonti.");
                try {
                    DokumenttiDto createDtoFor = createDtoFor(
                            p.getId(),
                            kieli,
                            koodi,
                            GeneratorVersion.UUSI
                    );
                    setStarted(createDtoFor);
                    generateWithDtoSynchronous(createDtoFor);

                    return true;

                } catch (DokumenttiException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } else {
                LOG.debug(String.format("%04d", counter)
                        + " Perusteesta " + "(" + p.getId() + ", " + koodi
                        + ", " + kieli + ")" + " on jo luotu uusi dokumentti.");
            }

            return false;

        });
    }

    @Transactional(propagation = Propagation.NEVER)
    private void paivitaKVliite(Long perusteId, Kieli kieli, int counter) {

        TransactionTemplate template = new TransactionTemplate(tm);

        template.execute(status -> {
            // Haetaan peruste
            Peruste p = perusteRepository.findOne(perusteId);

            // Haetaan uusin dokumentti
            DokumenttiDto latest = findLatest(p.getId(), kieli, null,
                    GeneratorVersion.KVLIITE);

            // Jos uusin dokumentti on "vanhentunut" luodaan uusi tilalle.
            if (latest != null && !DokumenttiTila.EI_OLE.equals(latest.getTila())
                    && (latest.getAloitusaika() == null || latest.getAloitusaika()
                    .before(p.getGlobalVersion().getAikaleima()))) {

                LOG.debug(String.format("%04d", counter)
                        + " Aloitetaan perusteelle " + "(" + p.getId() + ", " + kieli + ")"
                        + " uuden kvliite-dokumentin luonti.");
                try {
                    DokumenttiDto createDtoFor = createDtoFor(
                            p.getId(),
                            kieli,
                            p.getSuoritustavat().iterator().next().getSuoritustapakoodi(),
                            GeneratorVersion.KVLIITE
                    );
                    setStarted(createDtoFor);
                    generateWithDtoSynchronous(createDtoFor);

                    return true;

                } catch (DokumenttiException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

            } else {
                LOG.debug(String.format("%04d", counter)
                        + " Peruteen " + "(" + p.getId() + ", " + kieli + ")"
                        + " on jo luotu uusi kvliite-dokumentti.");
            }

            return false;
        });
    }

}
