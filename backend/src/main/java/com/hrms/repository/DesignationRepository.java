package com.hrms.repository;

import com.hrms.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    boolean existsByName(String name);

    List<Designation> findByIsActive(Boolean isActive);

    List<Designation> findByDepartmentId(Long departmentId);
}
