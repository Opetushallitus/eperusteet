package fi.vm.sade.eperusteet.domain.validation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;

public abstract class ValidHtmlValidatorBase {

	private Whitelist whitelist;
	
	protected void setupValidator(ValidHtml constraintAnnotation) {
		whitelist = constraintAnnotation.whitelist().getWhitelist();
	}
	
	protected boolean isValid(TekstiPalanen palanen) {
		if(palanen != null && palanen.getTeksti() != null && !palanen.getTeksti().isEmpty()) {
			for(Kieli kieli : palanen.getTeksti().keySet()) {
				if(!Jsoup.isValid(palanen.getTeksti().get(kieli), whitelist)) {
					return false;
				}
			}
		}
		return true;
	}
}
