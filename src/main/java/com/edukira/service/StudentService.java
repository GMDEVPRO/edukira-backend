package com.edukira.service;
import com.edukira.dto.request.StudentRequest;
import com.edukira.dto.response.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface StudentService {
    Page<StudentResponse> findAll(UUID schoolId, Pageable pageable);
    StudentResponse findById(UUID id, UUID schoolId);
    StudentResponse create(StudentRequest request, UUID schoolId);
    StudentResponse update(UUID id, StudentRequest request, UUID schoolId);
    void delete(UUID id, UUID schoolId);
}
