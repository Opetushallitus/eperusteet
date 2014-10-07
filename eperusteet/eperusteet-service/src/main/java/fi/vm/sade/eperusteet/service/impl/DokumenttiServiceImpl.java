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
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.service.DokumenttiService;
import com.google.code.docbook4j.renderer.PerustePDFRenderer;
import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.DokumenttiVirhe;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jussini
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiServiceImpl.class);

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    DokumenttiBuilderService builder;

    @Value("${fi.vm.sade.eperusteet.fop_directory:}")
    private String fopDirectory;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto createDtoFor(long id, Kieli kieli) {

        String name = SecurityUtil.getAuthenticatedPrincipal().getName();
        Dokumentti dokumentti = new Dokumentti();
        dokumentti.setTila(DokumenttiTila.EI_OLE);
        dokumentti.setKieli(kieli);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(name);
        dokumentti.setPerusteId(id);

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
    public DokumenttiDto findLatest(Long id, Kieli kieli) {
        Sort sort = new Sort(Sort.Direction.DESC, "valmistumisaika");
        List<Dokumentti> documents = dokumenttiRepository.findByPerusteIdAndKieliAndTila(id, kieli, DokumenttiTila.VALMIS, sort);
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

        } catch (TransformerException | ParserConfigurationException | Docbook4JException | IOException | RuntimeException ex) {
            LOG.error("Exception during document generation:", ex);
            doc.setTila(DokumenttiTila.EPAONNISTUI);
            doc.setVirhekoodi(DokumenttiVirhe.TUNTEMATON);
            dokumenttiRepository.save(doc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] get(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id);
        if (dokumentti != null) {
            return dokumentti.getData();
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void setStarted(DokumenttiDto dto) {
        Dokumentti doc = dokumenttiRepository.findById(dto.getId());
        doc.setTila(DokumenttiTila.LUODAAN);
        dokumenttiRepository.save(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DokumenttiDto query(Long id) {
        Dokumentti findById = dokumenttiRepository.findById(id);
        return mapper.map(findById, DokumenttiDto.class);
    }

    private byte[] generateFor(DokumenttiDto dto) throws IOException, TransformerException, ParserConfigurationException, Docbook4JException {

        Peruste peruste = perusteRepository.findOne(dto.getPerusteId());
        Kieli kieli = dto.getKieli();

        String xmlpath = builder.generateXML(peruste, kieli);
        LOG.debug("Temporary xml file: \n{}", xmlpath);
        // we could also use
        //String style = "file:///full/path/to/docbookstyle.xsl";
        String style = "res:docgen/docbookstyle.xsl";

        //PDFRenderer r = PDFRenderer.create(xmlpath, style);
        PerustePDFRenderer r = new PerustePDFRenderer().xml(xmlpath).xsl(style);
        r.setFopDirectory(fopDirectory);
        r.parameter("l10n.gentext.language", kieli.toString());

        InputStream is;
        is = r.render();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // TODO, maybe use ioutils as it seems to be project dependency anyways
        byte[] buf = new byte[2048];
        int n = -1;
        while ((n = is.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        baos.close();
        is.close();

        return baos.toByteArray();
    }

}
