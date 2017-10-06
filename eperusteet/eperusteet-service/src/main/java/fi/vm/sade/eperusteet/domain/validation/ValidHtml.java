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

import org.jsoup.safety.Whitelist;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {ValidHtmlValidator.class, ValidHtmlCollectionValidator.class})
@Documented
public @interface ValidHtml {

    String message() default "Teksti saa sisältää vain ennaltamääriteltyjä html-elementtejä";

    WhitelistType whitelist() default WhitelistType.NORMAL;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum WhitelistType {
        MINIMAL(Whitelist.none()),
        SIMPLIFIED(Whitelist.none().addTags("p", "strong", "em", "s", "ol", "li", "ul")),
        NORMAL(Whitelist.none()
                .addTags("p", "span", "strong", "em", "s", "ol", "li", "ul", "blockquote", "table", "caption",
                        "tbody", "tr", "td", "hr", "pre", "th", "thead", "a", "abbr")
                .addAttributes("table", "align", "border", "cellpadding", "cellspacing", "style", "summary")
                .addAttributes("th", "scope", "colspan", "rowspan", "style")
                .addAttributes("td", "colspan", "rowspan", "style")
                .addAttributes("a", "href", "target")
                .addAttributes("img", "data-uid", "src", "alt", "height", "width", "style")
                .addAttributes("abbr", "data-viite")
                .addAttributes("span", "class"));

        private Whitelist whitelist;

        WhitelistType(Whitelist whitelist) {
            this.whitelist = whitelist;
        }

        public Whitelist getWhitelist() {
            return whitelist;
        }
    }
}
