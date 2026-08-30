package com.hrms.repository;

import com.hrms.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByOfficialEmail(String officialEmail);
    Optional<Employee> findByUserId(Long userId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByOfficialEmail(String officialEmail);

    @Query("SELECT e FROM Employee e WHERE e.isActive = true AND " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.officialEmail) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:designationId IS NULL OR e.designation.id = :designationId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status)")
    Page<Employee> searchEmployees(
        @Param("search") String search,
        @Param("departmentId") Long departmentId,
        @Param("designationId") Long designationId,
        @Param("status") Employee.EmploymentStatus status,
        Pageable pageable);

    List<Employee> findByDepartmentIdAndIsActive(Long departmentId, Boolean isActive);
    List<Employee> findByReportingManagerId(Long managerId);
    long countByEmploymentStatus(Employee.EmploymentStatus status);
    long countByIsActive(Boolean isActive);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.joiningDate >= :startDate")
    long countNewJoiners(@Param("startDate") java.time.LocalDate startDate);

    @Query("SELECT MAX(e.employeeId) FROM Employee e WHERE e.employeeId LIKE 'EMP%'")
    String findLastEmployeeId();
}
