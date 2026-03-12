package com.edukira.controller;

import com.edukira.dto.request.BroadcastMessageRequest;
import com.edukira.dto.request.DirectMessageRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.MessageResponse;
import com.edukira.dto.response.MessageStatusResponse;
import com.edukira.service.MessageService;
import com.edukira.util.TenantUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/direct")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<MessageResponse>> sendDirect(
            @Valid @RequestBody DirectMessageRequest request) {
        UUID schoolId = TenantUtil.currentSchoolId();
        UUID senderId = TenantUtil.currentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.sendDirect(request, schoolId, senderId)));
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> sendBroadcast(
            @Valid @RequestBody BroadcastMessageRequest request) {
        UUID schoolId = TenantUtil.currentSchoolId();
        UUID senderId = TenantUtil.currentUserId();
        List<MessageResponse> result = messageService.sendBroadcast(request, schoolId, senderId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getHistory(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID schoolId = TenantUtil.currentSchoolId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getHistory(schoolId, pageable)));
    }

    @GetMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<MessageStatusResponse>> getStatus(
            @PathVariable UUID id) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getStatus(id, schoolId)));
    }
}
