package fi.vm.sade.eperusteet.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;

public class ValidHtmlValidator extends ValidHtmlValidatorBase implements ConstraintValidator<ValidHtml, TekstiPalanen>{

	@Override
	public void initialize(ValidHtml constraintAnnotation) {
		setupValidator(constraintAnnotation);
	}

	@Override
	public boolean isValid(TekstiPalanen value, ConstraintValidatorContext context) {
		return isValid(value);
	}
}
