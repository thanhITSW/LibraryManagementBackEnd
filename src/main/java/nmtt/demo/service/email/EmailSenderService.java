package nmtt.demo.service.email;

import org.springframework.scheduling.annotation.Async;

public interface EmailSenderService {
    void sendSimpleEmail(String toEmail,
                                String subject,
                                String body);

    void sendHtmlEmail(String toEmail, String subject, String htmlBody);
}
