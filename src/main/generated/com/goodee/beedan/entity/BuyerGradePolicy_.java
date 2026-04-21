package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(BuyerGradePolicy.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BuyerGradePolicy_ {

	public static final String BGP_ID = "bgpId";
	public static final String BGP_GR = "bgpGr";
	public static final String BGP_MIN_ORD_CNT = "bgpMinOrdCnt";
	public static final String BGP_EF_FR_DT = "bgpEfFrDt";
	public static final String BGP_CR_DT = "bgpCrDt";
	public static final String BGP_DES = "bgpDes";
	public static final String BGP_AC_YN = "bgpAcYn";
	public static final String BGP_EF_TO_DT = "bgpEfToDt";
	public static final String BGP_UP_DT = "bgpUpDt";
	public static final String BGP_MIN_TT_AM = "bgpMinTtAm";

	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpId
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, Long> bgpId;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpGr
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, String> bgpGr;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpMinOrdCnt
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, Integer> bgpMinOrdCnt;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpEfFrDt
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, LocalDateTime> bgpEfFrDt;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpCrDt
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, LocalDateTime> bgpCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy
	 **/
	public static volatile EntityType<BuyerGradePolicy> class_;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpDes
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, String> bgpDes;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpAcYn
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, Boolean> bgpAcYn;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpEfToDt
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, LocalDateTime> bgpEfToDt;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpUpDt
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, LocalDateTime> bgpUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.BuyerGradePolicy#bgpMinTtAm
	 **/
	public static volatile SingularAttribute<BuyerGradePolicy, BigDecimal> bgpMinTtAm;

}

