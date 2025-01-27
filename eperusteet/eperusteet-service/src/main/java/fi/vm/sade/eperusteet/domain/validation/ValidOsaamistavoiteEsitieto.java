package fi.vm.sade.eperusteet.domain.validation;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidOsaamistavoiteEsitietoValidator.class)
@Documented
public @interface ValidOsaamistavoiteEsitieto {
    String message() default "Osaamistavoitteen esitieto voi osoittaa vain pakolliseen osaamistavoitteeseen.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
