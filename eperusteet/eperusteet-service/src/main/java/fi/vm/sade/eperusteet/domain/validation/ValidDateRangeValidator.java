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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author harrik
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidateDateRange, Object> {

    private static final Class<?>[] EMPTY_TYPE_ARRAY = new Class<?>[0];
    private String startMethodName;
    private String endMethodName;

    private static final Logger LOG = LoggerFactory.getLogger(ValidDateRangeValidator.class);

    @Override
    public void initialize(ValidateDateRange validateDateRange) {
        startMethodName = getAccessorMethodName(validateDateRange.start());
        endMethodName = getAccessorMethodName(validateDateRange.end());
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext cvc) {
        Date startDate = invokeDateGetter(obj, startMethodName);
        Date endDate = invokeDateGetter(obj, endMethodName);
        if (startDate == null || endDate == null) {
            return true;
        }
        return (startDate.before(endDate));
    }

    private static Date invokeDateGetter(Object obj, String methodName) {
        try {
            return (Date) obj.getClass().getMethod(methodName, EMPTY_TYPE_ARRAY).invoke(obj, (Object) null);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
            throw new IllegalArgumentException("Unable to access " + obj.getClass().getName() + "#" + methodName, e);
        }
    }

    private static String getAccessorMethodName(String property) {
        StringBuilder builder = new StringBuilder("get");
        builder.append(Character.toUpperCase(property.charAt(0)));
        builder.append(property.substring(1));
        return builder.toString();
    }
}
