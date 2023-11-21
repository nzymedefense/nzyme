package app.nzyme.core.rest.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { UUIDValidator.class })
public @interface UUID {

    String message() default "Not a valid UUID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
