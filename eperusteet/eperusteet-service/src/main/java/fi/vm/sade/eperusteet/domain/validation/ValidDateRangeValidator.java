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

package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import java.lang.reflect.Method;
import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author harrik
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidateDateRange, Perusteprojekti> {
    private String start;
    private String end;

    private static final Logger LOG = LoggerFactory.getLogger(ValidDateRangeValidator.class);
    
    @Override
    public void initialize(ValidateDateRange validateDateRange) {
        start = validateDateRange.start();
        end = validateDateRange.end();
    }

    @Override
    public boolean isValid(Perusteprojekti perusteprojekti, ConstraintValidatorContext cvc) {
        try {
            Class clazz = perusteprojekti.getClass();
            Date startDate = null;
            Method startGetter = clazz.getMethod(getAccessorMethodName(start), new Class[0]);
            Object startGetterResult = startGetter.invoke(perusteprojekti, null);
            
            if (startGetterResult == null) {
                return true;
            }
            if (startGetterResult instanceof Date){
                startDate = (Date) startGetterResult;
            }
            
            Date endDate = null;
            Method endGetter = clazz.getMethod(getAccessorMethodName(end), new Class[0]);
            Object endGetterResult = endGetter.invoke(perusteprojekti, null);
            if (endGetterResult == null){
                return true;
            }
            if (endGetterResult instanceof Date){
                endDate = (Date) endGetterResult;
            }
            return (startDate.before(endDate));           
        } catch (Throwable e) {
            LOG.error("ValidDateRangeValidator virhe", e);
        }

        return false;
    }
    
    private String getAccessorMethodName(String property){
        StringBuilder builder = new StringBuilder("get");
        builder.append(Character.toUpperCase(property.charAt(0))); 
        builder.append(property.substring(1));
        return builder.toString();
    }
}
