package com.knowflow;

import com.knowflow.entity.Document;
import com.knowflow.entity.DocumentAcl;
import com.knowflow.entity.Role;
import com.knowflow.entity.User;
import com.knowflow.exception.AccessDeniedException;
import com.knowflow.repository.DocumentRepository;
import com.knowflow.service.AccessControlService;
import com.knowflow.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private TestAuditService auditService;
    private AccessControlService accessControlService;

    private User aliceEng;
    private User bobHr;
    private User adminDave;
    private Document engDoc;
    private Document hrDoc;
    private Document publicDoc;

    static class TestAuditService extends AuditService {
        int idorCount = 0;
        int accessDeniedCount = 0;

        public TestAuditService() {
            super(null);
        }

        @Override
        public void logIdorAttempt(User user, Long documentId, String details) {
            idorCount++;
        }

        @Override
        public void logAccessDenied(User user, String resourceType, String resourceId, String reason) {
            accessDeniedCount++;
        }
    }

    @BeforeEach
    void setUp() {
        auditService = new TestAuditService();
        accessControlService = new AccessControlService(documentRepository, auditService);

        Role roleUser = new Role("ROLE_USER");
        Role roleEng = new Role("ROLE_ENGINEERING");
        Role roleHr = new Role("ROLE_HR");
        Role roleAdmin = new Role("ROLE_ADMIN");

        aliceEng = new User("alice@knowflow.ai", "hash", "Alice Chen", "ENGINEERING");
        aliceEng.setId(1L);
        aliceEng.setRoles(Set.of(roleUser, roleEng));

        bobHr = new User("bob@knowflow.ai", "hash", "Bob Martinez", "HR");
        bobHr.setId(2L);
        bobHr.setRoles(Set.of(roleUser, roleHr));

        adminDave = new User("dave@knowflow.ai", "hash", "Dave Kowalski", "MANAGEMENT");
        adminDave.setId(3L);
        adminDave.setRoles(Set.of(roleUser, roleAdmin));

        // Engineering Doc
        engDoc = new Document();
        engDoc.setId(101L);
        engDoc.setTitle("Engineering Deployment Guide");
        engDoc.setDepartment("ENGINEERING");
        engDoc.setOwner(adminDave);
        engDoc.setStatus("ACTIVE");
        engDoc.addAcl(new DocumentAcl(engDoc, "ROLE", "ROLE_ENGINEERING", "READ"));

        // HR Doc
        hrDoc = new Document();
        hrDoc.setId(102L);
        hrDoc.setTitle("Executive Compensation Framework");
        hrDoc.setDepartment("HR");
        hrDoc.setOwner(bobHr);
        hrDoc.setStatus("ACTIVE");
        hrDoc.addAcl(new DocumentAcl(hrDoc, "ROLE", "ROLE_HR", "READ"));

        // Public Doc
        publicDoc = new Document();
        publicDoc.setId(103L);
        publicDoc.setTitle("Employee Handbook");
        publicDoc.setDepartment("GENERAL");
        publicDoc.setOwner(adminDave);
        publicDoc.setStatus("ACTIVE");
        publicDoc.addAcl(new DocumentAcl(publicDoc, "PUBLIC", "*", "READ"));
    }

    @Test
    @DisplayName("Alice (Engineering) can access Engineering and Public documents, but NOT HR")
    void testAliceAccessBoundaries() {
        assertTrue(accessControlService.canRead(aliceEng, engDoc), "Alice should have access to Engineering doc");
        assertTrue(accessControlService.canRead(aliceEng, publicDoc), "Alice should have access to Public doc");
        assertFalse(accessControlService.canRead(aliceEng, hrDoc), "Alice must NEVER have access to HR doc");
    }

    @Test
    @DisplayName("Bob (HR) can access HR and Public documents, but NOT Engineering")
    void testBobAccessBoundaries() {
        assertTrue(accessControlService.canRead(bobHr, hrDoc), "Bob should have access to HR doc");
        assertTrue(accessControlService.canRead(bobHr, publicDoc), "Bob should have access to Public doc");
        assertFalse(accessControlService.canRead(bobHr, engDoc), "Bob must NOT have access to Engineering doc");
    }

    @Test
    @DisplayName("Admin Dave has unrestricted visibility across all enterprise documents")
    void testAdminUnrestrictedAccess() {
        assertTrue(accessControlService.canRead(adminDave, engDoc));
        assertTrue(accessControlService.canRead(adminDave, hrDoc));
        assertTrue(accessControlService.canRead(adminDave, publicDoc));
    }

    @Test
    @DisplayName("assertCanRead throws AccessDeniedException and triggers Audit log on violation")
    void testAssertCanReadThrowsOnViolation() {
        assertThrows(AccessDeniedException.class, () -> accessControlService.assertCanRead(aliceEng, hrDoc));
        assertEquals(1, auditService.idorCount, "Audit log should record IDOR violation attempt");
    }
}
