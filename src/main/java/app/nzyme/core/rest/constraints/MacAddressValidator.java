package app.nzyme.core.rest.constraints;

import app.nzyme.core.util.Tools;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MacAddressValidator implements ConstraintValidator<MacAddress, String> {

    @Override
    public void initialize(MacAddress constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Tools.isValidMacAddress(value);
    }

}
