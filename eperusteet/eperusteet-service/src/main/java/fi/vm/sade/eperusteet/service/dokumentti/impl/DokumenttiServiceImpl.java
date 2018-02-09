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

import com.google.code.docbook4j.Docbook4JException;
import com.google.code.docbook4j.renderer.PerustePDFRenderer;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.internal.PdfService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.preflight.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
        if (version != null) {
            documents = new ArrayList<>(dokumenttiRepository.findByPerusteIdAndKieliAndTilaAndGeneratorVersion(
                    id, kieli, DokumenttiTila.VALMIS, version, sort));
        }
        else {
            documents = new ArrayList<>(dokumenttiRepository
                    .findByPerusteIdAndKieliAndTilaAndSuoritustapakoodiAndGeneratorVersion(
                            id, kieli, DokumenttiTila.VALMIS, suoritustapakoodi, GeneratorVersion.UUSI, sort));
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

            if (activeProfile != "test") {
                throw new DokumenttiException(ex.getMessage(), ex);
            }
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
            throws IOException,TransformerException, ParserConfigurationException, Docbook4JException, SAXException {

        Peruste peruste = perusteRepository.findOne(dto.getPerusteId());
        Kieli kieli = dto.getKieli();
        Suoritustapakoodi suoritustapakoodi = dto.getSuoritustapakoodi();
        Dokumentti dokumentti = mapper.map(dto, Dokumentti.class);
        byte[] toReturn = null;
        ValidationResult result;
        GeneratorVersion version = dto.getGeneratorVersion();

        LOG.info("Generate PDF (perusteId=" + dto.getPerusteId() + ")");
        switch (version) {
            case VANHA:
                String xmlpath = builder.generateXML(peruste, kieli, suoritustapakoodi);
                LOG.debug("Temporary xml file: {}", xmlpath);
                String style = "res:docgen/docbookstyle.xsl";

                PerustePDFRenderer r = new PerustePDFRenderer().xml(xmlpath).xsl(style);
                r.setFopConfig(fopConfig.getFile());
                r.parameter("l10n.gentext.language", kieli.toString());

                // rendataan data ja otetaan kopio datasta.
                InputStream is = r.render();
                byte[] copy = IOUtils.toByteArray(is);
                is.close();

                // koitetaan puljata date-kenttä kuntoon fopin jäljiltä, mutta jos se
                // mistään syystä heittää poikkeuksen, palautetaan alkuperäinen data
                try {
                    byte[] fixedba;
                    try (InputStream fixed = fixMetadata(new ByteArrayInputStream(copy))) {
                        fixedba = IOUtils.toByteArray(fixed);
                    }
                    toReturn = fixedba;
                } catch (Exception ex) {
                    LOG.warn("Fixing the xmp date field failed, returning nonfixed. Error was: ", ex);
                    toReturn = copy;
                }

                break;
            case UUSI:
                Document doc = newBuilder.generateXML(peruste, dokumentti);
                toReturn = pdfService.xhtml2pdf(doc);

                // Validoidaan dokumnetti
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    LOG.info("PDF (peruste " + dto.getId() + ") is a valid PDF/A-1b file");
                } else {
                    LOG.warn("PDF (peruste " + dto.getId() + ") is not valid, error(s) :");
                    result.getErrorsList().forEach(error -> LOG
                            .warn(error.getErrorCode() + " : " + error.getDetails()));
                }

                break;
            case KVLIITE:
                doc = kvLiiteBuilderService.generateXML(peruste, kieli);
                toReturn = pdfService.xhtml2pdf(doc, version);

                // Validoi kvliite
                result = DokumenttiUtils.validatePdf(toReturn);
                if (result.isValid()) {
                    LOG.info("PDF (peruste " + dto.getId() + ") is a valid PDF/A-1b file");
                } else {
                    LOG.warn("PDF (peruste " + dto.getId() + ") is not valid, error(s) :");
                    result.getErrorsList().forEach(error -> LOG
                            .warn(error.getErrorCode() + " : " + error.getDetails()));
                }

                break;
            default:
                break;
        }
        return toReturn;
    }

    /*
      XMP-speksin mukaan dc:date arvot on oltava rdf:Seq -elementin sisällä,
      mutta FOP ei laita Seqiä, mikäli dateja on vain yksi. Tämän takia
      dokumentti ei läpäise validointia.

      fixMetadata korjaa tämän syötteenä saadusta pdf-dokumentista.
    */
    private InputStream fixMetadata(InputStream pdf) throws IOException, ParserConfigurationException,
            SAXException, TransformerException
    {
        try (InputStream xslresource = getClass().getClassLoader().getResourceAsStream("docgen/fopdate.xsl")) {

            try (PDDocument document = PDDocument.load(pdf)) {
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata metadata = catalog.getMetadata();

                // luetaan metadata-xml pdf:stä ja parsitaan se domiin
                InputStream metadataInputStream = metadata.createInputStream();
                SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
                DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
                fact.setNamespaceAware(true);
                DocumentBuilder bldr = fact.newDocumentBuilder();
                Document xml = bldr.parse(metadataInputStream);
                DOMSource source = new DOMSource(xml);

                // muljataan metadatan date-kentän sisältö xmp-speksin mukaisksi
                Templates templates = stf.newTemplates(new StreamSource(xslresource));
                TransformerHandler th = stf.newTransformerHandler(templates);
                ByteArrayOutputStream transformOut = new ByteArrayOutputStream();
                th.setResult(new StreamResult(transformOut));
                Transformer transformer = stf.newTransformer();
                transformer.transform(source, new SAXResult(th));

                // lopuks pistetään muokattu metadata takaisin pdf:ään
                InputStream newXMPData = new ByteArrayInputStream(transformOut.toByteArray());
                PDMetadata newMetadata = new PDMetadata(document, newXMPData);
                catalog.setMetadata(newMetadata);

                // tallennetaan modattu pdf-dokumentti
                ByteArrayOutputStream saved = new ByteArrayOutputStream();
                document.save(saved);

                return new ByteArrayInputStream(saved.toByteArray());
            }
        }
    }
}
