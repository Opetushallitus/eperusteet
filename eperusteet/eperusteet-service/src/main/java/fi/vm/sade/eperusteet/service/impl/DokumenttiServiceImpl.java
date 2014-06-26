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
import fi.vm.sade.eperusteet.docgen.DokumenttiBuilder;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PerusteRepository perusteRepository;

    @Override
    @Transactional(readOnly = true)
    public void generateWithToken(long id, String token, Kieli kieli){
        LOG.debug("generate with token {},{}", id, token);

        try {
            File fout = getTmpFile(token);
            fout.createNewFile(); // so that tell whether it exists if queried
            File finalFile = getFinalFile(token);
            byte[] doc = generateFor(id, kieli);
            try (FileOutputStream fos = new FileOutputStream(fout)) {
                LOG.debug("dumping to file {}", fout.getAbsolutePath());
                fos.write(doc);
            }

            LOG.debug("renaming file to {}", finalFile.getAbsolutePath());
            fout.renameTo(finalFile);

        } catch (IOException ex) {
            LOG.error("IOException when writing doc: {}", ex);
        }
    }

    @Override
    public String getNewTokenFor(long id) {
        return new StringBuilder()
                .append("peruste_")
                .append(id)
                .append("_")
                .append(new Date().getTime())
                .toString();
    }

    @Override
    public byte[] getWithToken(String token) {
        File f = getFinalFile(token);
        try {
            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(f)) {
                baos = new ByteArrayOutputStream();
                IOUtils.copy(fis, baos);
            }
            baos.close();
            return baos.toByteArray();
        } catch (FileNotFoundException ex) {
            LOG.debug("{} does not exist", f.getAbsolutePath());
            return null;
        } catch (IOException ex) {
            LOG.error("IOException when copying: {}", ex);
            return null;
        }
    }

    @Override
    public DokumenttiDto query(String token) {
        boolean tmpExists = getTmpFile(token).exists();
        boolean finalExists = getFinalFile(token).exists();

        DokumenttiDto dto = new DokumenttiDto();
        dto.setToken(token);
        if (finalExists) {
            dto.setTila(DokumenttiDto.Tila.VALMIS);
        } else if (tmpExists) {
            dto.setTila(DokumenttiDto.Tila.LUODAAN);
        } else {
            dto.setTila(DokumenttiDto.Tila.EI_OLE);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateFor(long id, Kieli kieli) {

        LOG.info("generateFor: id {}", id);

        Peruste peruste = perusteRepository.findOne(id);

        DokumenttiBuilder builder = new DokumenttiBuilder(peruste, kieli);

        try {
            String xmlpath = builder.generateXML();
            LOG.debug("Temporary xml file: \n{}", xmlpath);
            // we could also use
            //String style = "file:///full/path/to/docbookstyle.xsl";
            String style = "res:docgen/docbookstyle.xsl";

            //PDFRenderer r = PDFRenderer.create(xmlpath, style);
            PerustePDFRenderer r = new PerustePDFRenderer().xml(xmlpath).xsl(style);
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

        } catch (Docbook4JException | IOException | TransformerException | ParserConfigurationException ex) {
            LOG.error(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private File getTmpFile(String token) {
        String tmppath =  getFilenameBase(token) + ".tmp";
        return new File(tmppath);
    }

    private File getFinalFile(String token) {
        String finalpath = getFilenameBase(token) + ".pdf";
        return new File(finalpath);
    }

    private String getFilenameBase(String token) {
        String sane = token.replace("/", "");
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.separator + sane;
    }
}
