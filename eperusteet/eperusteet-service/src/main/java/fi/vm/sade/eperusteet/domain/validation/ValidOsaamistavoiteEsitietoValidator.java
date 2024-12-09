package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidOsaamistavoiteEsitietoValidator implements ConstraintValidator<ValidOsaamistavoiteEsitieto, Osaamistavoite> {

    @Override
    public void initialize(ValidOsaamistavoiteEsitieto a) {
    }

    @Override
    public boolean isValid(Osaamistavoite osaamistavoite, ConstraintValidatorContext cvc) {
        if (osaamistavoite.getEsitieto() != null) {
            return osaamistavoite.getEsitieto() != osaamistavoite &&
                   osaamistavoite.getEsitieto().isPakollinen() &&
                   osaamistavoite.getEsitieto().getEsitieto() == null;
        }
        return true;
    }

}
