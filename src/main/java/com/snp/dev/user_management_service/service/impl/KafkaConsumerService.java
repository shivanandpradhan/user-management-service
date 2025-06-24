package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @KafkaListener(
            topics = "${app.kafka.topics.email}",
            autoStartup = "#{${app.kafka.consumer-enabled}}"
    )
    public void handleEmailMessage(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());

            Context context = new Context();
            context.setVariables(message.getVariables());

            String htmlContent = templateEngine.process(
                    message.getTemplate(),
                    context
            );

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

            log.info("Email sent to {}", message.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}", message.getTo(), e);
        }
    }
}
