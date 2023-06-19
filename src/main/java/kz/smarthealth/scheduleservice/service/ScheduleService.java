package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.exception.CustomException;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static kz.smarthealth.scheduleservice.util.MessageSource.RESERVED_SCHEDULES_EXIST;

/**
 * Service class that works with scheduling of doctor's services
 *
 * Created by Samat Abibulla on 2023-06-12
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ModelMapper modelMapper;

    /**
     * Creates schedule entities.
     * - If there are some schedules with overlapping dates, they will be replaced with new schedules.
     * - If there are reserved schedules, method throws exception.
     *
     * @param scheduleCreateDTO schedule entities parameters
     */
    @Transactional
    public void createSchedules(ScheduleCreateDTO scheduleCreateDTO) {
        UUID userId = scheduleCreateDTO.getUserId();
        LocalDateTime startDateTime = scheduleCreateDTO.getStartDateTime().withSecond(0).withNano(0);
        LocalDateTime endDateTime = scheduleCreateDTO.getEndDateTime().withSecond(0).withNano(0);

        if (!scheduleRepository.findAllReservedSchedulesByUserIdBetweenDates(userId, startDateTime,
                endDateTime).isEmpty()) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(RESERVED_SCHEDULES_EXIST.name())
                    .errorMessage(RESERVED_SCHEDULES_EXIST.getText())
                    .build();
        }

        scheduleRepository.deleteAllOverlappingSchedulesByUserUdBetweenDates(userId, startDateTime, endDateTime);
        scheduleRepository.saveAll(getScheduleEntities(scheduleCreateDTO));
    }

    /**
     * Creates list of {@link ScheduleEntity}
     *
     * @param scheduleCreateDTO request paramteres
     * @return list of schedule entities
     */
    private static List<ScheduleEntity> getScheduleEntities(ScheduleCreateDTO scheduleCreateDTO) {
        int interval = scheduleCreateDTO.getInterval();
        LocalDateTime startDateTime = scheduleCreateDTO.getStartDateTime().withSecond(0).withNano(0);
        LocalDateTime endDateTime = startDateTime.plusMinutes(interval).withSecond(0).withNano(0);
        List<ScheduleEntity> scheduleEntityList = new LinkedList<>();

        while (endDateTime.minusMinutes(1).isBefore(scheduleCreateDTO.getEndDateTime())) {
            scheduleEntityList.add(ScheduleEntity.builder()
                    .userId(scheduleCreateDTO.getUserId())
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime)
                    .build());
            startDateTime = endDateTime;
            endDateTime = startDateTime.plusMinutes(interval);
        }

        return scheduleEntityList;
    }
}
