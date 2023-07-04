package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
        OffsetDateTime startDateTime = OffsetDateTime.of(startDate,
                scheduleCreateDTO.getWorkingDayStartTime().toLocalTime(),
                scheduleCreateDTO.getWorkingDayStartTime().getOffset());
        OffsetDateTime endDateTime = OffsetDateTime.of(endDate, scheduleCreateDTO.getWorkingDayEndTime().toLocalTime(),
                scheduleCreateDTO.getWorkingDayEndTime().getOffset());
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
    private static List<ScheduleEntity> getScheduleEntities(ScheduleCreateDTO scheduleCreateDTO) {
        int interval = scheduleCreateDTO.getInterval();
        OffsetDateTime startDateTime = OffsetDateTime.of(scheduleCreateDTO.getStartDate(),
                scheduleCreateDTO.getWorkingDayStartTime().toLocalTime(),
                scheduleCreateDTO.getWorkingDayStartTime().getOffset()).withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(interval);
        OffsetTime workingDayStartTime = scheduleCreateDTO.getWorkingDayStartTime();
        OffsetTime workingDayEndTime = scheduleCreateDTO.getWorkingDayEndTime();
        List<ScheduleEntity> scheduleEntityList = new LinkedList<>();

        while (endDateTime.isBefore(OffsetDateTime.of(scheduleCreateDTO.getEndDate().plusDays(1),
                LocalTime.of(0, 1), scheduleCreateDTO.getWorkingDayStartTime().getOffset()))) {
            if (startDateTime.toOffsetTime().isBefore(workingDayStartTime)
                    || startDateTime.toOffsetTime().isAfter(workingDayEndTime)
                    || endDateTime.toOffsetTime().isAfter(workingDayEndTime)
                    || endDateTime.toOffsetTime().isBefore(workingDayStartTime)) {
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

        return scheduleEntityList;
    }

    /**
     * Gets all schedules by user for the next 3 months from now
     *
     * @param userId user id
     * @return list of schedules
     */
    public List<ScheduleDTO> getSchedulesByUserId(UUID userId) {
        List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAllByUserIdBetweenDates(userId,
                OffsetDateTime.now(), OffsetDateTime.now().plusMonths(3));

        return scheduleEntityList.stream()
                .map(scheduleEntity -> modelMapper.map(scheduleEntity, ScheduleDTO.class))
                .toList();
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
            OffsetDateTime startDateTime = reservedScheduleEntities.get(j).getStartDateTime();
            OffsetDateTime endDateTime = reservedScheduleEntities.get(j).getEndDateTime();
            ScheduleEntity scheduleEntity = scheduleEntities.get(i);

            if (!scheduleEntity.getEndDateTime().isAfter(startDateTime)) {
                i++;
            } else if (!scheduleEntity.getStartDateTime().isBefore(endDateTime)) {
                j++;
            } else {
                scheduleEntities.remove(scheduleEntity);
                i++;
            }
        }

        return scheduleEntities;
    }
}
