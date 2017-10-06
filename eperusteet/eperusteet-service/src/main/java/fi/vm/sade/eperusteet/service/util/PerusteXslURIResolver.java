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

package fi.vm.sade.eperusteet.service.util;

import com.google.code.docbook4j.FileObjectUtils;
import com.google.code.docbook4j.XslURIResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jussi
 */
public class PerusteXslURIResolver extends XslURIResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteXslURIResolver.class);

    private String docbookXslBase;

    @Override
    public Source resolve(String href, String base) throws TransformerException {

        if (href == null || href.trim().length() == 0)
            return null;

        if (docbookXslBase == null && href.startsWith("res:")
                && href.endsWith("docbook.xsl")) {
            try {
                docbookXslBase = FileObjectUtils.resolveFile(href).getParent()
                        .getURL().toExternalForm();
            } catch (FileSystemException e) {
                docbookXslBase = null;
            }
        }

        String normalizedBase = null;
        if (base != null) {
            try {
                normalizedBase = FileObjectUtils.resolveFile(base).getParent()
                        .getURL().toExternalForm();
            } catch (FileSystemException e) {
                normalizedBase = null;
            }
        }

        try {

            FileObject urlFileObject = FileObjectUtils.resolveFile(href,
                    normalizedBase);

            if (!urlFileObject.exists())
                throw new FileSystemException("File object not found: "
                        + urlFileObject);

            return new StreamSource(
                    fileObjectToInputStream(urlFileObject),
                    urlFileObject.getURL().toExternalForm());

        } catch (FileSystemException e) {

            // not exists for given base? try with docbook base...
            try {
                if (docbookXslBase != null) {
                    FileObject urlFileObject = FileObjectUtils.resolveFile(
                            href, docbookXslBase);
                    return new StreamSource(urlFileObject.getContent()
                            .getInputStream(), urlFileObject.getURL()
                            .toExternalForm());
                }

            } catch (FileSystemException e1) {
                // do nothing.
            }

            LOG.error("Error resolving href=" + href + " for base=" + base, e);
        }

        return null;

    }

    /**
     * Helper method to avoid leaking file handles. For some reason document
     * parsing leaves behind insane amount of open file descriptors. Reading
     * the file contents into buffer and returning stream into that seems to
     * help.
     * <p>
     * By gods this is ugly but at least we're not running into
     * too many open file -issues.
     **/
    private InputStream fileObjectToInputStream(FileObject urlFileObject)
            throws FileSystemException {
        InputStream is = urlFileObject.getContent().getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(is, baos);
            is.close();
        } catch (IOException ex) {
            LOG.error("{}", ex);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

}
