package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(QuoteSubmitCheck.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteSubmitCheck_ {

	public static final String QSC_CR_DT = "qscCrDt";
	public static final String QSC_ID = "qscId";
	public static final String QSC_RQ_YN = "qscRqYn";
	public static final String QSC_UP_DT = "qscUpDt";
	public static final String QSC_DES = "qscDes";
	public static final String QSC_KEY = "qscKey";
	public static final String QSC_SORT = "qscSort";
	public static final String QSC_YN = "qscYn";
	public static final String QSC_DFLT_YN = "qscDfltYn";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscCrDt
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, LocalDateTime> qscCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscId
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, Long> qscId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscRqYn
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, Boolean> qscRqYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscUpDt
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, LocalDateTime> qscUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscDes
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, String> qscDes;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscKey
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, String> qscKey;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscSort
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, Integer> qscSort;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscYn
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, Boolean> qscYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck
	 **/
	public static volatile EntityType<QuoteSubmitCheck> class_;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteSubmitCheck#qscDfltYn
	 **/
	public static volatile SingularAttribute<QuoteSubmitCheck, Boolean> qscDfltYn;

}

