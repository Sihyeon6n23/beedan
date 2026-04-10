package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(PortCustomsRate.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class PortCustomsRate_ {

	public static final String PCR_ID = "pcrId";
	public static final String PCR_SM_AM = "pcrSmAm";
	public static final String PCR_LG_AM = "pcrLgAm";
	public static final String PCR_UP_DT = "pcrUpDt";
	public static final String PCR_DES = "pcrDes";
	public static final String PCR_MD_AM = "pcrMdAm";
	public static final String PCR_TY = "pcrTy";
	public static final String PCR_YN = "pcrYn";
	public static final String PCR_CR_DT = "pcrCrDt";

	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrId
	 **/
	public static volatile SingularAttribute<PortCustomsRate, Long> pcrId;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrSmAm
	 **/
	public static volatile SingularAttribute<PortCustomsRate, BigDecimal> pcrSmAm;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrLgAm
	 **/
	public static volatile SingularAttribute<PortCustomsRate, BigDecimal> pcrLgAm;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrUpDt
	 **/
	public static volatile SingularAttribute<PortCustomsRate, LocalDateTime> pcrUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrDes
	 **/
	public static volatile SingularAttribute<PortCustomsRate, String> pcrDes;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrMdAm
	 **/
	public static volatile SingularAttribute<PortCustomsRate, BigDecimal> pcrMdAm;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrTy
	 **/
	public static volatile SingularAttribute<PortCustomsRate, String> pcrTy;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate
	 **/
	public static volatile EntityType<PortCustomsRate> class_;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrYn
	 **/
	public static volatile SingularAttribute<PortCustomsRate, Boolean> pcrYn;
	
	/**
	 * @see com.goodee.beedan.entity.PortCustomsRate#pcrCrDt
	 **/
	public static volatile SingularAttribute<PortCustomsRate, LocalDateTime> pcrCrDt;

}

