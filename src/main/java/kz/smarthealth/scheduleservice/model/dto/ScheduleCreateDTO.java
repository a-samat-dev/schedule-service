package kz.smarthealth.scheduleservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import kz.smarthealth.scheduleservice.util.AppConstants;
import kz.smarthealth.scheduleservice.validator.ScheduleCreate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

/**
 * DTO class used to create time slots
 *
 * Created by Samat Abibulla on 2023-07-30
 */
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@ScheduleCreate
public class ScheduleCreateDTO {

    @NotNull
    private UUID userId;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE_FORMAT)
    private LocalDate startDate;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE_FORMAT)
    private LocalDate endDate;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_TIME_FORMAT)
    private OffsetTime workingDayStartTime;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_TIME_FORMAT)
    private OffsetTime workingDayEndTime;
    @NotNull
    private Integer interval;
}
