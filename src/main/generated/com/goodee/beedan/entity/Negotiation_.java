package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Negotiation.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Negotiation_ {

	public static final String NG_END_DT = "ngEndDt";
	public static final String NG_ID = "ngId";
	public static final String NG_NM = "ngNm";
	public static final String NG_CRE_DT = "ngCreDt";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Negotiation#ngEndDt
	 **/
	public static volatile SingularAttribute<Negotiation, LocalDateTime> ngEndDt;
	
	/**
	 * @see com.goodee.beedan.entity.Negotiation#ngId
	 **/
	public static volatile SingularAttribute<Negotiation, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.Negotiation#ngNm
	 **/
	public static volatile SingularAttribute<Negotiation, String> ngNm;
	
	/**
	 * @see com.goodee.beedan.entity.Negotiation#ngCreDt
	 **/
	public static volatile SingularAttribute<Negotiation, LocalDateTime> ngCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Negotiation
	 **/
	public static volatile EntityType<Negotiation> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Negotiation#memId
	 **/
	public static volatile SingularAttribute<Negotiation, Long> memId;

}

