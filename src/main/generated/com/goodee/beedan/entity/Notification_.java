package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Notification.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Notification_ {

	public static final String NOTI_REA_YN = "notiReaYn";
	public static final String NOTI_ID = "notiId";
	public static final String NOTI_CON = "notiCon";
	public static final String NOTI_REF = "notiRef";
	public static final String MEMBER = "member";
	public static final String NOTI_UPD_MEM_ID = "notiUpdMemId";
	public static final String NOTI_TTL = "notiTtl";
	public static final String NOTI_DEL_YN = "notiDelYn";
	public static final String NOTI_CRE_DT = "notiCreDt";
	public static final String NOTI_UPD_DT = "notiUpdDt";

	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiReaYn
	 **/
	public static volatile SingularAttribute<Notification, Boolean> notiReaYn;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiId
	 **/
	public static volatile SingularAttribute<Notification, Long> notiId;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiCon
	 **/
	public static volatile SingularAttribute<Notification, String> notiCon;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiRef
	 **/
	public static volatile SingularAttribute<Notification, String> notiRef;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#member
	 **/
	public static volatile SingularAttribute<Notification, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiUpdMemId
	 **/
	public static volatile SingularAttribute<Notification, Long> notiUpdMemId;
	
	/**
	 * @see com.goodee.beedan.entity.Notification
	 **/
	public static volatile EntityType<Notification> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiTtl
	 **/
	public static volatile SingularAttribute<Notification, String> notiTtl;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiDelYn
	 **/
	public static volatile SingularAttribute<Notification, Boolean> notiDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiCreDt
	 **/
	public static volatile SingularAttribute<Notification, LocalDateTime> notiCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Notification#notiUpdDt
	 **/
	public static volatile SingularAttribute<Notification, LocalDateTime> notiUpdDt;

}

