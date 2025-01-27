package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidArvioinninKohdeValidator implements ConstraintValidator<ValidArvioinninKohde, ArvioinninKohde> {

    @Override
    public void initialize(ValidArvioinninKohde constraintAnnotation) {
        //NOP
    }

    @Override
    public boolean isValid(ArvioinninKohde arvioinninKohde, ConstraintValidatorContext context) {
        if (arvioinninKohde == null || arvioinninKohde.getOsaamistasonKriteerit() == null || arvioinninKohde.getOsaamistasonKriteerit().isEmpty()) {
            return true;
        }

        if (arvioinninKohde.getArviointiAsteikko() == null
            || arvioinninKohde.getArviointiAsteikko().getOsaamistasot().size() != arvioinninKohde.getOsaamistasonKriteerit().size()) {
            return false;
        }

        for (OsaamistasonKriteeri kriteeri : arvioinninKohde.getOsaamistasonKriteerit()) {
            if (kriteeri.getOsaamistaso() == null || !osaamistasoExistsInArviointiasteikko(kriteeri.getOsaamistaso(), arvioinninKohde.getArviointiAsteikko())) {
                return false;
            }
        }

        return true;
    }

    private boolean osaamistasoExistsInArviointiasteikko(Osaamistaso targetOsaamistaso, ArviointiAsteikko arviointiAsteikko) {
        for (Osaamistaso osaamistaso : arviointiAsteikko.getOsaamistasot()) {
            if (osaamistaso.getId().equals(targetOsaamistaso.getId())) {
                return true;
            }
        }
        return false;
    }
}
