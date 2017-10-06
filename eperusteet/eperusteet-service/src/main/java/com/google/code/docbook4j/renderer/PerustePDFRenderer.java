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
package com.google.code.docbook4j.renderer;

import com.google.code.docbook4j.Docbook4JException;
import com.google.code.docbook4j.FileObjectUtils;
import fi.vm.sade.eperusteet.service.dokumentti.impl.DokumenttiEventListener;
import fi.vm.sade.eperusteet.service.util.PerusteXslURIResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.fop.apps.*;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author jussi
 */
public class PerustePDFRenderer extends FORenderer<PerustePDFRenderer> {

    private File fopConfig;

    @Override
    protected String getMimeType() {
        return MimeConstants.MIME_PDF;
    }

    @Override
    protected Transformer createTransformer(FileObject xmlSource, FileObject xslStylesheet)
            throws TransformerConfigurationException, IOException {

        TransformerFactory transformerFactory = createTransformerFactory();
        if (xslStylesheet != null) {
            transformerFactory.setURIResolver(new PerusteXslURIResolver());
        }
        FileObject xsl = xslStylesheet != null ? xslStylesheet : getDefaultXslStylesheet();

        Source source = new StreamSource(xsl.getContent().getInputStream(), xsl.getURL().toExternalForm());
        Transformer transformer = transformerFactory.newTransformer(source);
        transformer.setParameter("use.extensions", "1");
        transformer.setParameter("callout.graphics", "0");
        transformer.setParameter("callout.unicode", "1");
        transformer.setParameter("callouts.extension", "1");
        transformer.setParameter("base.dir", xmlSource.getParent().getURL().toExternalForm());
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
        }

        return transformer;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected FileObject postProcess(final FileObject xmlSource, final FileObject xslSource,
                                     final FileObject xsltResult) throws Docbook4JException {
        FileObject target = null;

        try {
            FopFactory fopFactory = FopFactory.newInstance(fopConfig);

            FOUserAgent userAgent = fopFactory.newFOUserAgent();
            userAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
            userAgent.getEventBroadcaster().addEventListener(new DokumenttiEventListener());

            String tmpPdf = "tmp://" + UUID.randomUUID().toString();
            target = FileObjectUtils.resolveFile(tmpPdf);
            target.createFile();

            Fop fop = fopFactory.newFop(getMimeType(), userAgent, target.getContent().getOutputStream());


            Source src = new StreamSource(xsltResult.getContent().getInputStream());
            Result res = new SAXResult(fop.getDefaultHandler());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setParameter("use.extensions", "1");
            transformer.setParameter("fop.extensions", "0");
            transformer.setParameter("fop1.extensions", "1");

            transformer.transform(src, res);

        } catch (FileSystemException e) {
            throw new Docbook4JException("Error create filesystem manager!", e);
        } catch (TransformerException | FOPException e) {
            throw new Docbook4JException("Error transforming fo to pdf!", e);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            FileObjectUtils.closeFileObjectQuietly(target);
        }

        return target;
    }

    public void setFopConfig(File fopConfig) {
        this.fopConfig = fopConfig;
    }
}
