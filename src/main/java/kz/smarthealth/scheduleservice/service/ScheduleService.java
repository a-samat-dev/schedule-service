package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.exception.CustomException;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import kz.smarthealth.scheduleservice.util.MessageSource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static kz.smarthealth.scheduleservice.util.AppConstants.UTC_ZONE_ID;

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
        LocalDate startDate = scheduleCreateDTO.getStartDate();
        LocalDate endDate = scheduleCreateDTO.getEndDate();
        ZoneId zoneId = ZoneId.of(scheduleCreateDTO.getZoneOffset());
        LocalDateTime startDateTime = LocalDateTime.of(startDate, scheduleCreateDTO.getWorkingDayStartTime())
                .atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, scheduleCreateDTO.getWorkingDayEndTime())
                .atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
        List<ScheduleEntity> reservedScheduleEntities = scheduleRepository
                .findAllReservedSchedulesByUserIdBetweenDates(userId, startDateTime, endDateTime);
        List<ScheduleEntity> scheduleEntities = getScheduleEntities(scheduleCreateDTO);
        removeReservedSchedules(scheduleEntities, reservedScheduleEntities);
        scheduleRepository.deleteAllOverlappingSchedulesByUserUdBetweenDates(userId, startDateTime, endDateTime);
        scheduleRepository.saveAll(scheduleEntities);
    }

    /**
     * Creates list of {@link ScheduleEntity}
     *
     * @param scheduleCreateDTO request paramteres
     * @return list of schedule entities
     */
    private List<ScheduleEntity> getScheduleEntities(ScheduleCreateDTO scheduleCreateDTO) {
        ZoneId zoneId = ZoneId.of(scheduleCreateDTO.getZoneOffset());
        int interval = scheduleCreateDTO.getInterval();
        LocalDateTime startDateTime = LocalDateTime.of(scheduleCreateDTO.getStartDate(),
                scheduleCreateDTO.getWorkingDayStartTime());
        LocalDateTime endDateTime = startDateTime.plusMinutes(interval);
        LocalTime workingDayStartTime = scheduleCreateDTO.getWorkingDayStartTime();
        LocalTime workingDayEndTime = scheduleCreateDTO.getWorkingDayEndTime();
        List<ScheduleEntity> scheduleEntityList = new LinkedList<>();
        LocalDateTime terminalDateTime = LocalDateTime.of(scheduleCreateDTO.getEndDate().plusDays(1),
                LocalTime.of(0, 1));

        while (endDateTime.isBefore(terminalDateTime)) {
            if (startDateTime.toLocalTime().isBefore(workingDayStartTime)
                    || startDateTime.toLocalTime().isAfter(workingDayEndTime)
                    || endDateTime.toLocalTime().isAfter(workingDayEndTime)
                    || endDateTime.toLocalTime().isBefore(workingDayStartTime)) {
                startDateTime = endDateTime;
                endDateTime = startDateTime.plusMinutes(interval);
                continue;
            }

            scheduleEntityList.add(ScheduleEntity.builder()
                    .userId(scheduleCreateDTO.getUserId())
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime)
                    .isReserved(false)
                    .build());
            startDateTime = endDateTime;
            endDateTime = startDateTime.plusMinutes(interval);
        }

        scheduleEntityList.forEach(entity -> {
            entity.setStartDateTime(entity.getStartDateTime().atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID)
                    .toLocalDateTime());
            entity.setEndDateTime(entity.getEndDateTime().atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID)
                    .toLocalDateTime());
        });

        return scheduleEntityList;
    }

    /**
     * Removes overlapping schedules from new schedules list
     *
     * @param scheduleEntities         new schedules list
     * @param reservedScheduleEntities already existing reserved schedules list
     * @return schedules list
     */
    private List<ScheduleEntity> removeReservedSchedules(List<ScheduleEntity> scheduleEntities,
                                                         List<ScheduleEntity> reservedScheduleEntities) {
        if (scheduleEntities.isEmpty() || reservedScheduleEntities.isEmpty()) {
            return scheduleEntities;
        }

        int i = 0;
        int j = 0;

        while (i < scheduleEntities.size() && j < reservedScheduleEntities.size()) {
            ScheduleEntity scheduleEntity = scheduleEntities.get(i);

            if (!scheduleEntity.getEndDateTime().isAfter(reservedScheduleEntities.get(j).getStartDateTime())) {
                i++;
            } else if (!scheduleEntity.getStartDateTime().isBefore(reservedScheduleEntities.get(j).getEndDateTime())) {
                j++;
            } else {
                scheduleEntities.remove(scheduleEntity);
            }
        }

        return scheduleEntities;
    }

    /**
     * Gets all schedules by user for the next 3 months from now
     *
     * @param userId user id
     * @return list of schedules
     */
    public List<ScheduleDTO> getSchedulesByUserId(UUID userId) {
        List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAllByUserIdBetweenDates(userId,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().plusMonths(3));

        return scheduleEntityList.stream()
                .map(scheduleEntity -> modelMapper.map(scheduleEntity, ScheduleDTO.class))
                .toList();
    }

    /**
     * Deletes schedule by id
     *
     * @param id schedule id
     * @throws CustomException if schedule by id not found, or if schedule is already reserved
     */
    @Transactional
    public void deleteScheduleById(UUID id) {
        ScheduleEntity scheduleEntity = scheduleRepository.findById(id)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .errorMessage(MessageSource.SCHEDULE_NOT_FOUND.getText(id.toString()))
                        .build());

        if (Boolean.TRUE.equals(scheduleEntity.getIsReserved())) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .errorMessage(MessageSource.SCHEDULE_RESERVED.getText())
                    .build();
        }

        scheduleRepository.deleteById(id);
    }
}
