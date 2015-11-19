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

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 *
 * @author jhyoty
 */
public class CacheableResponse {

    public static <T> ResponseEntity<T> create(PerusteVersionDto perusteVersio, int age, Supplier<T> response) {
        if (perusteVersio == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return create(perusteVersio.getAikaleima(), age, response);
    }

    public static <T> ResponseEntity<T> create(Date modificationDate, int age, Supplier<T> response) {
        if (modificationDate == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        long time = modificationDate.getTime();
        ResponseEntity<T> cached = handleCache(modificationDate, time);
        if (cached != null) {
            return cached;
        }
        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> create(Revision rev, int age, Supplier<T> response) {
        if (rev == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!rev.equals(Revision.DRAFT)) {
            ResponseEntity<T> cached = handleCache(rev.getPvm(), rev.getNumero());
            if (cached != null) {
                return cached;
            }
        }
        T dto = response.get();

        if (!rev.equals(Revision.DRAFT)) {
            return respond(rev.getPvm(), rev.getNumero(), age, dto);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    private static <T> ResponseEntity<T> handleCache(Date lastModifiedAt, Number eTagNumber) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String eTag = request.getHeader("If-None-Match");
        long l = request.getDateHeader("If-Modified-Since");
        boolean notmodified = (eTag != null || l > 0);
        notmodified &= eTag == null || eTag.equals(Etags.eTagOf(eTagNumber));
        notmodified &= l == -1 || (lastModifiedAt.getTime() / 1000) == (l / 1000);
        if (notmodified) {
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache");
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }
        return null;
    }

    private static <T> ResponseEntity<T> respond(Date lastModifiedAt, Number eTagNumber, int age, T dto) {
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = Etags.eTagHeader(eTagNumber);
        headers.setCacheControl("public, max-age=" + age);
        headers.setLastModified(lastModifiedAt.getTime());
        return new ResponseEntity<>(dto, headers, HttpStatus.OK);
    }
}
