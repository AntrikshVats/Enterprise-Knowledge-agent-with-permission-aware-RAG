package com.knowflow.repository;

import com.knowflow.entity.DocumentAcl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAclRepository extends JpaRepository<DocumentAcl, Long> {
    List<DocumentAcl> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
    Optional<DocumentAcl> findByDocumentIdAndPrincipalTypeAndPrincipalId(Long documentId, String principalType, String principalId);
}
