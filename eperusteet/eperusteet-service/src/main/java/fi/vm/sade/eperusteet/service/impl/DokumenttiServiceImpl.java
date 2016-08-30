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
package fi.vm.sade.eperusteet.service.impl;

import com.google.code.docbook4j.Docbook4JException;
import com.google.code.docbook4j.renderer.PerustePDFRenderer;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.DokumenttiService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.NoSuchMessageException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jussini
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiServiceImpl.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    DokumenttiBuilderService builder;

    @Value("/docgen/fop.xconf")
    private String fopConfig;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    @IgnorePerusteUpdateCheck
    public DokumenttiDto createDtoFor(long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi) {

        String name = SecurityUtil.getAuthenticatedPrincipal().getName();
        Dokumentti dokumentti = new Dokumentti();
        dokumentti.setTila(DokumenttiTila.EI_OLE);
        dokumentti.setKieli(kieli);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(name);
        dokumentti.setPerusteId(id);
        dokumentti.setSuoritustapakoodi(suoritustapakoodi);

        Peruste peruste = perusteRepository.findOne(id);
        if (peruste != null) {
            Dokumentti saved = dokumenttiRepository.save(dokumentti);
            return mapper.map(saved, DokumenttiDto.class);
        } else {
            dokumentti.setTila(DokumenttiTila.EPAONNISTUI);
            // TODO: localize
            dokumentti.setVirhekoodi(DokumenttiVirhe.PERUSTETTA_EI_LOYTYNYT);
            return mapper.map(dokumentti, DokumenttiDto.class);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi) {
        Sort sort = new Sort(Sort.Direction.DESC, "valmistumisaika");
        List<Dokumentti> documents = dokumenttiRepository.findByPerusteIdAndKieliAndTilaAndSuoritustapakoodi(id, kieli, DokumenttiTila.VALMIS, suoritustapakoodi, sort);
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
    @Transactional
    @IgnorePerusteUpdateCheck
    public void generateWithDto(DokumenttiDto dto) {
        LOG.debug("generate with dto {}", dto);

        Dokumentti doc = dokumenttiRepository.findById(dto.getId());

        //TODO pitäisi tarkistaa että luonti ei ole jo käynnissä
        try {
            byte[] data = generateFor(dto);

            doc.setData(data);
            doc.setTila(DokumenttiTila.VALMIS);
            doc.setValmistumisaika(new Date());
            dokumenttiRepository.save(doc);

        }
        catch (NoSuchMessageException ex) {
           LOG.error("Exception during document generation:", ex);
           doc.setTila(DokumenttiTila.EPAONNISTUI);
           doc.setVirhekoodi(DokumenttiVirhe.TUNTEMATON_LOKALISOINTI);
           dokumenttiRepository.save(doc);
        }
        catch (TransformerException | ParserConfigurationException | Docbook4JException | IOException | RuntimeException ex) {
            LOG.error("Exception during document generation:", ex);
            doc.setTila(DokumenttiTila.EPAONNISTUI);
            doc.setVirhekoodi(DokumenttiVirhe.TUNTEMATON);
            dokumenttiRepository.save(doc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public byte[] get(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id);

        if (dokumentti != null) {
            Peruste peruste = perusteRepository.findOne(dokumentti.getPerusteId());

            String name = SecurityUtil.getAuthenticatedPrincipal().getName();

            if (name.equals("anonymousUser") && !peruste.getTila().equals(PerusteTila.VALMIS)) {
                return null;
            }

            return dokumentti.getData();
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public Long getDokumenttiId(Long perusteId, Kieli kieli) {
        List<Long> dokumenttiIds = dokumenttiRepository
                .findLatest(perusteId, kieli, DokumenttiTila.VALMIS);
        if (!dokumenttiIds.isEmpty()) {
            return dokumenttiIds.get(0);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    @IgnorePerusteUpdateCheck
    public void setStarted(DokumenttiDto dto) {
        Dokumentti doc = dokumenttiRepository.findById(dto.getId());
        doc.setTila(DokumenttiTila.LUODAAN);
        dokumenttiRepository.save(doc);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public DokumenttiDto query(Long id) {
        Dokumentti findById = dokumenttiRepository.findById(id);
        return mapper.map(findById, DokumenttiDto.class);
    }

    private byte[] generateFor(DokumenttiDto dto) throws IOException, TransformerException, ParserConfigurationException, Docbook4JException {

        Peruste peruste = perusteRepository.findOne(dto.getPerusteId());
        Kieli kieli = dto.getKieli();
        Suoritustapakoodi suoritustapakoodi = dto.getSuoritustapakoodi();

        String xmlpath = builder.generateXML(peruste, kieli, suoritustapakoodi);
        LOG.debug("Temporary xml file: \n{}", xmlpath);
        String style = "res:docgen/docbookstyle.xsl";

        PerustePDFRenderer r = new PerustePDFRenderer().xml(xmlpath).xsl(style);
        String fontDir = servletContext.getRealPath("WEB-INF/classes/docgen/fonts");
        r.setBaseFontDirectory(fontDir);
        r.setFopConfig(fopConfig);
        r.parameter("l10n.gentext.language", kieli.toString());

        // rendataan data ja otetaan kopio datasta.
        InputStream is = r.render();
        byte[] copy = IOUtils.toByteArray(is);
        is.close();

        // koitetaan puljata date-kenttä kuntoon fopin jäljiltä, mutta jos se
        // mistään syystä heittää poikkeuksen, palautetaan alkuperäinen data
        byte[] toReturn;
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

       return toReturn;
    }

    /*
      XMP-speksin mukaan dc:date arvot on oltava rdf:Seq -elementin sisällä,
      mutta FOP ei laita Seqiä, mikäli dateja on vain yksi. Tämän takia
      dokumentti ei läpäise validointia.

      fixMetadata korjaa tämän syötteenä saadusta pdf-dokumentista.
    */
    private InputStream fixMetadata(InputStream pdf) throws FileNotFoundException,
            IOException, ParserConfigurationException, SAXException,
            TransformerConfigurationException, TransformerException,
            COSVisitorException
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
                PDMetadata newMetadata = new PDMetadata(document, newXMPData, false);
                catalog.setMetadata(newMetadata);

                // tallennetaan modattu pdf-dokumentti
                ByteArrayOutputStream saved = new ByteArrayOutputStream();
                document.save(saved);

                return new ByteArrayInputStream(saved.toByteArray());
            }
        }
    }
}
