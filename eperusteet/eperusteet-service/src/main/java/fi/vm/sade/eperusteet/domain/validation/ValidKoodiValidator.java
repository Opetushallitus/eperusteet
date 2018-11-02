package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Koodi;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ValidKoodiValidator implements ConstraintValidator<ValidKoodisto, Koodi> {
    private String value = "";

    @Override
    public void initialize(ValidKoodisto constraintAnnotation) {
        this.value = constraintAnnotation.koodisto();
    }

    @Override
    public boolean isValid(Koodi value, ConstraintValidatorContext context) {
        if (this.value != null && !this.value.isEmpty()) {
            return Objects.equals(this.value, value.getKoodisto());
        }
        else {
            return true;
        }
    }
}
