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

import fi.vm.sade.eperusteet.resource.util.CacheControl;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author jhyoty
 */
public class CacheHeaderInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CacheHeaderInterceptor.class);

    public CacheHeaderInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (!returnsResponseEntity(handlerMethod)) {
                //jos palautetaan ResponsEntity, annetaan mahdollisuus muokata vastausta sitä kautta
                CacheControl cc = handlerMethod.getMethodAnnotation(CacheControl.class);
                long date = 0;
                String cacheControl = CacheControls.PRIVATE_NOT_CACHEABLE;
                if (cc != null) {
                    date = (new Date().getTime()) + cc.age() * 1000;
                    cacheControl = CacheControls.buildCacheControl(cc);
                }
                response.setDateHeader(com.google.common.net.HttpHeaders.EXPIRES, date);
                response.setHeader(com.google.common.net.HttpHeaders.CACHE_CONTROL, cacheControl);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws
        Exception {
        //Posthandlessa ei voi enää muokata vastausta
        //@see CacheHeadersAspect
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
        Exception {
    }

    private static boolean returnsResponseEntity(HandlerMethod handlerMethod) {
        return !handlerMethod.isVoid()
            && ResponseEntity.class.isAssignableFrom(handlerMethod.getReturnType().getParameterType());
    }

}
