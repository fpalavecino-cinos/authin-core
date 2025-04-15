package org.cinos.authin_core.mail.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record SendEmailRequest (
    String from,
    String[] to,
    String[] cc,
    String[] bcc,
    String subject,
    String message,
    List<MultipartFile> attachments
) { }
