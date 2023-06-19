package kz.smarthealth.scheduleservice.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ScheduleCreateValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ScheduleCreate {

    String message() default "Invalid Schedule Create Request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
