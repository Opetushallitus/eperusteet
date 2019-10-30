package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Koodi;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ValidKoodiValidator implements ConstraintValidator<ValidKoodisto, Koodi> {
    private String koodisto = "";

    @Override
    public void initialize(ValidKoodisto constraintAnnotation) {
        koodisto = constraintAnnotation.koodisto();
    }

    @Override
    public boolean isValid(Koodi koodi, ConstraintValidatorContext context) {
        if (koodi != null && !ObjectUtils.isEmpty(koodisto)) {
            boolean isValid = Objects.equals(koodisto, koodi.getKoodisto());
            return isValid;
        }
        else {
            return true;
        }
    }
}
