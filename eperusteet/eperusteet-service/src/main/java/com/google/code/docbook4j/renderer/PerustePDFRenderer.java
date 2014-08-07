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

import fi.vm.sade.eperusteet.service.util.PerusteXslURIResolver;
import java.io.IOException;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.vfs2.FileObject;
import org.apache.fop.apps.MimeConstants;

/**
 *
 * @author jussi
 */
public class PerustePDFRenderer extends FORenderer<PerustePDFRenderer>{
        
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
}
