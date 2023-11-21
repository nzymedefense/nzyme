package app.nzyme.core.rest.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { SubsystemValidator.class })
public @interface Subsystem {

    String message() default "Not a valid nzyme subsystem";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
