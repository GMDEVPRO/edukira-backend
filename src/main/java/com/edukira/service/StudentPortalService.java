package com.edukira.service;

import com.edukira.dto.request.StudentContactUpdateRequest;
import com.edukira.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface StudentPortalService {
    StudentPortalResponse getMyProfile(UUID studentAccountId);
    StudentPortalResponse updateContact(UUID studentAccountId, StudentContactUpdateRequest request);
    List<StudentGradePortalResponse> getMyGrades(UUID studentId);
    StudentPaymentPortalResponse getMyPayments(UUID studentId, UUID schoolId);
    List<StudentDocumentResponse> getMyDocuments(UUID studentId);
    // Admin: aprovar/rejeitar cadastro
    void approveAccount(UUID accountId, UUID adminUserId, UUID schoolId);
    void rejectAccount(UUID accountId, String reason, UUID schoolId);
}
