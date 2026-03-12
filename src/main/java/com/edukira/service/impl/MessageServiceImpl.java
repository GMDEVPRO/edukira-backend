package com.edukira.service.impl;

import com.edukira.dto.request.BroadcastMessageRequest;
import com.edukira.dto.request.DirectMessageRequest;
import com.edukira.dto.response.MessageResponse;
import com.edukira.dto.response.MessageStatusResponse;
import com.edukira.entity.Message;
import com.edukira.entity.Student;
import com.edukira.enums.MessageStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.MessageRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.MessageService;
import com.edukira.service.SmsService;
import com.edukira.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository  messageRepo;
    private final StudentRepository  studentRepo;
    private final SmsService         smsService;
    private final WhatsAppService    whatsAppService;

    @Override
    @Transactional
    public MessageResponse sendDirect(DirectMessageRequest req, UUID schoolId, UUID senderId) {
        Student student = studentRepo.findByIdAndSchoolId(req.getStudentId(), schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));

        String phone = student.getGuardianPhone();
        String externalId = null;

        try {
            externalId = switch (req.getChannel()) {
                case SMS      -> smsService.send(phone, req.getBody());
                case WHATSAPP -> whatsAppService.send(phone, req.getBody());
                default       -> null;
            };
        } catch (Exception e) {
            log.error("[MESSAGE] Erro ao enviar: {}", e.getMessage());
        }

        Message message = Message.builder()
                .school(student.getSchool())
                .student(student)
                .recipientPhone(phone)
                .body(req.getBody())
                .channel(req.getChannel())
                .status(externalId != null ? MessageStatus.SENT : MessageStatus.FAILED)
                .externalMessageId(externalId)
                .sentAt(Instant.now())
                .build();

        messageRepo.save(message);
        log.info("[MESSAGE] Directo enviado | aluno={} canal={}", student.getId(), req.getChannel());
        return toResponse(message);
    }

    @Override
    @Transactional
    public List<MessageResponse> sendBroadcast(BroadcastMessageRequest req, UUID schoolId, UUID senderId) {
        List<Student> students = req.getClassLevel() != null
                ? studentRepo.findBySchoolIdAndClassLevel(schoolId, req.getClassLevel())
                : studentRepo.findBySchoolId(schoolId);

        List<MessageResponse> results = new ArrayList<>();

        for (Student student : students) {
            String phone = student.getGuardianPhone();
            if (phone == null || phone.isBlank()) continue;

            String externalId = null;
            try {
                externalId = switch (req.getChannel()) {
                    case SMS      -> smsService.send(phone, req.getBody());
                    case WHATSAPP -> whatsAppService.send(phone, req.getBody());
                    default       -> null;
                };
            } catch (Exception e) {
                log.warn("[BROADCAST] Falhou para aluno={}: {}", student.getId(), e.getMessage());
            }

            Message message = Message.builder()
                    .school(student.getSchool())
                    .student(student)
                    .recipientPhone(phone)
                    .body(req.getBody())
                    .channel(req.getChannel())
                    .status(externalId != null ? MessageStatus.SENT : MessageStatus.FAILED)
                    .externalMessageId(externalId)
                    .sentAt(Instant.now())
                    .build();

            messageRepo.save(message);
            results.add(toResponse(message));
        }

        log.info("[BROADCAST] {} mensagens enviadas | canal={} turma={}",
                results.size(), req.getChannel(), req.getClassLevel());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getHistory(UUID schoolId, Pageable pageable) {
        return messageRepo.findBySchoolId(schoolId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStatusResponse getStatus(UUID messageId, UUID schoolId) {
        Message message = messageRepo.findByIdAndSchoolId(messageId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Mensagem"));

        return MessageStatusResponse.builder()
                .id(message.getId())
                .status(message.getStatus())
                .channel(message.getChannel())
                .externalMessageId(message.getExternalMessageId())
                .recipientPhone(message.getRecipientPhone())
                .sentAt(message.getSentAt())
                .build();
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .recipientPhone(m.getRecipientPhone())
                .body(m.getBody())
                .channel(m.getChannel())
                .status(m.getStatus())
                .externalMessageId(m.getExternalMessageId())
                .sentAt(m.getSentAt())
                .build();
    }
}
