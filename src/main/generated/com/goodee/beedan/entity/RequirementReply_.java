package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(RequirementReply.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class RequirementReply_ {

	public static final String REQ_REP_PER_YN = "reqRepPerYn";
	public static final String REQ_REP_CON = "reqRepCon";
	public static final String REQ_REP_UPD_DT = "reqRepUpdDt";
	public static final String REQ_REP_CRE_DT = "reqRepCreDt";
	public static final String REQ_REP_ID = "reqRepId";
	public static final String REQ_REP_TTL = "reqRepTtl";
	public static final String REQ_ID = "reqId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepPerYn
	 **/
	public static volatile SingularAttribute<RequirementReply, Boolean> reqRepPerYn;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepCon
	 **/
	public static volatile SingularAttribute<RequirementReply, String> reqRepCon;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepUpdDt
	 **/
	public static volatile SingularAttribute<RequirementReply, LocalDateTime> reqRepUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepCreDt
	 **/
	public static volatile SingularAttribute<RequirementReply, LocalDateTime> reqRepCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepId
	 **/
	public static volatile SingularAttribute<RequirementReply, Long> reqRepId;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqRepTtl
	 **/
	public static volatile SingularAttribute<RequirementReply, String> reqRepTtl;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply
	 **/
	public static volatile EntityType<RequirementReply> class_;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#reqId
	 **/
	public static volatile SingularAttribute<RequirementReply, Long> reqId;
	
	/**
	 * @see com.goodee.beedan.entity.RequirementReply#memId
	 **/
	public static volatile SingularAttribute<RequirementReply, Long> memId;

}

