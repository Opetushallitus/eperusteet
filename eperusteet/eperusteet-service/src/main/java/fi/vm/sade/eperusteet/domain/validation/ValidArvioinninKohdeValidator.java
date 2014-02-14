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

import fi.vm.sade.eperusteet.domain.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author teele1
 */
public class ValidArvioinninKohdeValidator implements ConstraintValidator<ValidArvioinninKohde, ArvioinninKohde>{

    @Override
    public void initialize(ValidArvioinninKohde constraintAnnotation) {
    }

    @Override
    public boolean isValid(ArvioinninKohde arvioinninKohde, ConstraintValidatorContext context) {
        if(arvioinninKohde == null || arvioinninKohde.getOsaamistasonKriteerit() == null || arvioinninKohde.getOsaamistasonKriteerit().size() < 1) {
            return true;
        }
                
        if(arvioinninKohde.getArviointiAsteikko() == null 
                || arvioinninKohde.getArviointiAsteikko().getOsaamistasot().size() != arvioinninKohde.getOsaamistasonKriteerit().size()) {
            return false;
        }
        
        for(OsaamistasonKriteeri kriteeri : arvioinninKohde.getOsaamistasonKriteerit()) {
            if(kriteeri.getOsaamistaso() == null || !osaamistasoExistsInArviointiasteikko(kriteeri.getOsaamistaso(), arvioinninKohde.getArviointiAsteikko())) {
                return false;
            }
        }
        
        return true;
    }

    private boolean osaamistasoExistsInArviointiasteikko(Osaamistaso targetOsaamistaso, ArviointiAsteikko arviointiAsteikko) {
        for(Osaamistaso osaamistaso : arviointiAsteikko.getOsaamistasot()) {
            if(osaamistaso.getId().equals(targetOsaamistaso.getId())) return true; 
        }
        return false;
    }
}
