package kz.smarthealth.scheduleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.scheduleservice.model.dto.ErrorResponseDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import kz.smarthealth.scheduleservice.util.MessageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ScheduleController}
 *
 * Created by Samat Abibulla on 2023-06-16
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class ScheduleControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
    }

    @Test
    void createSchedules_returnsBadRequest_whenMandatoryFieldsNotProvided() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder().build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(4, invalidFields.size());
        assertTrue(invalidFields.containsKey("userId"));
        assertTrue(invalidFields.containsKey("startDateTime"));
        assertTrue(invalidFields.containsKey("endDateTime"));
        assertTrue(invalidFields.containsKey("interval"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenInvalidStartDate() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(10))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(1, invalidFields.size());
        assertTrue(invalidFields.containsKey("startDateTime"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenInvalidEndDate() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(10))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(1, invalidFields.size());
        assertTrue(invalidFields.containsKey("endDateTime"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenStartDateTimeBeforeNow() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().minusHours(1).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(1).withMinute(0))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(1, invalidFields.size());
        assertTrue(invalidFields.containsKey("startDateTime"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenEndDateTimeBeforeStartDateTime() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("startDateTime"));
        assertTrue(invalidFields.containsKey("endDateTime"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenInvalidInterval() throws Exception {
        // given
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusHours(1).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .interval(20)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(1, invalidFields.size());
        assertTrue(invalidFields.containsKey("interval"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenAlreadyReservedSchedules() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0).plusMinutes(30))
                .isReserved(true)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(MessageSource.RESERVED_SCHEDULES_EXIST.getText(), errorResponseDTO.getMessage());
        assertNotNull(errorResponseDTO.getDateTime());
    }

    @Test
    void createSchedules_createsSchedules() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        ScheduleEntity existingSchedule1 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(1).withMinute(45).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .createdAt(LocalDateTime.now())
                .build());
        ScheduleEntity existingSchedule2 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(30).withSecond(0).withNano(0))
                .createdAt(LocalDateTime.now())
                .build());
        ScheduleEntity existingSchedule3 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(3).withMinute(0).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(15).withSecond(0).withNano(0))
                .createdAt(LocalDateTime.now())
                .build());
        scheduleRepository.save(existingSchedule1);
        scheduleRepository.save(existingSchedule2);
        scheduleRepository.save(existingSchedule3);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(0))
                .interval(15)
                .build();
        String requestBody = objectMapper.writeValueAsString(scheduleCreateDTO);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAllByUserIdBetweenDates(userId,
                LocalDateTime.now().plusHours(1).withMinute(30), LocalDateTime.now().plusHours(3).withMinute(15));

        assertFalse(scheduleEntityList.isEmpty());
        assertEquals(6, scheduleEntityList.size());
    }
}