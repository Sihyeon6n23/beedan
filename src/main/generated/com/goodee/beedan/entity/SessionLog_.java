package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(SessionLog.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class SessionLog_ {

	public static final String SL_ID = "slId";
	public static final String SL_SS_ID = "slSsId";
	public static final String SL_EN_DT = "slEnDt";
	public static final String SL_MEM_ID = "slMemId";
	public static final String SL_DU_SEC = "slDuSec";
	public static final String SL_ST_DT = "slStDt";

	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slId
	 **/
	public static volatile SingularAttribute<SessionLog, Long> slId;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slSsId
	 **/
	public static volatile SingularAttribute<SessionLog, String> slSsId;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slEnDt
	 **/
	public static volatile SingularAttribute<SessionLog, LocalDateTime> slEnDt;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slMemId
	 **/
	public static volatile SingularAttribute<SessionLog, Long> slMemId;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slDuSec
	 **/
	public static volatile SingularAttribute<SessionLog, Long> slDuSec;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog#slStDt
	 **/
	public static volatile SingularAttribute<SessionLog, LocalDateTime> slStDt;
	
	/**
	 * @see com.goodee.beedan.entity.SessionLog
	 **/
	public static volatile EntityType<SessionLog> class_;

}

