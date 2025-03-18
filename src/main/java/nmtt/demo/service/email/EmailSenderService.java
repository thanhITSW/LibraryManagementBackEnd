package nmtt.demo.service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public interface EmailSenderService {
    public void sendSimpleEmail(String toEmail,
                                String subject,
                                String body);

    public void sendHtmlEmail(String toEmail, String subject, String htmlBody);
}
