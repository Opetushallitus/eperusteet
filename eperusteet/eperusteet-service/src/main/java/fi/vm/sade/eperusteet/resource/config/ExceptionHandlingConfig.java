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

package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 *
 * @author teele1
 */
@ControllerAdvice
public class ExceptionHandlingConfig extends ResponseEntityExceptionHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlingConfig.class);
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionExceptions(TransactionSystemException e, WebRequest request) {
        if(e.getRootCause() != null && e.getRootCause() instanceof ConstraintViolationException) {
            return handleExceptionInternal((ConstraintViolationException) e.getRootCause(), null, new HttpHeaders(), 
                    HttpStatus.BAD_REQUEST, request);
        } else {
            return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex ,HttpHeaders headers, HttpStatus status, 
            WebRequest request) {
        if(ex.getRootCause() != null && ex.getRootCause() instanceof UnrecognizedPropertyException) {
            return handleExceptionInternal((UnrecognizedPropertyException) ex.getRootCause(), null, headers, status, request);
        } else {
            return handleExceptionInternal(ex, null, headers, status, request);
        }
    }
    
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("koodi", status.value());
        
        if(ex instanceof BindException) {
            map.put("syy", "Virhe datan kytkemisessä.");
        }else if(ex instanceof ConversionNotSupportedException) {
            map.put("syy", "Datamuunnos ei ole tuettu.");
        }else if(ex instanceof HttpMediaTypeNotAcceptableException) {
            map.put("syy", "Mediatyyppi ei ole hyväksytty.");
        }else if(ex instanceof HttpMediaTypeNotSupportedException) {
            map.put("syy", "Mediatyyppi ei ole tuettu.");
        }else if(ex instanceof HttpMessageNotWritableException) {
            map.put("syy", "Http-viestiä ei pystytty kirjoittamaan.");
        }else if(ex instanceof HttpRequestMethodNotSupportedException) {
            map.put("syy", "Palvelin ei pystynyt käsittelemään http-pyyntöä.");
        }else if(ex instanceof MethodArgumentNotValidException) {
            map.put("syy", "Palvelin ei pystynyt käsittelemään http-pyyntöä.");
        }else if(ex instanceof MissingServletRequestParameterException) {
            map.put("syy", "Pyynnöstä puuttui parametri, eikä sitä voitu tästä syystä käsitellä.");
        }else if(ex instanceof MissingServletRequestPartException) {
            map.put("syy", "Pyynnöstä puuttui osa, eikä sitä voitu tästä syystä käsitellä.");
        }else if(ex instanceof NoSuchRequestHandlingMethodException) {
            map.put("syy", "Palvelimelta ei löytynyt http-pyynnölle käsittelijää.");
        }else if(ex instanceof TypeMismatchException) {
            map.put("syy", "Tyypin yhteensopivuusongelma.");
        }else if(ex instanceof TransactionSystemException) {
            map.put("syy", "Datan käsittelyssä tapahtui odottamaton virhe.");
        }else if(ex instanceof UnrecognizedPropertyException) {
            map.put("syy", "Dataa ei pystytty käsittelemään. Lähetetyssä datassa esiintyi tuntematon kenttä \"" 
                    + ((UnrecognizedPropertyException) ex).getPropertyName() + "\"");
        }else if(ex instanceof ConstraintViolationException) {
            List<String> reasons = new ArrayList<>();
            for(ConstraintViolation constraintViolation : ((ConstraintViolationException) ex).getConstraintViolations()) {
                reasons.add(constraintViolation.getMessage());
            }
            map.put("syy", reasons);
        }else {
            LOG.error("Creating common error response for exception", ex);
            map.put("syy", "Sovelluspalvelimessa tapahtui odottamaton virhe");
        }
        
        return super.handleExceptionInternal(ex, map, headers, status, request);
    }
}
