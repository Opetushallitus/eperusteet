package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;

public class ValidMaxLengthValidator extends ValidHtmlValidatorBase implements ConstraintValidator<ValidMaxLength, TekstiPalanen>  {
    private int pituus = 512;

    @Override
    public void initialize(ValidMaxLength validator) {
        this.pituus = validator.pituus();
    }

    @Override
    public boolean isValid(TekstiPalanen tekstiPalanen, ConstraintValidatorContext constraintValidatorContext) {
        if (tekstiPalanen == null) {
            return true;
        }

        Map<Kieli, String> tekstit = tekstiPalanen.getTeksti();
        for (Map.Entry<Kieli, String> teksti : tekstit.entrySet()) {
            if (!StringUtils.isEmpty(teksti.getValue()) && teksti.getValue().length() >= this.pituus) {
                return false;
            }
        }
        return true;
    }
}
