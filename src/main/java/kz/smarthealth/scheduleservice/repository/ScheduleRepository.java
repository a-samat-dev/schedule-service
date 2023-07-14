package kz.smarthealth.scheduleservice.repository;

import kz.smarthealth.scheduleservice.model.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PostgreSQL repository for {@link ScheduleEntity}
 *
 * Created by Samat Abibulla on 2023-06-12
 */
@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    @Modifying
    @Query(value = "select * from schedules s where  s.user_id = :userId and s.is_reserved " +
            "and (s.start_date_time <= :startDateTime and s.end_date_time >= :endDateTime " +
            "or s.end_date_time > :startDateTime and s.end_date_time <= :endDateTime) " +
            "order by start_date_time",
            nativeQuery = true)
    List<ScheduleEntity> findAllReservedSchedulesByUserIdBetweenDates(UUID userId,
                                                                      LocalDateTime startDateTime,
                                                                      LocalDateTime endDateTime);

    @Modifying
    @Query(value = "DELETE FROM schedules s WHERE s.user_id = :userId and s.is_reserved = false " +
            "and (s.start_date_time >= :startDateTime and s.start_date_time < :endDateTime or " +
            "s.end_date_time > :startDateTime and s.end_date_time <= :endDateTime)",
            nativeQuery = true)
    void deleteAllOverlappingSchedulesByUserUdBetweenDates(UUID userId,
                                                           LocalDateTime startDateTime,
                                                           LocalDateTime endDateTime);

    @Query(value = "SELECT * FROM schedules s WHERE s.user_id = :userId " +
            "and (s.start_date_time >= :startDateTime and " +
            "s.start_date_time < :endDateTime or s.end_date_time > :startDateTime and s.end_date_time <= :endDateTime) " +
            "order by s.start_date_time",
            nativeQuery = true)
    List<ScheduleEntity> findAllByUserIdBetweenDates(UUID userId,
                                                     LocalDateTime startDateTime,
                                                     LocalDateTime endDateTime);
}
