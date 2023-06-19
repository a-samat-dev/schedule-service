package kz.smarthealth.scheduleservice.validator;

import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
    void isValid_returnsFalse_whenInvalidStartDateTimeMinutes() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(1))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(30)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenInvalidEndDateTimeMinutes() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(1))
                .interval(30)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenStartDateTimeInPast() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDateTime(LocalDateTime.now().minusHours(1).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenEndDateTimeBeforeStartDateTime() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(1).withMinute(0))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenDifferenceBetweenStartAndEndDatetimeLessThenInterval() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(USER_ID)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0).plusMinutes(15))
                .interval(30)
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
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(20)
                .build();
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
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(15)
                .build();
        // when
        boolean result = underTest.isValid(scheduleCreateDTO, constraintValidatorContext);
        // then
        assertTrue(result);
    }
}