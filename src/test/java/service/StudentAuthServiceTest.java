package com.edukira.service;

import com.edukira.dto.request.StudentLoginRequest;
import com.edukira.dto.request.StudentRegisterRequest;
import com.edukira.dto.response.StudentAuthResponse;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.entity.StudentAccount;
import com.edukira.enums.DocumentType;
import com.edukira.enums.Role;
import com.edukira.enums.StudentAccountStatus;
import com.edukira.enums.StudentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentAccountRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.security.JwtService;
import com.edukira.service.impl.StudentAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static javax.management.Query.eq;
import static jdk.jfr.internal.jfc.model.Constraint.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentAuthService — testes unitários")
class StudentAuthServiceTest {

    @Mock StudentRepository        studentRepo;
    @Mock StudentAccountRepository accountRepo;
    @Mock SchoolRepository         schoolRepo;
    @Mock JwtService               jwtService;
    @Mock PasswordEncoder          passwordEncoder;

    @InjectMocks StudentAuthServiceImpl studentAuthService;

    private School  school;
    private Student student;
    private UUID    schoolId;

    @BeforeEach
    void setUp() {
        schoolId = UUID.randomUUID();
        school = School.builder().id(schoolId).name("Escola Test").country("SN").build();
        student = Student.builder()
                .id(UUID.randomUUID()).school(school)
                .firstName("Ibrahim").lastName("Konaté")
                .classLevel("Terminale").status(StudentStatus.ACTIVE)
                .build();
    }

    // ── REGISTER ─────────────────────────────────────────────────────

    @Test
    @DisplayName("register aluno — cria StudentAccount com status PENDING_APPROVAL")
    void register_student_createsPendingAccount() {
        StudentRegisterRequest req = buildStudentRegisterReq();

        when(schoolRepo.findByCode(req.getSchoolId())).thenReturn(Optional.of(school));
        when(accountRepo.existsByDocumentTypeAndDocumentNumberAndSchoolId(
                any(), eq(req.getDocumentNumber()), eq(schoolId))).thenReturn(false);
        when(studentRepo.findByDocumentNumberAndSchoolId(req.getDocumentNumber(), schoolId))
                .thenReturn(Optional.of(student));
        when(passwordEncoder.encode(req.getPassword())).thenReturn("$hashed");
        when(accountRepo.save(any())).thenAnswer(i -> {
            StudentAccount acc = i.getArgument(0);
            acc.setId(UUID.randomUUID());
            return acc;
        });

        StudentAuthResponse response = studentAuthService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(StudentAccountStatus.PENDING_APPROVAL);
        verify(accountRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("register com documento já existente lança 409")
    void register_duplicateDocument_throws409() {
        StudentRegisterRequest req = buildStudentRegisterReq();

        when(schoolRepo.findByCode(req.getSchoolId())).thenReturn(Optional.of(school));
        when(accountRepo.existsByDocumentTypeAndDocumentNumberAndSchoolId(
                any(), eq(req.getDocumentNumber()), eq(schoolId))).thenReturn(true);

        assertThatThrownBy(() -> studentAuthService.register(req))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("register com escola inexistente lança 404")
    void register_unknownSchool_throws404() {
        StudentRegisterRequest req = buildStudentRegisterReq();

        when(schoolRepo.findByCode(req.getSchoolId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentAuthService.register(req))
                .isInstanceOf(EdukiraException.class);
    }

    // ── LOGIN ────────────────────────────────────────────────────────

    @Test
    @DisplayName("login aprovado — retorna access token")
    void login_approvedAccount_returnsToken() {
        StudentLoginRequest req = new StudentLoginRequest();
        req.setSchoolId("EDK-2025-DAKAR");
        req.setDocumentNumber("1234567890");
        req.setPassword("senha123");

        StudentAccount account = StudentAccount.builder()
                .id(UUID.randomUUID())
                .student(student)
                .school(school)
                .documentType(DocumentType.BI)
                .documentNumber("1234567890")
                .passwordHash("$hashed")
                .status(StudentAccountStatus.APPROVED)
                .role(Role.STUDENT)
                .build();

        when(schoolRepo.findByCode("EDK-2025-DAKAR")).thenReturn(Optional.of(school));
        when(accountRepo.findByDocumentNumberAndSchoolId("1234567890", schoolId))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("senha123", "$hashed")).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("student.jwt.token");
        when(jwtService.generateRefreshToken(any())).thenReturn("student.refresh.token");

        StudentAuthResponse response = studentAuthService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("student.jwt.token");
        assertThat(response.getStatus()).isEqualTo(StudentAccountStatus.APPROVED);
    }

    @Test
    @DisplayName("login com conta PENDING_APPROVAL lança 403")
    void login_pendingAccount_throws403() {
        StudentLoginRequest req = new StudentLoginRequest();
        req.setSchoolId("EDK-2025-DAKAR");
        req.setDocumentNumber("1234567890");
        req.setPassword("senha123");

        StudentAccount account = StudentAccount.builder()
                .id(UUID.randomUUID()).student(student).school(school)
                .documentNumber("1234567890").passwordHash("$hashed")
                .status(StudentAccountStatus.PENDING_APPROVAL)
                .role(Role.STUDENT)
                .build();

        when(schoolRepo.findByCode("EDK-2025-DAKAR")).thenReturn(Optional.of(school));
        when(accountRepo.findByDocumentNumberAndSchoolId("1234567890", schoolId))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("senha123", "$hashed")).thenReturn(true);

        assertThatThrownBy(() -> studentAuthService.login(req))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("login com conta SUSPENDED lança 403")
    void login_suspendedAccount_throws403() {
        StudentLoginRequest req = new StudentLoginRequest();
        req.setSchoolId("EDK-2025-DAKAR");
        req.setDocumentNumber("1234567890");
        req.setPassword("senha123");

        StudentAccount account = StudentAccount.builder()
                .id(UUID.randomUUID()).student(student).school(school)
                .documentNumber("1234567890").passwordHash("$hashed")
                .status(StudentAccountStatus.SUSPENDED)
                .role(Role.STUDENT)
                .build();

        when(schoolRepo.findByCode("EDK-2025-DAKAR")).thenReturn(Optional.of(school));
        when(accountRepo.findByDocumentNumberAndSchoolId("1234567890", schoolId))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("senha123", "$hashed")).thenReturn(true);

        assertThatThrownBy(() -> studentAuthService.login(req))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("login com password errada lança 401")
    void login_wrongPassword_throws401() {
        StudentLoginRequest req = new StudentLoginRequest();
        req.setSchoolId("EDK-2025-DAKAR");
        req.setDocumentNumber("1234567890");
        req.setPassword("errada");

        StudentAccount account = StudentAccount.builder()
                .id(UUID.randomUUID()).student(student).school(school)
                .documentNumber("1234567890").passwordHash("$hashed")
                .status(StudentAccountStatus.APPROVED)
                .role(Role.STUDENT)
                .build();

        when(schoolRepo.findByCode("EDK-2025-DAKAR")).thenReturn(Optional.of(school));
        when(accountRepo.findByDocumentNumberAndSchoolId("1234567890", schoolId))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("errada", "$hashed")).thenReturn(false);

        assertThatThrownBy(() -> studentAuthService.login(req))
                .isInstanceOf(EdukiraException.class);
    }

    // ── Helper ───────────────────────────────────────────────────────

    private StudentRegisterRequest buildStudentRegisterReq() {
        StudentRegisterRequest req = new StudentRegisterRequest();
        req.setSchoolId("EDK-2025-DAKAR");
        req.setFirstName("Ibrahim");
        req.setLastName("Konaté");
        req.setDocumentType(DocumentType.BI);
        req.setDocumentNumber("1234567890");
        req.setPassword("senha123");
        req.setRole(Role.STUDENT);
        return req;
    }
}
