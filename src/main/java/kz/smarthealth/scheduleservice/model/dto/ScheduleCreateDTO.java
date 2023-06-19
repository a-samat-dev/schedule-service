package kz.smarthealth.scheduleservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import kz.smarthealth.scheduleservice.util.AppConstants;
import kz.smarthealth.scheduleservice.validator.ScheduleCreate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@ScheduleCreate
public class ScheduleCreateDTO {

    @NotNull
    private UUID userId;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT)
    private LocalDateTime startDateTime;
    @NotNull
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT)
    private LocalDateTime endDateTime;
    @NotNull
    private Integer interval;
}
