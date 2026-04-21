package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(Buyer.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Buyer_ {

	public static final String BY_TTL_AM = "byTtlAm";
	public static final String BGP_GR = "bgpGr";
	public static final String MEM_BIZ_NO = "memBizNo";
	public static final String BY_FR_DT = "byFrDt";
	public static final String BY_ORD_CNT = "byOrdCnt";
	public static final String BY_LT_DT = "byLtDt";
	public static final String BY_ID = "byId";
	public static final String MEM_BIZ_TTL = "memBizTtl";

	
	/**
	 * @see com.goodee.beedan.entity.Buyer#byTtlAm
	 **/
	public static volatile SingularAttribute<Buyer, BigDecimal> byTtlAm;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#bgpGr
	 **/
	public static volatile SingularAttribute<Buyer, String> bgpGr;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#memBizNo
	 **/
	public static volatile SingularAttribute<Buyer, String> memBizNo;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#byFrDt
	 **/
	public static volatile SingularAttribute<Buyer, LocalDateTime> byFrDt;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#byOrdCnt
	 **/
	public static volatile SingularAttribute<Buyer, Integer> byOrdCnt;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#byLtDt
	 **/
	public static volatile SingularAttribute<Buyer, LocalDateTime> byLtDt;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#byId
	 **/
	public static volatile SingularAttribute<Buyer, Long> byId;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer
	 **/
	public static volatile EntityType<Buyer> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Buyer#memBizTtl
	 **/
	public static volatile SingularAttribute<Buyer, String> memBizTtl;

}

