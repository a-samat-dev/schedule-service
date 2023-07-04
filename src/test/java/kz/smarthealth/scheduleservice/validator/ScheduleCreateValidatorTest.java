package kz.smarthealth.scheduleservice.validator;

import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ScheduleCreateValidator}
 *
 * Created by Samat Abibulla on 2023-06-14
 */
@ExtendWith(MockitoExtension.class)
class ScheduleCreateValidatorTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @InjectMocks
    private ScheduleCreateValidator underTest;

    @Test
    void isValid_returnsFalse_whenMandatoryFieldsNotPassed() {
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenStartDateInPast() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .workingDayStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenEndDateBeforeStartDate() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(1))
                .workingDayStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenInvalidInterval() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .workingDayStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .interval(20)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenWorkingDayEndTimeIsBeforeStartTime() {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .workingDayStartTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .interval(15)
                .build();
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsTrue() {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .workingDayStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC))
                .workingDayEndTime(OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertTrue(result);
    }
}