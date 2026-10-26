package com.knowflow.repository;

import com.knowflow.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatus(String status);

    List<Document> findByOwnerId(Long ownerId);

    @Query("""
        SELECT DISTINCT d FROM Document d
        LEFT JOIN d.acls acl
        WHERE d.status = 'ACTIVE' AND (
            d.owner.id = :userId
            OR acl.principalType = 'PUBLIC'
            OR (acl.principalType = 'USER' AND acl.principalId = :userIdStr)
            OR (acl.principalType = 'DEPARTMENT' AND LOWER(acl.principalId) = LOWER(:department))
            OR (acl.principalType = 'ROLE' AND acl.principalId IN :roles)
        )
        ORDER BY d.updatedAt DESC
    """)
    List<Document> findAuthorizedDocuments(
        @Param("userId") Long userId,
        @Param("userIdStr") String userIdStr,
        @Param("department") String department,
        @Param("roles") Collection<String> roles
    );

    @Query("""
        SELECT DISTINCT d.id FROM Document d
        LEFT JOIN d.acls acl
        WHERE d.status = 'ACTIVE' AND (
            d.owner.id = :userId
            OR acl.principalType = 'PUBLIC'
            OR (acl.principalType = 'USER' AND acl.principalId = :userIdStr)
            OR (acl.principalType = 'DEPARTMENT' AND LOWER(acl.principalId) = LOWER(:department))
            OR (acl.principalType = 'ROLE' AND acl.principalId IN :roles)
        )
    """)
    List<Long> findAuthorizedDocumentIds(
        @Param("userId") Long userId,
        @Param("userIdStr") String userIdStr,
        @Param("department") String department,
        @Param("roles") Collection<String> roles
    );
}
