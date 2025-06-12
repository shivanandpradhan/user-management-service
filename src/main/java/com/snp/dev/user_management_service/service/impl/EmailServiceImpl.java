//package com.snp.dev.user_management_service.service.impl;
//
//import com.snp.dev.user_management_service.service.EmailService;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.thymeleaf.context.Context;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailServiceImpl implements EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//
//    @Override
//    public Mono<Void> sendEmailVerification(String email, String token) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            context.setVariable("token", token);
//            String content = templateEngine.process("email-verification", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Email Verification");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendPasswordResetEmail(String email, String token) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            context.setVariable("token", token);
//            String content = templateEngine.process("password-reset", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Password Reset");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendWelcomeEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("welcome-email", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Welcome to Our Service");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendAccountLockedEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("account-locked", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Account Locked");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendAccountUnlockedEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("account-unlocked", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Account Unlocked");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendPasswordChangedEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("password-changed", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Password Changed");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendMfaEnabledEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("mfa-enabled", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("MFA Enabled");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//
//    @Override
//    public Mono<Void> sendMfaDisabledEmail(String email) {
//        return Mono.fromCallable(() -> {
//            Context context = new Context();
//            String content = templateEngine.process("mfa-disabled", context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("MFA Disabled");
//            helper.setText(content, true);
//            mailSender.send(message);
//            return null;
//        }).subscribeOn(Schedulers.boundedElastic()).then();
//    }
//}
