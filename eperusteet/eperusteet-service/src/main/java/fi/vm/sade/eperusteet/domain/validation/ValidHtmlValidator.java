package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Kieli;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Map;

public class ValidHtmlValidator extends ValidHtmlValidatorBase implements ConstraintValidator<ValidHtml, TekstiPalanen> {

	@Override
	public void initialize(ValidHtml constraintAnnotation) {
		setupValidator(constraintAnnotation);
	}

	@Override
	public boolean isValid(TekstiPalanen value, ConstraintValidatorContext context) {
		return isValid(value);
	}

    public static boolean isValid(TekstiPalanen tekstiPalanen, Safelist whitelist) {
        if (tekstiPalanen != null) {
            Map<Kieli, String> tekstit = tekstiPalanen.getTeksti();
            if (tekstit != null) {
                return tekstit.values().stream()
                        .allMatch(teksti -> Jsoup.isValid(teksti, whitelist));
            }
        }
        return true;
    }
}
