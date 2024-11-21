package fi.vm.sade.eperusteet.domain.validation;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidArvioinninKohdeValidator.class)
@Documented
public @interface ValidArvioinninKohde {

    String message() default "Arvioinnin kohteella tulee olla arviointiasteikko ja kaikki "
            + "kohteen kriteerit tulee sitoa näihin arviointiasteikon osaamistasoihin.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
