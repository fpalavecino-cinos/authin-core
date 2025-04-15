package org.cinos.authin_core.mail.config;

import org.eclipse.angus.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.security.GeneralSecurityException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:email.properties")
public class EmailConfig {

    @Value("${email.username}")
    private String EMAIL;
    @Value("${email.password}")
    private String PASSWORD;

    private Properties getProperties() throws GeneralSecurityException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
        socketFactory.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.socketFactory", socketFactory);

        return properties;
    }

    @Bean
    public JavaMailSender getJavaMailSender() throws GeneralSecurityException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setUsername(EMAIL);
        mailSender.setPassword(PASSWORD);
        mailSender.setJavaMailProperties(getProperties());
        return mailSender;
    }

    @Bean
    public ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }
}
