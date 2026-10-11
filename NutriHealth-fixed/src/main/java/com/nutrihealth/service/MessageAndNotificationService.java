package com.nutrihealth.service;

import com.nutrihealth.entity.Message;
import com.nutrihealth.entity.Notification;
import com.nutrihealth.entity.User;
import com.nutrihealth.repository.MessageRepository;
import com.nutrihealth.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageAndNotificationService {

    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public MessageAndNotificationService(MessageRepository messageRepository,
                                         NotificationRepository notificationRepository) {
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<Message> getInbox(User recipient) {
        return messageRepository.findByRecipientOrderBySentAtDesc(recipient);
    }

    public List<Message> getSent(User sender) {
        return messageRepository.findBySenderOrderBySentAtDesc(sender);
    }

    public Message sendMessage(User sender, User recipient, String subject, String content) {
        Message msg = new Message(sender, recipient, subject, content);
        Message saved = messageRepository.save(msg);
        
        // Also send instant notification to recipient
        sendNotification(recipient, "New Message from " + sender.getFullName(), subject, "SYSTEM", "/patient/messages");
        return saved;
    }

    public void markMessageRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
    }

    public List<Notification> getNotifications(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public void sendNotification(User recipient, String title, String message, String type, String linkUrl) {
        Notification notification = new Notification(recipient, title, message, type, linkUrl);
        notificationRepository.save(notification);
    }

    public boolean markNotificationRead(Long notificationId, User recipient) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getRecipient().getId().equals(recipient.getId()))
                .map(n -> {
                    if (!n.isRead()) {
                        n.setRead(true);
                        notificationRepository.save(n);
                    }
                    return true;
                })
                .orElse(false);
    }

    public int markAllNotificationsRead(User recipient) {
        return notificationRepository.markAllAsRead(recipient);
    }

    public long getUnreadNotificationsCount(User recipient) {
        return notificationRepository.countByRecipientAndReadFalse(recipient);
    }
}
