package fi.vm.sade.eperusteet.domain.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidDateRangeValidator implements ConstraintValidator<ValidateDateRange, Object> {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private String startMethodName;
    private String endMethodName;

    private static final Logger LOG = LoggerFactory.getLogger(ValidDateRangeValidator.class);

    @Override
    public void initialize(ValidateDateRange validateDateRange) {
        startMethodName = getAccessorMethodName(validateDateRange.start());
        endMethodName = getAccessorMethodName(validateDateRange.end());
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext cvc) {
        Date startDate = invokeDateGetter(obj, startMethodName);
        Date endDate = invokeDateGetter(obj, endMethodName);
        if (startDate == null || endDate == null) {
            return true;
        }
        return (startDate.before(endDate));
    }

    private static Date invokeDateGetter(Object obj, String methodName) {
        try {
            return (Date) obj.getClass().getMethod(methodName, EMPTY_CLASS_ARRAY).invoke(obj, EMPTY_OBJECT_ARRAY);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
            throw new IllegalArgumentException("Unable to access " + obj.getClass().getName() + "#" + methodName, e);
        }
    }

    private static String getAccessorMethodName(String property) {
        StringBuilder builder = new StringBuilder("get");
        builder.append(Character.toUpperCase(property.charAt(0)));
        builder.append(property.substring(1));
        return builder.toString();
    }
}
