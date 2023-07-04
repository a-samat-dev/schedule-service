package kz.smarthealth.scheduleservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.smarthealth.scheduleservice.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ScheduleDTO {

    private UUID id;

    private UUID userId;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE_TIME_FORMAT)
    private OffsetDateTime startDateTime;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE_TIME_FORMAT)
    private OffsetDateTime endDateTime;

    private Boolean isReserved;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE_TIME_FORMAT)
    protected OffsetDateTime createdAt;
}
