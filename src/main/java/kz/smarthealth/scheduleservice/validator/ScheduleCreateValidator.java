package kz.smarthealth.scheduleservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.Set;

/**
 * Validator class for {@link ScheduleCreateDTO}
 *
 * Created by Samat Abibulla on 2023-06-14
 */
public class ScheduleCreateValidator implements ConstraintValidator<ScheduleCreate, ScheduleCreateDTO> {

    private static final Set<Integer> VALID_INTERVALS = Set.of(15, 30, 45, 60, 90, 120);

    @Override
    public boolean isValid(ScheduleCreateDTO scheduleCreateDTO,
                           ConstraintValidatorContext constraintValidatorContext) {
        LocalDate startDate = scheduleCreateDTO.getStartDate();
        LocalDate endDate = scheduleCreateDTO.getEndDate();
        Integer interval = scheduleCreateDTO.getInterval();

        if (startDate == null || endDate == null || scheduleCreateDTO.getWorkingDayStartTime() == null
                || scheduleCreateDTO.getWorkingDayEndTime() == null || interval == null) {
            return false;
        }
        if (!isStartDateBeforeCurrentDate(startDate, constraintValidatorContext)) {
            return false;
        }
        if (!isEndDateBeforeStartDate(startDate, endDate, constraintValidatorContext)) {
            return false;
        }
        if (!isValidInterval(interval, constraintValidatorContext)) {
            return false;
        }

        return isValidWorkingDayTimes(scheduleCreateDTO.getWorkingDayStartTime(),
                scheduleCreateDTO.getWorkingDayEndTime(), constraintValidatorContext);
    }

    private static boolean isStartDateBeforeCurrentDate(LocalDate startDate,
                                                        ConstraintValidatorContext constraintValidatorContext) {
        if (startDate.isBefore(LocalDate.now())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private static boolean isEndDateBeforeStartDate(LocalDate startDate,
                                                    LocalDate endDate,
                                                    ConstraintValidatorContext constraintValidatorContext) {
        if (endDate.isBefore(startDate)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid start date")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid end date")
                    .addPropertyNode("endDate")
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

    private boolean isValidWorkingDayTimes(OffsetTime workingDayStartTime,
                                           OffsetTime workingDayEndTime,
                                           ConstraintValidatorContext constraintValidatorContext) {
        if (workingDayEndTime.isBefore(workingDayStartTime)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Invalid working day end time")
                    .addPropertyNode("workingDayEndTime")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
