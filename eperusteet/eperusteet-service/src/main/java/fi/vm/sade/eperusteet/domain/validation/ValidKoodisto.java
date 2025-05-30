package fi.vm.sade.eperusteet.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {ValidKoodiValidator.class})
@Documented
public @interface ValidKoodisto {
    String[] koodisto() default "";
    String message() default "koodilla-vaara-koodisto";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
