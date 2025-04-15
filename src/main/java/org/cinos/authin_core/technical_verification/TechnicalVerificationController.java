package org.cinos.authin_core.technical_verification;

import lombok.RequiredArgsConstructor;
import org.cinos.authin_core.posts.dto.AcceptVerificationRequest;
import org.cinos.authin_core.technical_verification.dto.TechnicalVerificationRequest;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.technical_verification.service.ITechnicalVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/technical-verification")
@RequiredArgsConstructor
public class TechnicalVerificationController {

    private final ITechnicalVerificationService technicalVerificationService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/order/{postId}")
    public ResponseEntity<Object> orderVerification(@PathVariable final Long postId) throws PostNotFoundException {
        technicalVerificationService.orderVerification(postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/accept")
    public ResponseEntity<Object> acceptVerification(@RequestBody final AcceptVerificationRequest acceptVerificationRequest) throws PostNotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime parsedDate = LocalDateTime.parse(acceptVerificationRequest.appointmentDate(), formatter);
        technicalVerificationService.acceptVerification(acceptVerificationRequest.postId(), parsedDate);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/process")
    public ResponseEntity<Object> processVerification(@RequestBody final TechnicalVerificationRequest technicalVerificationRequest) throws PostNotFoundException {
        technicalVerificationService.processVerification(technicalVerificationRequest);
        return ResponseEntity.ok().build();
    }
}
