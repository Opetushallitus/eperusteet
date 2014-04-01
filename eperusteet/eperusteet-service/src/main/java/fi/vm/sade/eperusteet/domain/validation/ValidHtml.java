package fi.vm.sade.eperusteet.domain.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jsoup.safety.Whitelist;

@Target({ FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidHtmlValidator.class)
@Documented
public @interface ValidHtml {

	String message() default "Teksti saa sisältää vain ennaltamääriteltyjä html-elementtejä";
	WhitelistType whitelist() default WhitelistType.NORMAL;

    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default { };
    
    public enum WhitelistType {
    	MINIMAL(Whitelist.none()),
    	SIMPLIFIED(Whitelist.none().addTags("p","strong","em","s","ol","li","ul")),
    	NORMAL(Whitelist.none()
    			.addTags("p","strong","em","s","ol","li","ul","blockquote","table","caption","tbody","tr","td","hr","h1","h2","h3","pre")
    			.addAttributes("table", "align","border","cellpadding","cellspacing","style","summary"));
    	
    	private Whitelist whitelist;
    	
    	private WhitelistType(Whitelist whitelist) {
    		this.whitelist = whitelist;
    	}
    	
    	public Whitelist getWhitelist() {
    		return whitelist;
    	}
    }
}
