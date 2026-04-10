package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(FileUpload.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class FileUpload_ {

	public static final String FILE_UPD_DT = "fileUpdDt";
	public static final String FILE_DEL_YN = "fileDelYn";
	public static final String FILE_CTP = "fileCtp";
	public static final String FILE_SZ = "fileSz";
	public static final String FILE_EXT = "fileExt";
	public static final String FILE_PAT = "filePat";
	public static final String BRD_REF_NO = "brdRefNo";
	public static final String FILE_NM = "fileNm";
	public static final String BRD_REF_TY = "brdRefTy";
	public static final String FILE_CRE_DT = "fileCreDt";
	public static final String FILE_OR = "fileOr";
	public static final String FILE_UPD_MEM_ID = "fileUpdMemId";
	public static final String FILE_UUID = "fileUuid";
	public static final String FILE_ID = "fileId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileUpdDt
	 **/
	public static volatile SingularAttribute<FileUpload, LocalDateTime> fileUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileDelYn
	 **/
	public static volatile SingularAttribute<FileUpload, Boolean> fileDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileCtp
	 **/
	public static volatile SingularAttribute<FileUpload, String> fileCtp;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileSz
	 **/
	public static volatile SingularAttribute<FileUpload, Long> fileSz;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileExt
	 **/
	public static volatile SingularAttribute<FileUpload, String> fileExt;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#filePat
	 **/
	public static volatile SingularAttribute<FileUpload, String> filePat;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#brdRefNo
	 **/
	public static volatile SingularAttribute<FileUpload, Long> brdRefNo;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileNm
	 **/
	public static volatile SingularAttribute<FileUpload, String> fileNm;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#brdRefTy
	 **/
	public static volatile SingularAttribute<FileUpload, String> brdRefTy;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileCreDt
	 **/
	public static volatile SingularAttribute<FileUpload, LocalDateTime> fileCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileOr
	 **/
	public static volatile SingularAttribute<FileUpload, Integer> fileOr;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileUpdMemId
	 **/
	public static volatile SingularAttribute<FileUpload, Long> fileUpdMemId;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload
	 **/
	public static volatile EntityType<FileUpload> class_;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileUuid
	 **/
	public static volatile SingularAttribute<FileUpload, String> fileUuid;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#fileId
	 **/
	public static volatile SingularAttribute<FileUpload, Long> fileId;
	
	/**
	 * @see com.goodee.beedan.entity.FileUpload#memId
	 **/
	public static volatile SingularAttribute<FileUpload, Long> memId;

}

