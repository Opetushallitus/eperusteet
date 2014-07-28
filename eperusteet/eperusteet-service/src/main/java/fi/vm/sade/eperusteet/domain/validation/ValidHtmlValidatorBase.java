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
