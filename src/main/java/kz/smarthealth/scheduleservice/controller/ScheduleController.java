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
}
