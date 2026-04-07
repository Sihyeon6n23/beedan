package com.goodee.beedan.service.mail;

import com.goodee.beedan.common.constant.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final String SITE_URL = "http://localhost:8080";

    public void sendMail(String emailAddress, NotificationType notificationType, Long targetId) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String title = "[Beedan 서비스 안내] " + notificationType.getDefaultTitle();
            String fullUrl = SITE_URL + notificationType.generateUrl(targetId);

            String htmlContent = String.format(
                    "<h3>%s 안내</h3>" +
                            "<p>%s</p>" +
                            "<p><a href='%s'>상세 페이지로 이동하기</a></p>",

                    notificationType.getDefaultTitle(),
                    notificationType.getMessage(),
                    fullUrl
            );

            helper.setFrom("Beedan 서비스 <cotowook@naver.com>");
            helper.setTo(emailAddress);
            helper.setSubject(title);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
