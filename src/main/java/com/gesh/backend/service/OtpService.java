package com.gesh.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private record OtpEntry(String code, Instant expiresAt, int attempts) {}

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSentAt = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final JavaMailSender mailSender;

    @Value("${app.otp.use-console-log:true}")
    private boolean useConsoleLog;

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.otp.cooldown-seconds:60}")
    private int cooldownSeconds;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {
        String normalized = email.trim().toLowerCase();

        Instant last = lastSentAt.get(normalized);
        if (last != null && Instant.now().isBefore(last.plusSeconds(cooldownSeconds))) {
            throw new IllegalArgumentException(
                    "لطفاً " + cooldownSeconds + " ثانیه صبر کنید و دوباره تلاش کنید");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));

        if (useConsoleLog) {
            System.out.println("------------------------------------");
            System.out.println("OTP for " + normalized + ":" + code);
        } else {
            sendRealEmail(normalized, code);
        }

        otpStore.put(normalized, new OtpEntry(code, Instant.now().plusSeconds(expirationMinutes * 60L), 0));
        lastSentAt.put(normalized, Instant.now());
    }

    public boolean verifyOtp(String email, String code) {
        String normalized = email.trim().toLowerCase();
        OtpEntry entry = otpStore.get(normalized);

        if (entry == null) return false;

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(normalized);
            return false;
        }

        if (entry.attempts() >= 5) {
            otpStore.remove(normalized);
            return false;
        }

        if (!entry.code().equals(code.trim())) {
            otpStore.put(normalized, new OtpEntry(entry.code(), entry.expiresAt(), entry.attempts() + 1));
            return false;
        }

        otpStore.remove(normalized);
        lastSentAt.remove(normalized);
        return true;
    }

    private void sendRealEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "گِش");
            helper.setTo(email);
            helper.setSubject("کد ورود شما به سامانه گِش");

            String html = """
                <div style="font-family:Tahoma,Arial,sans-serif;direction:rtl;text-align:center;
                            max-width:520px;margin:auto;padding:32px;background:#f8fafc;border-radius:16px;">
                    <h2 style="color:#1e40af;margin-bottom:8px;">ورود به سامانه گِش</h2>
                    <p style="color:#475569;font-size:15px;">کد یک‌بارمصرف شما:</p>
                    <div style="font-size:36px;font-weight:bold;letter-spacing:10px;
                                margin:28px 0;color:#0f172a;background:white;
                                padding:16px 24px;border-radius:12px;display:inline-block;
                                box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                        %s
                    </div>
                    <p style="color:#64748b;font-size:14px;">این کد تا <b>%d دقیقه</b> معتبر است.</p>
                    <hr style="border:none;border-top:1px solid #e2e8f0;margin:28px 0;">
                    <p style="color:#94a3b8;font-size:12px;">
                        اگر شما درخواست ورود نداده‌اید، این ایمیل را نادیده بگیرید.
                    </p>
                </div>
                """.formatted(code, expirationMinutes);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "ارسال ایمیل انجام نشد. تنظیمات SMTP و App Password را بررسی کنید.", e);
        }
    }
}