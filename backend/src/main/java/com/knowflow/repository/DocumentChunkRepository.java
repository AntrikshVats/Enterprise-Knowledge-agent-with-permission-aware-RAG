package com.knowflow.repository;

import com.knowflow.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);

    @Query("""
        SELECT c FROM DocumentChunk c
        JOIN FETCH c.document d
        WHERE c.document.id IN :authorizedDocumentIds
    """)
    List<DocumentChunk> findAuthorizedChunks(@Param("authorizedDocumentIds") Collection<Long> authorizedDocumentIds);

    @Query("""
        SELECT COUNT(c) FROM DocumentChunk c
        WHERE c.document.id IN :authorizedDocumentIds
    """)
    long countAuthorizedChunks(@Param("authorizedDocumentIds") Collection<Long> authorizedDocumentIds);
}
