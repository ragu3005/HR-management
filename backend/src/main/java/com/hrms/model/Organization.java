package com.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 500)
    private String logoPath;

    @Column(length = 200)
    private String website;

    @Column(nullable = false)
    @Builder.Default
    private String workStartTime = "09:00:00";

    @Column(nullable = false)
    @Builder.Default
    private String workEndTime = "18:00:00";

    @Column(nullable = false)
    @Builder.Default
    private String workingDays = "MON,TUE,WED,THU,FRI";

    @Column(nullable = false)
    @Builder.Default
    private Integer lateCheckInMins = 15;

    @Column(nullable = false)
    @Builder.Default
    private Integer overtimeThresholdMins = 480;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
