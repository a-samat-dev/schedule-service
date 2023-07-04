package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    private ArgumentCaptor<OffsetDateTime> startDateTimeArgumentCaptor;
    @Captor
    private ArgumentCaptor<OffsetDateTime> endDateTimeArgumentCaptor;
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
                .workingDayStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .interval(interval)
                .build();
        when(scheduleRepository.findAllReservedSchedulesByUserIdBetweenDates(any(), any(), any()))
                .thenReturn(List.of(ScheduleEntity.builder()
                        .startDateTime(OffsetDateTime.of(startDate, LocalTime.of(11, 0, 0, 0), ZoneOffset.UTC))
                        .endDateTime(OffsetDateTime.of(startDate, LocalTime.of(12, 0, 0, 0), ZoneOffset.UTC))
                        .build()));
        // when
        underTest.createSchedules(scheduleCreateDTO);
        // then
        verify(scheduleRepository).deleteAllOverlappingSchedulesByUserUdBetweenDates(userIdArgumentCaptor.capture(),
                startDateTimeArgumentCaptor.capture(), endDateTimeArgumentCaptor.capture());
        verify(scheduleRepository).saveAll(listArgumentCaptor.capture());
        UUID actualUserId = userIdArgumentCaptor.getValue();
        OffsetDateTime actualStartDateTime = startDateTimeArgumentCaptor.getValue();
        OffsetDateTime actualEndDateTime = endDateTimeArgumentCaptor.getValue();
        List<ScheduleEntity> actualScheduleEntityList = listArgumentCaptor.getValue();

        assertEquals(userId, actualUserId);
        assertEquals(OffsetDateTime.of(startDate, scheduleCreateDTO.getWorkingDayStartTime().toLocalTime(),
                scheduleCreateDTO.getWorkingDayStartTime().getOffset()), actualStartDateTime);
        assertEquals(OffsetDateTime.of(endDate, scheduleCreateDTO.getWorkingDayEndTime().toLocalTime(),
                scheduleCreateDTO.getWorkingDayEndTime().getOffset()), actualEndDateTime);
        assertFalse(actualScheduleEntityList.isEmpty());
        assertEquals(17, actualScheduleEntityList.size());

        OffsetDateTime currStartDateTime = OffsetDateTime.of(scheduleCreateDTO.getStartDate(),
                LocalTime.of(scheduleCreateDTO.getWorkingDayStartTime().getHour(),
                        scheduleCreateDTO.getWorkingDayStartTime().getMinute()),
                scheduleCreateDTO.getWorkingDayStartTime().getOffset());
        OffsetDateTime currEndDateTime = currStartDateTime.plusMinutes(interval);

        for (int i = 0; i < actualScheduleEntityList.size(); i++) {
            ScheduleEntity schedule = actualScheduleEntityList.get(i);
            assertEquals(userId, schedule.getUserId());
            assertEquals(currStartDateTime, schedule.getStartDateTime());
            assertEquals(currEndDateTime, schedule.getEndDateTime());
            assertFalse(schedule.getIsReserved());
            currStartDateTime = currEndDateTime;
            currEndDateTime = currStartDateTime.plusMinutes(interval);

            if (i == 1) {
                currStartDateTime = currEndDateTime;
                currEndDateTime = currStartDateTime.plusMinutes(interval);
            }

            if (currStartDateTime.toLocalTime().equals(scheduleCreateDTO.getWorkingDayEndTime().toLocalTime())) {
                currStartDateTime = currStartDateTime.plusDays(1).withHour(scheduleCreateDTO.getWorkingDayStartTime()
                        .getHour()).withMinute(scheduleCreateDTO.getWorkingDayStartTime().getMinute());
                currEndDateTime = currStartDateTime.plusMinutes(interval);
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
                        .startDateTime(OffsetDateTime.now().plusDays(2).withHour(9).withMinute(0))
                        .endDateTime(OffsetDateTime.now().plusDays(2).withHour(9).withMinute(30))
                        .isReserved(false)
                        .createdAt(OffsetDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(OffsetDateTime.now().plusDays(2).withHour(9).withMinute(30))
                        .endDateTime(OffsetDateTime.now().plusDays(2).withHour(10).withMinute(0))
                        .isReserved(false)
                        .createdAt(OffsetDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(OffsetDateTime.now().plusDays(2).withHour(10).withMinute(0))
                        .endDateTime(OffsetDateTime.now().plusDays(2).withHour(10).withMinute(30))
                        .isReserved(false)
                        .createdAt(OffsetDateTime.now().minusDays(2))
                        .build(),
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .startDateTime(OffsetDateTime.now().plusDays(2).withHour(10).withMinute(30))
                        .endDateTime(OffsetDateTime.now().plusDays(2).withHour(11).withMinute(0))
                        .isReserved(true)
                        .createdAt(OffsetDateTime.now().minusDays(2))
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
}