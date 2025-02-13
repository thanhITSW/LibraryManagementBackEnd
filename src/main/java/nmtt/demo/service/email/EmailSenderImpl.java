package nmtt.demo.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderImpl implements EmailSenderService{
    private final JavaMailSender mailSender;

    /**
     * Sends a simple email to the specified recipient with the provided subject and body.
     * This method uses SimpleMailMessage to construct the email and sends it through the configured mail sender.
     *
     * @param toEmail The recipient's email address.
     * @param subject The subject of the email.
     * @param body The body content of the email.
     */
    @Override
    public void sendSimpleEmail(String toEmail,
                                String subject,
                                String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
    }
}
