package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Koodi;
import java.util.Collection;
import javax.validation.UnexpectedTypeException;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidKoodiValidator implements ConstraintValidator<ValidKoodisto, Object> {
    private List<String> koodistot = new ArrayList<>();

    @Override
    public void initialize(ValidKoodisto constraintAnnotation) {
        koodistot = Arrays.asList(constraintAnnotation.koodisto());
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {

        if (object instanceof Koodi) {
            return isValid((Koodi) object, context);
        }

        if (object instanceof Collection) {
            return ((Collection) object).stream().allMatch(koodi -> isValid((Koodi) koodi, context));
        }

        if (object != null) {
            throw new UnexpectedTypeException("validation object not supported");
        }

        return true;
    }

    public boolean isValid(Koodi koodi, ConstraintValidatorContext context) {
        if (koodi != null && !ObjectUtils.isEmpty(koodistot)) {
            return koodistot.stream().anyMatch(k -> k.equals(koodi.getKoodisto()));
        } else {
            return true;
        }
    }
}
