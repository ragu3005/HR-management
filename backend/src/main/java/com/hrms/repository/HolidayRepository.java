package com.hrms.repository;

import com.hrms.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByHolidayDateBetweenOrderByHolidayDate(
            LocalDate startDate,
            LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year ORDER BY h.holidayDate ASC")
    List<Holiday> findByYearOrderByHolidayDate(@Param("year") int year);
}