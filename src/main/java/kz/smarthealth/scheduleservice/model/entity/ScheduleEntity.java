package kz.smarthealth.scheduleservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "schedules")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "start_date_time", nullable = false)
    private OffsetDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private OffsetDateTime endDateTime;

    @Column(name = "is_reserved", nullable = false)
    private Boolean isReserved;

    @Column(name = "created_at", nullable = false)
    protected OffsetDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
