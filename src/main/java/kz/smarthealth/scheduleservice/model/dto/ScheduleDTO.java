package kz.smarthealth.scheduleservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kz.smarthealth.scheduleservice.util.AppConstants;
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
public class ScheduleDTO {

    private UUID id;

    private UUID userId;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT)
    private LocalDateTime startDateTime;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT)
    private LocalDateTime endDateTime;

    private boolean isReserved;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT)
    protected LocalDateTime createdAt;
}
