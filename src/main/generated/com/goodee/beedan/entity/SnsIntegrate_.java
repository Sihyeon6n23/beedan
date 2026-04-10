package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.SnsType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(SnsIntegrate.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class SnsIntegrate_ {

	public static final String SNS_CAN_YN = "snsCanYn";
	public static final String SNS_UPD_DT = "snsUpdDt";
	public static final String SNS_SE_NO = "snsSeNo";
	public static final String MEMBER = "member";
	public static final String SNS_ID = "snsId";
	public static final String SNS_TP = "snsTp";
	public static final String SNS_CON_DT = "snsConDt";

	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsCanYn
	 **/
	public static volatile SingularAttribute<SnsIntegrate, Boolean> snsCanYn;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsUpdDt
	 **/
	public static volatile SingularAttribute<SnsIntegrate, LocalDateTime> snsUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsSeNo
	 **/
	public static volatile SingularAttribute<SnsIntegrate, String> snsSeNo;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#member
	 **/
	public static volatile SingularAttribute<SnsIntegrate, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsId
	 **/
	public static volatile SingularAttribute<SnsIntegrate, Long> snsId;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsTp
	 **/
	public static volatile SingularAttribute<SnsIntegrate, SnsType> snsTp;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate#snsConDt
	 **/
	public static volatile SingularAttribute<SnsIntegrate, LocalDateTime> snsConDt;
	
	/**
	 * @see com.goodee.beedan.entity.SnsIntegrate
	 **/
	public static volatile EntityType<SnsIntegrate> class_;

}

