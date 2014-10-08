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
import com.google.code.docbook4j.VfsURIResolver;
import fi.vm.sade.eperusteet.service.util.PerusteXslURIResolver;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author jussi
 */
public class PerustePDFRenderer extends FORenderer<PerustePDFRenderer>{

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PerustePDFRenderer.class);

    private String fopDirectory = "";

    @Override
    protected String getMimeType() {
        return MimeConstants.MIME_PDF;
    }

    @Override
    protected Transformer createTransformer(FileObject xmlSource,
            FileObject xslStylesheet) throws TransformerConfigurationException,
            IOException {

        TransformerFactory transformerFactory = createTransformerFactory();
        if (xslStylesheet != null) {
            transformerFactory.setURIResolver(new PerusteXslURIResolver());
        }
        FileObject xsl = xslStylesheet != null ? xslStylesheet
                : getDefaultXslStylesheet();

        Source source = new StreamSource(xsl.getContent().getInputStream(), xsl
                .getURL().toExternalForm());
        Transformer transformer = transformerFactory.newTransformer(source);

        transformer.setParameter("use.extensions", "1");
        transformer.setParameter("callout.graphics", "0");
        transformer.setParameter("callout.unicode", "1");
        transformer.setParameter("callouts.extension", "1");
        transformer.setParameter("base.dir", xmlSource.getParent().getURL()
                .toExternalForm());

        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
        }

        return transformer;
    }

    @Override
    protected FileObject postProcess(final FileObject xmlSource,
                                     final FileObject xslSource, final FileObject xsltResult)
            throws Docbook4JException {

        FileObject target = null;
        try {

            final FopFactory fopFactory = FopFactory.newInstance();
            // bleh, had to duplicate the full postProcess function just to add
            // this line :(
            enhanceFopFactory(fopFactory);

            final FOUserAgent userAgent = fopFactory.newFOUserAgent();
            userAgent.setBaseURL(xmlSource.getParent().getURL()
                    .toExternalForm());
            userAgent.setURIResolver(new VfsURIResolver());

            enhanceFOUserAgent(userAgent);

            String tmpPdf = "tmp://" + UUID.randomUUID().toString();
            target = FileObjectUtils.resolveFile(tmpPdf);
            target.createFile();

            Configuration configuration = createFOPConfig();
            if (configuration != null) {
                fopFactory.setUserConfig(configuration);
            }

            Fop fop = fopFactory.newFop(getMimeType(), userAgent, target
                    .getContent().getOutputStream());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity
            // transformer
            transformer.setParameter("use.extensions", "1");
            transformer.setParameter("fop.extensions", "0");
            transformer.setParameter("fop1.extensions", "1");

            Source src = new StreamSource(xsltResult.getContent()
                    .getInputStream());
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
            return target;

        } catch (FileSystemException e) {
            throw new Docbook4JException("Error create filesystem manager!", e);
        } catch (TransformerException e) {
            throw new Docbook4JException("Error transforming fo to pdf!", e);
        } catch (FOPException e) {
            throw new Docbook4JException("Error transforming fo to pdf!", e);
        } finally {

            FileObjectUtils.closeFileObjectQuietly(target);
        }

    }

    @Override
    protected Configuration createFOPConfig() {

        if (StringUtils.isEmpty(this.fopDirectory)) {
            LOG.warn("Fop directory not set, won't set configuration");
            return null;
        }

        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        try {
            File fopconfDir = new File(this.fopDirectory);
            File fopconf = new File(fopconfDir, "fop.xconf");
            LOG.info("Using fop conf file: {}", fopconf.getAbsolutePath());

            Configuration conf = builder.buildFromFile(fopconf.getAbsolutePath());

            return conf;
        } catch (SAXException | IOException | ConfigurationException ex) {
            LOG.error("", ex);
        }
        return null;
    }

    @Override
    protected void enhanceFOUserAgent(FOUserAgent agent) {
        if (StringUtils.isEmpty(this.fopDirectory)) {
            LOG.warn("Fop directory not set, won't use pdf/a mode");
        } else {
            agent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
        }
    }

    protected void enhanceFopFactory(FopFactory factory) {
        try {
            if (StringUtils.isNotEmpty(this.fopDirectory)) {
                factory.getFontManager().setFontBaseURL(this.fopDirectory);
            } else {
                LOG.warn("Fop directory not set, won't set font base url");
            }
        } catch (MalformedURLException ex) {
            LOG.error("", ex);
        }
    }

    public void setFopDirectory(String fopDirectory) {
        this.fopDirectory = fopDirectory;
    }
}
