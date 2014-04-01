package fi.vm.sade.eperusteet.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;

public class ValidHtmlValidator implements ConstraintValidator<ValidHtml, TekstiPalanen>{

	private Whitelist whitelist;
	
	@Override
	public void initialize(ValidHtml constraintAnnotation) {
		whitelist = constraintAnnotation.whitelist().getWhitelist();
		
	}

	@Override
	public boolean isValid(TekstiPalanen value, ConstraintValidatorContext context) {
		if(value != null && value.getTeksti() != null && !value.getTeksti().isEmpty()) {
			for(Kieli kieli : value.getTeksti().keySet()) {
				if(!Jsoup.isValid(value.getTeksti().get(kieli), whitelist)) {
					return false;
				}
			}
		}
		return true;
	}

}
