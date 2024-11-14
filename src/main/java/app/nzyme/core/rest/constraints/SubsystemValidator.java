package app.nzyme.core.rest.constraints;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SubsystemValidator implements ConstraintValidator<Subsystem, String> {

    @Override
    public void initialize(Subsystem constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        try {
            app.nzyme.plugin.Subsystem.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
