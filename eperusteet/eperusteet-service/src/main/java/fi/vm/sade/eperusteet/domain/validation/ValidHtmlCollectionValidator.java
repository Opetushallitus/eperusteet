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
