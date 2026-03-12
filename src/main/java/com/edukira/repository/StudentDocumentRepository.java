package com.edukira.repository;

import com.edukira.entity.StudentDocument;
import com.edukira.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, UUID> {

    List<StudentDocument> findByStudentIdAndVisibleTrue(UUID studentId);

    List<StudentDocument> findByStudentIdAndTypeAndVisibleTrue(UUID studentId, DocumentType type);
}
