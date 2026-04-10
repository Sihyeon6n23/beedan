package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(QuoteSubmitCheckLog.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteSubmitCheckLog_ {

	public static final String QSCL_ID = "qsclId";
	public static final String QSCL_CHK_YN = "qsclChkYn";
	public static final String QSC_ID = "qscId";
	public static final String QSCL_CR_DT = "qsclCrDt";
	public static final String QU_ID = "quId";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog#qsclId
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheckLog, Long> qsclId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog#qsclChkYn
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheckLog, Boolean> qsclChkYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog#qscId
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheckLog, Long> qscId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog#qsclCrDt
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheckLog, LocalDateTime> qsclCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog#quId
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheckLog, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheckLog
	 **/
	public static volatile EntityType<QuoteSubmitCheckLog> class_;

}

