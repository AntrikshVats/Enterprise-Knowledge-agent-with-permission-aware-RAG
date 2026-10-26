package com.knowflow.config;

import com.knowflow.entity.*;
import com.knowflow.repository.*;
import com.knowflow.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        RoleRepository roleRepository,
        DocumentRepository documentRepository,
        DocumentService documentService,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded with demo enterprise data.");
            return;
        }

        log.info("Initializing enterprise knowledge base: roles, users, ACLs, and knowledge documents...");

        // 1. Roles
        Role rUser = roleRepository.save(new Role("ROLE_USER"));
        Role rEng = roleRepository.save(new Role("ROLE_ENGINEERING"));
        Role rHr = roleRepository.save(new Role("ROLE_HR"));
        Role rSec = roleRepository.save(new Role("ROLE_SECURITY"));
        Role rAdmin = roleRepository.save(new Role("ROLE_ADMIN"));

        // 2. Users
        String defaultPasswordHash = passwordEncoder.encode("password123");

        User alice = new User("alice.engineer@knowflow.ai", defaultPasswordHash, "Alice Chen", "ENGINEERING");
        alice.getRoles().addAll(Set.of(rUser, rEng));
        alice = userRepository.save(alice);

        User bob = new User("bob.hr@knowflow.ai", defaultPasswordHash, "Bob Martinez", "HR");
        bob.getRoles().addAll(Set.of(rUser, rHr));
        bob = userRepository.save(bob);

        User charlie = new User("charlie.sec@knowflow.ai", defaultPasswordHash, "Charlie Vance", "SECURITY");
        charlie.getRoles().addAll(Set.of(rUser, rSec));
        charlie = userRepository.save(charlie);

        User dave = new User("dave.admin@knowflow.ai", defaultPasswordHash, "Dave Kowalski", "MANAGEMENT");
        dave.getRoles().addAll(Set.of(rUser, rAdmin));
        dave = userRepository.save(dave);

        // 3. Documents
        createDoc(
            "Engineering Production Deployment Guide",
            "Comprehensive procedure for production releases, CI gates, approval requirements, and rollback runbooks.",
            "ENGINEERING",
            "INTERNAL",
            dave,
            List.of(
                new AclDef("ROLE", "ROLE_ENGINEERING", "READ"),
                new AclDef("ROLE", "ROLE_ADMIN", "ADMIN")
            ),
            """
            # Engineering Production Deployment Guide

            ## Release Prerequisites and CI Verification
            Before initiating a deployment to the production cluster, all automated CI checks must successfully pass.
            The pipeline enforces:
            1. 100% pass rate on unit, integration, and security static analysis (SAST) suites.
            2. Code coverage must exceed the mandatory 85% threshold.
            3. A minimum of two senior peer reviews and an explicit sign-off from the designated Release Lead.
            4. Artifact container images must be signed using Sigstore Cosign and scanned for CVEs with zero critical vulnerabilities.

            ## Deployment Stages and Canary Verification
            Production releases follow a phased canary progression:
            1. Deploy artifact to 5% canary traffic pod group for 15 minutes.
            2. Monitor Prometheus metric error rates and P99 latency (must remain below 120ms).
            3. Upon automated health check verification, traffic increments to 25%, 50%, and finally 100%.
            4. Database migrations must be backward-compatible with zero downtime using expand-contract patterns.

            ## Rollback Procedures and Incident Escalation
            If error rates exceed 0.5% during canary rollout or latency spikes above 250ms:
            1. Trigger automated rollback via the release control pipeline (`make rollback-prod`).
            2. Notify the #eng-production Slack channel and engage the Incident Commander on PagerDuty.
            3. Preserve Kubernetes container logs and heap dumps for post-incident review.
            """
        );

        createDoc(
            "Confidential Executive Compensation & Equity Framework",
            "Strictly restricted HR policy detailing salary bands, performance bonus calculations, and executive equity.",
            "HR",
            "RESTRICTED",
            bob,
            List.of(
                new AclDef("ROLE", "ROLE_HR", "READ"),
                new AclDef("ROLE", "ROLE_ADMIN", "ADMIN")
            ),
            """
            # Confidential Executive Compensation & Equity Framework

            ## Executive Salary Bands and Target Total Compensation
            Executive compensation at KnowFlow AI is benchmarked annually against Radford Global Technology metrics:
            - Director Level (L7): Base salary range $240,000 - $290,000 with a 35% target annual performance bonus.
            - Vice President (L8): Base salary range $320,000 - $390,000 with a 50% target performance bonus.
            - Senior Vice President / C-Suite (L9+): Base salary $450,000+ with 75% performance bonus tied to ARR growth.

            ## Performance Multipliers and Annual Bonus Calculation
            Bonus payouts are calculated using the corporate formula:
            `Bonus = Base Salary * Target Percentage * (Company Metric Score [0.0 - 1.5]) * (Individual KPI Score [0.0 - 1.3])`.
            Company metrics require achieving 115% of annual Net Recurring Revenue (NRR) and positive Free Cash Flow margin.

            ## Executive Equity Vesting and Liquidity Schedules
            Restricted Stock Units (RSUs) and incentive stock options operate on a 4-year schedule with a 1-year cliff (25% initial vesting), followed by monthly pro-rata vesting.
            Executive change-of-control double-trigger severance guarantees 12 months base salary plus 100% accelerated equity vesting.
            """
        );

        createDoc(
            "Security Incident Response & Infrastructure Hardening",
            "Protocols for managing Severity 1 cyber threats, zero-trust infrastructure, and incident forensics.",
            "SECURITY",
            "CONFIDENTIAL",
            charlie,
            List.of(
                new AclDef("ROLE", "ROLE_SECURITY", "READ"),
                new AclDef("ROLE", "ROLE_ADMIN", "ADMIN")
            ),
            """
            # Security Incident Response & Infrastructure Hardening

            ## Severity 1 Threat Classification and Containment
            A Sev-1 incident is declared when unauthorized data exfiltration, active privilege escalation, or compromised production credentials occur.
            The Security Operations Center (SOC) must:
            1. Isolate affected network pods immediately using Calico network security policies within 5 minutes.
            2. Revoke compromised Okta SAML tokens and rotate AWS IAM credentials.
            3. Snapshot disk volumes and memory state for chain-of-custody forensic examination.

            ## Zero-Trust Access and SSH Bastion Rotation
            Production servers reject all direct incoming public IP traffic.
            All engineer and operational access requires:
            1. Hardware FIDO2/WebAuthn hardware key authentication.
            2. Short-lived ephemeral certificates generated via HashiCorp Vault (TTL: 60 minutes).
            3. All bastion session terminals are recorded and audited for SOC 2 Type II compliance.

            ## Vulnerability Patching SLAs and Incident Escalation
            Critical vulnerabilities (CVSS 9.0+) require remediation and deployment within 24 hours of public disclosure.
            High vulnerabilities (CVSS 7.0 - 8.9) require resolution within 7 business days.
            """
        );

        createDoc(
            "Enterprise Employee Handbook & Workplace Guidelines",
            "General company policies on remote work, expense reimbursement, code of conduct, and wellness benefits.",
            "GENERAL",
            "PUBLIC",
            dave,
            List.of(
                new AclDef("PUBLIC", "*", "READ"),
                new AclDef("ROLE", "ROLE_ADMIN", "ADMIN")
            ),
            """
            # Enterprise Employee Handbook & Workplace Guidelines

            ## Remote Work and Flexible Work Hours
            KnowFlow AI embraces an asynchronous, hybrid-first culture:
            1. Core collaboration hours are 10:00 AM to 3:00 PM in your local timezone.
            2. Employees receive a one-time $1,000 ergonomic home office stipend upon joining.
            3. Annual $600 wellness subsidy covers gym memberships, mental health apps, and sports equipment.

            ## Travel and Expense Reimbursement Guidelines
            All business-related expenses must be submitted through the corporate expense portal within 30 days:
            1. Meals during business travel are capped at $85 per diem.
            2. Flights over 6 hours qualify for Premium Economy booking.
            3. Receipts are mandatory for all transactions above $25.

            ## Code of Conduct and Diversity Commitments
            We are dedicated to providing a collaborative, respectful, and inclusive environment.
            Zero tolerance is maintained for harassment, discrimination, or retaliation.
            Confidential reporting can be made through our anonymous ethics hotline at compliance@knowflow.ai.
            """
        );

        createDoc(
            "Distributed Event Streaming Architecture & Kafka Standards",
            "Technical specification for distributed stream processing, schema evolution, and topic partitions.",
            "ENGINEERING",
            "INTERNAL",
            alice,
            List.of(
                new AclDef("ROLE", "ROLE_ENGINEERING", "READ"),
                new AclDef("ROLE", "ROLE_ADMIN", "ADMIN")
            ),
            """
            # Distributed Event Streaming Architecture & Kafka Standards

            ## Topic Partitioning and Replication Factor Guidelines
            Production Kafka clusters operate on MSK across three Availability Zones:
            1. All business-critical topics must specify a replication factor of 3 with `min.insync.replicas=2`.
            2. Partition counts should scale based on producer throughput, targeting 10-20 MB/s per partition.
            3. Consumer groups must implement exponential backoff and dead-letter queues (DLQs) for failed deserialization.

            ## Schema Registry and Backward Compatibility
            Event payloads must be serialized using Apache Avro with schemas registered in the Confluent Schema Registry.
            1. The compatibility mode is strictly set to `BACKWARD_TRANSITIVE`.
            2. Deleting fields or changing field types without default values is prohibited.
            3. Field documentation strings must describe downstream business semantics.
            """
        );

        log.info("Finished seeding KnowFlow AI enterprise database with 4 demo users and 5 categorized knowledge documents.");
    }

    private record AclDef(String type, String id, String perm) {}

    private void createDoc(String title, String desc, String dept, String classification, User owner, List<AclDef> acls, String content) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setDescription(desc);
        doc.setDepartment(dept);
        doc.setClassification(classification);
        doc.setOwner(owner);
        doc.setStatus("ACTIVE");
        doc.setVersion(1);
        doc.setContentType("text/markdown");
        doc.setContentPreview(content.length() > 300 ? content.substring(0, 297) + "..." : content);
        doc.setFileSize((long) content.getBytes().length);

        Document savedDoc = documentRepository.save(doc);

        for (AclDef acl : acls) {
            savedDoc.addAcl(new DocumentAcl(savedDoc, acl.type(), acl.id(), acl.perm()));
        }

        List<DocumentChunk> chunks = documentService.chunkAndEmbed(savedDoc, content);
        savedDoc.getChunks().addAll(chunks);

        documentRepository.save(savedDoc);
    }
}
