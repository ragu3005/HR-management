package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Holiday;
import com.hrms.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayRepository holidayRepository;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.HolidayDTO>>> getHolidays(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<CommonDTOs.HolidayDTO> dtos = holidayRepository.findByYearOrderByHolidayDate(targetYear).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.HolidayDTO>> createHoliday(
            @RequestBody CommonDTOs.HolidayDTO request) {
        try {
            Holiday.HolidayType type = Holiday.HolidayType.PUBLIC;
            if (request.getType() != null) {
                try { type = Holiday.HolidayType.valueOf(request.getType()); } catch (Exception ignored) {}
            }

            Holiday h = Holiday.builder()
                    .name(request.getName())
                    .holidayDate(request.getHolidayDate())
                    .type(type)
                    .description(request.getDescription())
                    .build();

            Holiday saved = holidayRepository.save(h);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Holiday created", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deleteHoliday(@PathVariable Long id) {
        holidayRepository.deleteById(id);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Holiday deleted", null));
    }

    private CommonDTOs.HolidayDTO toDto(Holiday h) {
        return CommonDTOs.HolidayDTO.builder()
                .id(h.getId())
                .name(h.getName())
                .holidayDate(h.getHolidayDate())
                .type(h.getType().name())
                .description(h.getDescription())
                .year(h.getHolidayDate() != null ? h.getHolidayDate().getYear() : null)
                .build();
    }
}
