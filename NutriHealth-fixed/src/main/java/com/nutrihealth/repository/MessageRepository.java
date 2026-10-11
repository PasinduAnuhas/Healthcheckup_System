package com.nutrihealth.repository;

import com.nutrihealth.entity.Message;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientOrderBySentAtDesc(User recipient);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    long countByRecipientAndReadFalse(User recipient);
}
