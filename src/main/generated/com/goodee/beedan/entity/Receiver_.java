package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Receiver.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Receiver_ {

	public static final String RC_MSG = "rcMsg";
	public static final String RC_ZIP = "rcZip";
	public static final String RC_NM = "rcNm";
	public static final String RC_ADR = "rcAdr";
	public static final String RC_DEL_YN = "rcDelYn";
	public static final String RC_CRE_DT = "rcCreDt";
	public static final String RC_UPD_DT = "rcUpdDt";
	public static final String RC_PHN = "rcPhn";
	public static final String RC_IAM_YN = "rcIamYn";
	public static final String RC_ADR_DT = "rcAdrDt";
	public static final String MEMBER = "member";
	public static final String RC_ID = "rcId";
	public static final String RC_ADR_DF_YN = "rcAdrDfYn";
	public static final String RC_RGN = "rcRgn";

	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcMsg
	 **/
	public static volatile SingularAttribute<Receiver, String> rcMsg;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcZip
	 **/
	public static volatile SingularAttribute<Receiver, String> rcZip;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcNm
	 **/
	public static volatile SingularAttribute<Receiver, String> rcNm;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcAdr
	 **/
	public static volatile SingularAttribute<Receiver, String> rcAdr;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcDelYn
	 **/
	public static volatile SingularAttribute<Receiver, Boolean> rcDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcCreDt
	 **/
	public static volatile SingularAttribute<Receiver, LocalDateTime> rcCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcUpdDt
	 **/
	public static volatile SingularAttribute<Receiver, LocalDateTime> rcUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcPhn
	 **/
	public static volatile SingularAttribute<Receiver, String> rcPhn;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcIamYn
	 **/
	public static volatile SingularAttribute<Receiver, Boolean> rcIamYn;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcAdrDt
	 **/
	public static volatile SingularAttribute<Receiver, String> rcAdrDt;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#member
	 **/
	public static volatile SingularAttribute<Receiver, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcId
	 **/
	public static volatile SingularAttribute<Receiver, Long> rcId;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcAdrDfYn
	 **/
	public static volatile SingularAttribute<Receiver, Boolean> rcAdrDfYn;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver#rcRgn
	 **/
	public static volatile SingularAttribute<Receiver, String> rcRgn;
	
	/**
	 * @see com.goodee.beedan.entity.Receiver
	 **/
	public static volatile EntityType<Receiver> class_;

}

