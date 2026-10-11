package com.nutrihealth.repository;

import com.nutrihealth.entity.Notification;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndReadFalse(User recipient);

    @Modifying
    @Query("update Notification n set n.read = true where n.recipient = :recipient and n.read = false")
    int markAllAsRead(@Param("recipient") User recipient);
}
