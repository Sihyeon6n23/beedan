package com.goodee.beedan.repository.file;

import com.goodee.beedan.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc(String brdRefTy, Long brdRefNo);
    List<FileUpload> findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalse(String brdRefTy, Long brdRefNo);
}
