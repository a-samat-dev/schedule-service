package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.exception.CustomException;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import kz.smarthealth.scheduleservice.util.AppConstants;
import kz.smarthealth.scheduleservice.util.MessageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kz.smarthealth.scheduleservice.util.AppConstants.UTC_ZONE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScheduleService}
 *
 * Created by Samat Abibulla on 2023-06-12
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Spy
    private ModelMapper modelMapper;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Captor
    private ArgumentCaptor<UUID> userIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> startDateTimeArgumentCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endDateTimeArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<ScheduleEntity>> listArgumentCaptor;

    @InjectMocks
    private ScheduleService underTest;

    @Test
    void createSchedules_createsSchedules() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(3);
        int interval = 60;
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(LocalTime.of(9, 0))
                .workingDayEndTime(LocalTime.of(18, 0))
                .interval(interval)
                .zoneOffset("+06:00")
                .build();
        when(scheduleRepository.findAllReservedSchedulesByUserIdBetweenDates(any(), any(), any()))
                .thenReturn(List.of(ScheduleEntity.builder()
                        .startDateTime(LocalDateTime.of(startDate, LocalTime.of(4, 0)))
                        .endDateTime(LocalDateTime.of(startDate, LocalTime.of(5, 0)))
                        .build()));
        // when
        underTest.createSchedules(scheduleCreateDTO);
        // then
        verify(scheduleRepository).deleteAllOverlappingSchedulesByUserUdBetweenDates(userIdArgumentCaptor.capture(),
                startDateTimeArgumentCaptor.capture(), endDateTimeArgumentCaptor.capture());
        verify(scheduleRepository).saveAll(listArgumentCaptor.capture());
        UUID actualUserId = userIdArgumentCaptor.getValue();
        LocalDateTime actualStartDateTime = startDateTimeArgumentCaptor.getValue();
        LocalDateTime actualEndDateTime = endDateTimeArgumentCaptor.getValue();
        List<ScheduleEntity> actualScheduleEntityList = listArgumentCaptor.getValue();

        assertEquals(userId, actualUserId);
        assertEquals(LocalDateTime.of(startDate, scheduleCreateDTO.getWorkingDayStartTime()).atZone(
                        ZoneId.of(scheduleCreateDTO.getZoneOffset())).withZoneSameInstant(AppConstants.UTC_ZONE_ID)
                .toLocalDateTime(), actualStartDateTime);
        assertEquals(LocalDateTime.of(endDate, scheduleCreateDTO.getWorkingDayEndTime()).atZone(
                        ZoneId.of(scheduleCreateDTO.getZoneOffset())).withZoneSameInstant(AppConstants.UTC_ZONE_ID)
                .toLocalDateTime(), actualEndDateTime);
        assertEquals(17, actualScheduleEntityList.size());

        LocalDateTime currStartDateTime = LocalDateTime.of(scheduleCreateDTO.getStartDate(),
                        scheduleCreateDTO.getWorkingDayStartTime()).atZone(ZoneId.of(scheduleCreateDTO.getZoneOffset()))
                .withZoneSameInstant(AppConstants.UTC_ZONE_ID).toLocalDateTime();

        for (int i = 0; i < actualScheduleEntityList.size(); i++) {
            if (i == 1) {
                currStartDateTime = currStartDateTime.plusMinutes(scheduleCreateDTO.getInterval());
            }

            ScheduleEntity schedule = actualScheduleEntityList.get(i);
            assertEquals(userId, schedule.getUserId());
            assertEquals(currStartDateTime, schedule.getStartDateTime());
            assertEquals(currStartDateTime.plusMinutes(scheduleCreateDTO.getInterval()), schedule.getEndDateTime());
            assertFalse(schedule.getIsReserved());


            if (i == 7) {
                currStartDateTime = LocalDateTime.of(startDate.plusDays(1), scheduleCreateDTO
                                .getWorkingDayStartTime()).atZone(ZoneId.of(scheduleCreateDTO.getZoneOffset()))
                        .withZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
            } else {
                currStartDateTime = currStartDateTime.plusMinutes(scheduleCreateDTO.getInterval());
            }
        }
    }

    @Test
    void getSchedulesByUserId_returnsEmptyList_whenInvalidUserId() {
        // given
        UUID userId = UUID.randomUUID();
        when(scheduleRepository.findAllByUserIdBetweenDates(any(), any(), any())).thenReturn(Collections.emptyList());
        // when
        List<ScheduleDTO> scheduleList = underTest.getSchedulesByUserId(userId);
        // then
        assertTrue(scheduleList.isEmpty());
    }

    @Test
    void getSchedulesByUserId_returnsSchedules() {
        // given
        UUID userId = UUID.randomUUID();
        List<ScheduleEntity> scheduleEntityList = List.of(
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0))
                        .endDateTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(30))
                        .isReserved(false)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(30))
                        .endDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0))
                        .isReserved(false)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0))
                        .endDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(30))
                        .isReserved(false)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(30))
                        .endDateTime(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0))
                        .isReserved(true)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build());
        when(scheduleRepository.findAllByUserIdBetweenDates(any(), any(), any())).thenReturn(scheduleEntityList);
        // when
        List<ScheduleDTO> scheduleDTOList = underTest.getSchedulesByUserId(userId);
        // then
        assertFalse(scheduleDTOList.isEmpty());
        assertEquals(4, scheduleDTOList.size());

        for (int i = 0; i < scheduleEntityList.size(); i++) {
            ScheduleEntity entity = scheduleEntityList.get(i);
            ScheduleDTO dto = scheduleDTOList.get(i);

            assertEquals(entity.getId(), dto.getId());
            assertEquals(userId, dto.getUserId());
            assertEquals(entity.getStartDateTime(), dto.getStartDateTime());
            assertEquals(entity.getEndDateTime(), dto.getEndDateTime());
            assertEquals(entity.getIsReserved(), dto.getIsReserved());
            assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        }
    }

    @Test
    void deleteScheduleById_throwsException_whenScheduleNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(scheduleRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deleteScheduleById(id));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(MessageSource.SCHEDULE_NOT_FOUND.getText(id.toString()), exception.getErrorMessage());
    }

    @Test
    void deleteScheduleById_throwsException_whenScheduleAlreadyReserved() {
        // given
        UUID id = UUID.randomUUID();
        ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                .isReserved(true)
                .build();
        when(scheduleRepository.findById(id)).thenReturn(Optional.of(scheduleEntity));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deleteScheduleById(id));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(MessageSource.SCHEDULE_RESERVED.getText(), exception.getErrorMessage());
    }

    @Test
    void deleteScheduleById_deletesSchedule() {
        // given
        ArgumentCaptor<UUID> scheduleIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        UUID id = UUID.randomUUID();
        ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                .isReserved(false)
                .build();
        when(scheduleRepository.findById(id)).thenReturn(Optional.of(scheduleEntity));
        // when
        underTest.deleteScheduleById(id);
        // then
        verify(scheduleRepository).deleteById(scheduleIdArgumentCaptor.capture());
        UUID actualId = scheduleIdArgumentCaptor.getValue();

        assertEquals(id, actualId);
    }
}