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
        SIMPLIFIED(Whitelist.none().addTags("p", "strong", "em", "i", "s", "ol", "li", "ul")),
        NORMAL(getNormalWhiteList()),
        NORMAL_PDF(getNormalWhiteList().removeAttributes("a", "routenode"));

        private final Whitelist whitelist;

        WhitelistType(Whitelist whitelist) {
            this.whitelist = whitelist;
        }

        public Whitelist getWhitelist() {
            return whitelist;
        }

        private static Whitelist getNormalWhiteList() {
            return Whitelist.none()
                    .addTags("p", "span", "strong", "em", "i", "s", "ol", "li", "ul", "blockquote", "table", "caption",
                            "tbody", "tr", "td", "hr", "pre", "th", "thead", "a", "abbr", "comment", "figcaption", "br")
                    .addAttributes("table", "align", "border", "cellpadding", "cellspacing", "style", "summary")
                    .addAttributes("th", "scope", "colspan", "rowspan", "style")
                    .addAttributes("td", "colspan", "rowspan", "style", "data-colwidth")
                    .addAttributes("a", "href", "target", "rel", "routenode")
                    .addAttributes("img", "data-uid", "src", "alt", "height", "width", "style", "figcaption")
                    .addAttributes("abbr", "data-viite")
                    .addAttributes("figure", "class")
                    .addAttributes("span", "kommentti", "class");
        }
    }
}
