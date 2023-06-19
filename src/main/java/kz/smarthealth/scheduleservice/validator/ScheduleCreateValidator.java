package kz.smarthealth.scheduleservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Validator class for {@link ScheduleCreateDTO}
 *
 * Created by Samat Abibulla on 2023-06-14
 */
public class ScheduleCreateValidator implements ConstraintValidator<ScheduleCreate, ScheduleCreateDTO> {

    private static final Set<Integer> VALID_INTERVALS = Set.of(15, 30, 45, 60, 90, 120);
    private static final Set<Integer> VALID_MINUTES = Set.of(0, 15, 30, 45);

    @Override
    public boolean isValid(ScheduleCreateDTO scheduleCreateDTO,
                           ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime startDateTime = scheduleCreateDTO.getStartDateTime();
        LocalDateTime endDateTime = scheduleCreateDTO.getEndDateTime();
        Integer interval = scheduleCreateDTO.getInterval();

        if (startDateTime == null || endDateTime == null
                || interval == null) {
            return false;
        }
        if (!isValidStartDateTimeAndEndDateTime(constraintValidatorContext, startDateTime, endDateTime)) {
            return false;
        }
        if (!isStartDateTimeBeforeCurrentDateTime(startDateTime, constraintValidatorContext)) {
            return false;
        }
        if (!isEndDateTimeBeforeStartDateTime(startDateTime, endDateTime, constraintValidatorContext)) {
            return false;
        }
        if (!isValidInterval(interval, constraintValidatorContext)) {
            return false;
        }

        return isDateTimesMatchesInterval(startDateTime, endDateTime, interval, constraintValidatorContext);
    }

    private static boolean isValidStartDateTimeAndEndDateTime(ConstraintValidatorContext constraintValidatorContext,
                                                              LocalDateTime startDateTime,
                                                              LocalDateTime endDateTime) {
        if (!VALID_MINUTES.contains(startDateTime.getMinute())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date&time")
                    .addPropertyNode("startDateTime")
                    .addConstraintViolation();

            return false;
        }
        if (!VALID_MINUTES.contains(endDateTime.getMinute())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date&time")
                    .addPropertyNode("endDateTime")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private static boolean isStartDateTimeBeforeCurrentDateTime(LocalDateTime startDateTime,
                                                                ConstraintValidatorContext constraintValidatorContext) {
        if (startDateTime.isBefore(LocalDateTime.now())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date&time")
                    .addPropertyNode("startDateTime")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private static boolean isEndDateTimeBeforeStartDateTime(LocalDateTime startDateTime,
                                                            LocalDateTime endDateTime,
                                                            ConstraintValidatorContext constraintValidatorContext) {
        if (endDateTime.isBefore(startDateTime)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date&time")
                    .addPropertyNode("startDateTime")
                    .addConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid end date&time")
                    .addPropertyNode("endDateTime")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private static boolean isValidInterval(int interval, ConstraintValidatorContext constraintValidatorContext) {
        if (!VALID_INTERVALS.contains(interval)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid interval")
                    .addPropertyNode("interval")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private static boolean isDateTimesMatchesInterval(LocalDateTime startDateTime,
                                                      LocalDateTime endDateTime,
                                                      int interval,
                                                      ConstraintValidatorContext constraintValidatorContext) {
        long difference = ChronoUnit.MINUTES.between(startDateTime, endDateTime);

        if (difference < interval) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date&time")
                    .addPropertyNode("startDateTime")
                    .addConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid end date&time")
                    .addPropertyNode("endDateTime")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
