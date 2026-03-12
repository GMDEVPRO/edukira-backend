package com.edukira.service;

import com.edukira.dto.request.BroadcastMessageRequest;
import com.edukira.dto.request.DirectMessageRequest;
import com.edukira.dto.response.MessageResponse;
import com.edukira.dto.response.MessageStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponse sendDirect(DirectMessageRequest request, UUID schoolId, UUID senderId);

    List<MessageResponse> sendBroadcast(BroadcastMessageRequest request, UUID schoolId, UUID senderId);

    Page<MessageResponse> getHistory(UUID schoolId, Pageable pageable);

    MessageStatusResponse getStatus(UUID messageId, UUID schoolId);
}