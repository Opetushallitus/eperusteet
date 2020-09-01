package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Koodi;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ValidKoodiValidator implements ConstraintValidator<ValidKoodisto, Koodi> {
    private List<String> koodistot = new ArrayList<>();

    @Override
    public void initialize(ValidKoodisto constraintAnnotation) {
        koodistot = Arrays.asList(constraintAnnotation.koodisto());
    }

    @Override
    public boolean isValid(Koodi koodi, ConstraintValidatorContext context) {
        if (koodi != null && !ObjectUtils.isEmpty(koodistot)) {
            return koodistot.stream().anyMatch(k -> k.equals(koodi.getKoodisto()));
        }
        else {
            return true;
        }
    }
}
