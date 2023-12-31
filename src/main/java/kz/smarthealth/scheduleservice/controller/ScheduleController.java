package kz.smarthealth.scheduleservice.controller;

import jakarta.validation.Valid;
import kz.smarthealth.scheduleservice.aop.Log;
import kz.smarthealth.scheduleservice.model.dto.ScheduleCreateDTO;
import kz.smarthealth.scheduleservice.model.dto.ScheduleDTO;
import kz.smarthealth.scheduleservice.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API to work with doctor/organization schedules
 *
 * Created by Samat Abibulla on 2023-06-14
 */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * Creates the list of schedules
     *
     * @param scheduleCreateDTO schedules parameters
     */
    @Log
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSchedules(@RequestBody @Valid ScheduleCreateDTO scheduleCreateDTO) {
        scheduleService.createSchedules(scheduleCreateDTO);
    }

    @Log
    @GetMapping("/by-user-id/{userId}")
    public List<ScheduleDTO> getSchedulesByUserId(@PathVariable UUID userId) {
        return scheduleService.getSchedulesByUserId(userId);
    }

    @Log
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScheduleById(@PathVariable UUID id) {
        scheduleService.deleteScheduleById(id);
    }
}
