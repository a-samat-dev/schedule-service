package kz.smarthealth.scheduleservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.scheduleservice.model.dto.ErrorResponseDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import kz.smarthealth.scheduleservice.repository.ScheduleRepository;
import kz.smarthealth.scheduleservice.util.AppConstants;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static kz.smarthealth.scheduleservice.util.AppConstants.UTC_ZONE_ID;
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

        assertEquals(7, invalidFields.size());
        assertTrue(invalidFields.containsKey("userId"));
        assertTrue(invalidFields.containsKey("startDate"));
        assertTrue(invalidFields.containsKey("endDate"));
        assertTrue(invalidFields.containsKey("workingDayStartTime"));
        assertTrue(invalidFields.containsKey("workingDayEndTime"));
        assertTrue(invalidFields.containsKey("zoneOffset"));
        assertTrue(invalidFields.containsKey("interval"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenStartDateBeforeToday() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        LocalTime workingDayStartTime = LocalTime.of(9, 0);
        LocalTime workingDayEndTime = LocalTime.of(18, 0);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(workingDayStartTime)
                .workingDayEndTime(workingDayEndTime)
                .zoneOffset("+06:00")
                .interval(60)
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
        assertTrue(invalidFields.containsKey("startDate"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenEndDateBeforeStartDate() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(2);
        LocalTime workingDayStartTime = LocalTime.of(9, 0);
        LocalTime workingDayEndTime = LocalTime.of(18, 0);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(workingDayStartTime)
                .workingDayEndTime(workingDayEndTime)
                .zoneOffset("+06:00")
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
        assertTrue(invalidFields.containsKey("startDate"));
        assertTrue(invalidFields.containsKey("endDate"));
    }

    @Test
    void createSchedules_returnsBadRequest_whenInvalidInterval() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(3);
        LocalTime workingDayStartTime = LocalTime.of(9, 0);
        LocalTime workingDayEndTime = LocalTime.of(18, 0);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(workingDayStartTime)
                .workingDayEndTime(workingDayEndTime)
                .zoneOffset("+06:00")
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
    void createSchedules_returnsBadRequest_whenWorkingDayEndTimeBeforeWorkingDayStartTime() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(3);
        LocalTime workingDayStartTime = LocalTime.of(18, 0);
        LocalTime workingDayEndTime = LocalTime.of(9, 0);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(UUID.randomUUID())
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(workingDayStartTime)
                .workingDayEndTime(workingDayEndTime)
                .zoneOffset("+06:00")
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
        assertTrue(invalidFields.containsKey("workingDayEndTime"));
    }

    @Test
    void createSchedules_createsSchedules() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(3);
        LocalTime workingDayStartTime = LocalTime.of(9, 0);
        LocalTime workingDayEndTime = LocalTime.of(18, 0);
        ScheduleCreateDTO scheduleCreateDTO = ScheduleCreateDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .workingDayStartTime(workingDayStartTime)
                .workingDayEndTime(workingDayEndTime)
                .zoneOffset("+06:00")
                .interval(60)
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
                LocalDateTime.now(), LocalDateTime.now().plusDays(90));
        ZoneId zoneId = ZoneId.of(scheduleCreateDTO.getZoneOffset());
        LocalDateTime currStartDateTime = LocalDateTime.of(startDate, scheduleCreateDTO.getWorkingDayStartTime())
                .atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();

        assertFalse(scheduleEntityList.isEmpty());
        assertEquals(18, scheduleEntityList.size());

        for (int i = 0; i < scheduleEntityList.size(); i++) {
            ScheduleEntity scheduleEntity = scheduleEntityList.get(i);
            assertNotNull(scheduleEntity.getId());
            assertEquals(userId, scheduleEntity.getUserId());
            assertEquals(currStartDateTime, scheduleEntity.getStartDateTime());
            assertEquals(currStartDateTime.plusMinutes(scheduleCreateDTO.getInterval()),
                    scheduleEntity.getEndDateTime());
            assertFalse(scheduleEntity.getIsReserved());
            assertNotNull(scheduleEntity.getCreatedAt());

            if (i == 8) {
                currStartDateTime = LocalDateTime.of(startDate.plusDays(1),
                                scheduleCreateDTO.getWorkingDayStartTime())
                        .atZone(zoneId).withZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
            } else {
                currStartDateTime = currStartDateTime.plusMinutes(scheduleCreateDTO.getInterval());
            }
        }
    }

    @Test
    void getSchedulesByUserId_returnsEmptyList_whenInvalidUserId() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(
                                "/api/v1/schedules/by-user-id/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        List<Map<String, Object>> schedules = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        // then
        assertTrue(schedules.isEmpty());
    }

    @Test
    void getSchedulesByUserId_returnsSchedules() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<ScheduleEntity> scheduleEntityList = createSchedules(userId);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(
                                "/api/v1/schedules/by-user-id/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        List<Map<String, Object>> schedules = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        // then
        assertFalse(schedules.isEmpty());
        assertEquals(scheduleEntityList.size(), schedules.size());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT);

        for (int i = 0; i < scheduleEntityList.size(); i++) {
            ScheduleEntity entity = scheduleEntityList.get(i);
            Map<String, Object> dto = schedules.get(i);

            assertEquals(entity.getId().toString(), dto.get("id").toString());
            assertEquals(userId.toString(), dto.get("userId").toString());
            assertEquals(dateTimeFormatter.format(scheduleEntityList.get(i).getStartDateTime()), dto.get("startDateTime"));
            assertEquals(dateTimeFormatter.format(scheduleEntityList.get(i).getEndDateTime()), dto.get("endDateTime"));
            assertFalse((Boolean) dto.get("isReserved"));
            assertNotNull(dto.get("createdAt"));
        }
    }

    @Test
    void deleteScheduleById_returnsBadRequest_whenScheduleNotFound() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete(
                                "/api/v1/schedules/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(MessageSource.SCHEDULE_NOT_FOUND.getText(id.toString()), errorResponseDTO.getMessage());
    }

    @Test
    void deleteScheduleById_returnsBadRequest_whenScheduleReserved() throws Exception {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                .userId(UUID.randomUUID())
                .startDateTime(startDateTime)
                .endDateTime(startDateTime.plusMinutes(30))
                .isReserved(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        scheduleEntity = scheduleRepository.save(scheduleEntity);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete(
                                "/api/v1/schedules/" + scheduleEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(MessageSource.SCHEDULE_RESERVED.getText(), errorResponseDTO.getMessage());
    }

    @Test
    void deleteScheduleById_deletesSchedule() throws Exception {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                .userId(UUID.randomUUID())
                .startDateTime(startDateTime)
                .endDateTime(startDateTime.plusMinutes(30))
                .isReserved(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        scheduleEntity = scheduleRepository.save(scheduleEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete(
                                "/api/v1/schedules/" + scheduleEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent()).andReturn();
    }

    private List<ScheduleEntity> createSchedules(UUID userId) {
        ScheduleEntity schedule1 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(1).withMinute(45).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .isReserved(false)
                .createdAt(LocalDateTime.now())
                .build());
        ScheduleEntity schedule2 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(2).withMinute(30).withSecond(0).withNano(0))
                .isReserved(false)
                .createdAt(LocalDateTime.now())
                .build());
        ScheduleEntity schedule3 = scheduleRepository.save(ScheduleEntity.builder()
                .userId(userId)
                .startDateTime(LocalDateTime.now().plusHours(3).withMinute(0).withSecond(0).withNano(0))
                .endDateTime(LocalDateTime.now().plusHours(3).withMinute(15).withSecond(0).withNano(0))
                .isReserved(false)
                .createdAt(LocalDateTime.now())
                .build());
        schedule1 = scheduleRepository.save(schedule1);
        schedule2 = scheduleRepository.save(schedule2);
        schedule3 = scheduleRepository.save(schedule3);

        return List.of(schedule1, schedule2, schedule3);
    }
}