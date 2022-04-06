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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public abstract class ValidHtmlValidatorBase {

	private Whitelist whitelist;
	private UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
	private EmailValidator emailValidator = EmailValidator.getInstance(true, true);

	protected void setupValidator(ValidHtml constraintAnnotation) {
		whitelist = constraintAnnotation.whitelist().getWhitelist();
	}

	protected boolean isValid(TekstiPalanen palanen) {
		if (palanen != null && palanen.getTeksti() != null && !palanen.getTeksti().isEmpty()) {
			for (Kieli kieli : palanen.getTeksti().keySet()) {
				if (!Jsoup.isValid(palanen.getTeksti().get(kieli), whitelist) || !isValidUrls(palanen.getTeksti().get(kieli))) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isValidUrls(String teksti) {
		Document doc = Jsoup.parse(teksti);
		Elements links = doc.select("a[href]");
		return links.stream().allMatch(link ->
				!link.attr("routenode").isEmpty()
						|| urlValidator.isValid(link.attr("abs:href"))
						|| emailValidator.isValid(link.attr("href").replace("mailto:", "")));
	}

}
