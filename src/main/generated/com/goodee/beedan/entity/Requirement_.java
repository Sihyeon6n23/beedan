package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(Requirement.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Requirement_ {

	public static final String REQ_TTL = "reqTtl";
	public static final String REQ_CON = "reqCon";
	public static final String REQ_UPD_DT = "reqUpdDt";
	public static final String REQ_CUR = "reqCur";
	public static final String REQ_ID = "reqId";
	public static final String MEM_BIZ_TTL = "memBizTtl";
	public static final String REQ_PER_YN = "reqPerYn";
	public static final String REQ_PR = "reqPr";
	public static final String REQ_REP_YN = "reqRepYn";
	public static final String REQ_REF = "reqRef";
	public static final String REQ_STT = "reqStt";
	public static final String MEM_NM = "memNm";
	public static final String REQ_CRE_DT = "reqCreDt";
	public static final String REQ_DEL_YN = "reqDelYn";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqTtl
	 **/
	public static volatile SingularAttribute<Requirement, String> reqTtl;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqCon
	 **/
	public static volatile SingularAttribute<Requirement, String> reqCon;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqUpdDt
	 **/
	public static volatile SingularAttribute<Requirement, LocalDateTime> reqUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqCur
	 **/
	public static volatile SingularAttribute<Requirement, String> reqCur;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqId
	 **/
	public static volatile SingularAttribute<Requirement, Long> reqId;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#memBizTtl
	 **/
	public static volatile SingularAttribute<Requirement, String> memBizTtl;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqPerYn
	 **/
	public static volatile SingularAttribute<Requirement, Boolean> reqPerYn;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqPr
	 **/
	public static volatile SingularAttribute<Requirement, BigDecimal> reqPr;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqRepYn
	 **/
	public static volatile SingularAttribute<Requirement, Boolean> reqRepYn;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqRef
	 **/
	public static volatile SingularAttribute<Requirement, String> reqRef;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqStt
	 **/
	public static volatile SingularAttribute<Requirement, String> reqStt;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#memNm
	 **/
	public static volatile SingularAttribute<Requirement, String> memNm;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqCreDt
	 **/
	public static volatile SingularAttribute<Requirement, LocalDateTime> reqCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement
	 **/
	public static volatile EntityType<Requirement> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#reqDelYn
	 **/
	public static volatile SingularAttribute<Requirement, Boolean> reqDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.Requirement#memId
	 **/
	public static volatile SingularAttribute<Requirement, Long> memId;

}

