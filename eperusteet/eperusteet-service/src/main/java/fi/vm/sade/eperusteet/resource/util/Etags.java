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
package fi.vm.sade.eperusteet.resource.util;

import org.springframework.http.HttpHeaders;

/**
 *
 * @author jhyoty
 */
public final class Etags {

    private static final String WEAK_ETAG_PREFIX = "\"W/";

    private Etags() {
        //apuluokka
    }

    public static Integer revisionOf(String eTag) {
        if (eTag == null) {
            return null;
        }
        if (eTag.startsWith(WEAK_ETAG_PREFIX)) {
            return Integer.parseInt(eTag.substring(3, eTag.length() - 1));
        }
        throw new IllegalArgumentException("virheellinen eTag");
    }

    public static HttpHeaders eTagHeader(Integer revision) {
        return addETag(new HttpHeaders(), revision);
    }

    public static HttpHeaders addETag(HttpHeaders headers, Integer revision) {
        if (revision != null) {
            headers.setETag(wrap(String.valueOf(revision)));
        }
        return headers;
    }

    private static String wrap(String value) {
        return WEAK_ETAG_PREFIX + value + "\"";
    }

}
