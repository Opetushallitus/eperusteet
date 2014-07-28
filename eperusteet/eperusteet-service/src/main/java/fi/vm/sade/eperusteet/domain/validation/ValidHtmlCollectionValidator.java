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

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;

public class ValidHtmlCollectionValidator extends ValidHtmlValidatorBase implements
    ConstraintValidator<ValidHtml, Collection<TekstiPalanen>> {

    @Override
    public void initialize(ValidHtml constraintAnnotation) {
        setupValidator(constraintAnnotation);
    }

    @Override
    public boolean isValid(Collection<TekstiPalanen> value, ConstraintValidatorContext context) {
        if (value != null) {
            for (TekstiPalanen palanen : value) {
                if (!isValid(palanen)) {
                    return false;
                }
            }
        }
        return true;
    }
}
