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

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Lisää ResponseEntityyn Cache-Control ja Expires -headerit jos niitä ei ole erikseen lisätty.
 *
 * @author jhyoty
 */
@Aspect
@Component
public class CacheHeadersAspect {

    private static final Logger LOG = LoggerFactory.getLogger(CacheHeadersAspect.class);

    @Pointcut("execution(org.springframework.http.ResponseEntity fi.vm.sade.eperusteet.resource..*Controller.*(..))")
    public void controller() {
    }

    @Around("controller() && !@annotation(fi.vm.sade.eperusteet.resource.util.CacheControl)")
    public Object aroundResponse(ProceedingJoinPoint jp) throws Throwable {
        Object rv = jp.proceed();
        if (rv instanceof ResponseEntity) {
            return addCacheHeaders((ResponseEntity<?>) rv, -1, CacheControls.PRIVATE_NOT_CACHEABLE);
        } else {
            return rv;
        }
    }

    @Around("controller() && @annotation(cacheControl)")
    public Object aroundResponse(ProceedingJoinPoint jp, CacheControl cacheControl) throws Throwable {
        Object rv = jp.proceed();
        if (rv instanceof ResponseEntity) {
            return addCacheHeaders((ResponseEntity<?>) rv, cacheControl.age(), CacheControls.buildCacheControl(cacheControl));
        } else {
            return rv;
        }
    }

    private <T> Object addCacheHeaders(ResponseEntity<T> responseEntity, long age, String cacheControl) {
        if (responseEntity.getHeaders().getCacheControl() == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(responseEntity.getHeaders());
            if (age < 0) {
                headers.setExpires(0);
            } else {
                headers.setExpires((new Date().getTime()) + age * 1000);
            }
            headers.setCacheControl(cacheControl);
            return new ResponseEntity<>(responseEntity.getBody(), headers, responseEntity.getStatusCode());
        }
        return responseEntity;
    }
}
