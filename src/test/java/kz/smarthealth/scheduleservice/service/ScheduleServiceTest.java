package kz.smarthealth.scheduleservice.service;

import kz.smarthealth.scheduleservice.exception.CustomException;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static kz.smarthealth.scheduleservice.util.MessageSource.RESERVED_SCHEDULES_EXIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScheduleService}
 *
 * Created by Samat Abibulla on 2023-06-12
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Captor
    private ArgumentCaptor<List<ScheduleEntity>> listArgumentCaptor;

    @InjectMocks
    private ScheduleService underTest;

    @Test
    void createSchedules_throwsException_whenThereAreReservedSchedules() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(3);
        when(scheduleRepository.findAllReservedSchedulesByUserIdBetweenDates(any(), any(), any()))
                .thenReturn(List.of(ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .build()));
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.createSchedules(ScheduleCreateDTO.builder()
                        .userId(userId)
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .interval(15)
                        .build()));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(RESERVED_SCHEDULES_EXIST.name(), exception.getError());
        assertEquals(RESERVED_SCHEDULES_EXIST.getText(), exception.getErrorMessage());
    }

    @Test
    void createSchedules_createsSchedules() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate scheduleDate = LocalDate.now().plusDays(2);
        LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, LocalTime.of(9, 0));
        LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, LocalTime.of(12, 0));
        int interval = 30;
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(userId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .interval(interval)
                .build();
        when(scheduleRepository.findAllReservedSchedulesByUserIdBetweenDates(any(), any(), any()))
                .thenReturn(List.of());
        // when
        underTest.createSchedules(scheduleCreateDTO);
        // then
        verify(scheduleRepository).saveAll(listArgumentCaptor.capture());
        List<ScheduleEntity> actualScheduleEntityList = listArgumentCaptor.getValue();

        assertFalse(actualScheduleEntityList.isEmpty());
        assertEquals(6, actualScheduleEntityList.size());

        LocalDateTime currStartDateTime = startDateTime;
        LocalDateTime currEndDateTime = startDateTime.plusMinutes(interval);

        for (ScheduleEntity schedule : actualScheduleEntityList) {
            assertEquals(userId, schedule.getUserId());
            assertEquals(currStartDateTime, schedule.getStartDateTime());
            assertEquals(currEndDateTime, schedule.getEndDateTime());
            assertFalse(schedule.isReserved());
            currStartDateTime = currStartDateTime.plusMinutes(interval);
            currEndDateTime = currEndDateTime.plusMinutes(interval);
        }
    }
}