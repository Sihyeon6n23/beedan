package com.goodee.beedan.repository.file;

import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc(String brdRefTy, Long brdRefNo);
    List<FileUpload> findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalse(String brdRefTy, Long brdRefNo);
    @Query("""
    SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END 
    FROM FileUpload f 
    WHERE f.brdRefTy = :#{#ref.refTy} 
      AND f.brdRefNo = :#{#ref.refNo} 
      AND (f.fileDelYn IS NULL OR f.fileDelYn = false)
""")
    boolean existsByRefDto(@Param("ref") RefDto ref);

    Optional<FileUpload> findFileUploadByFileUuid(String fileUuid);

    FileUpload findByBrdRefTyAndBrdRefNo(String stock, Long stId);
}
