package com.goodee.beedan.entity;

import com.goodee.beedan.common.policy.FeeCalculationType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(FeePolicy.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class FeePolicy_ {

	public static final String FP_CR_DT = "fpCrDt";
	public static final String BGP_GR = "bgpGr";
	public static final String FP_EF_TO_DT = "fpEfToDt";
	public static final String FP_ID = "fpId";
	public static final String FP_FEE_TY = "fpFeeTy";
	public static final String FP_UP_DT = "fpUpDt";
	public static final String FP_VAL = "fpVal";
	public static final String FP_AC_YN = "fpAcYn";
	public static final String FP_EF_FR_DT = "fpEfFrDt";
	public static final String FP_DES = "fpDes";
	public static final String FP_CALC_TY = "fpCalcTy";

	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpCrDt
	 **/
	public static volatile SingularAttribute<FeePolicy, LocalDateTime> fpCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#bgpGr
	 **/
	public static volatile SingularAttribute<FeePolicy, String> bgpGr;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpEfToDt
	 **/
	public static volatile SingularAttribute<FeePolicy, LocalDateTime> fpEfToDt;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpId
	 **/
	public static volatile SingularAttribute<FeePolicy, Long> fpId;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpFeeTy
	 **/
	public static volatile SingularAttribute<FeePolicy, String> fpFeeTy;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpUpDt
	 **/
	public static volatile SingularAttribute<FeePolicy, LocalDateTime> fpUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpVal
	 **/
	public static volatile SingularAttribute<FeePolicy, BigDecimal> fpVal;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpAcYn
	 **/
	public static volatile SingularAttribute<FeePolicy, Boolean> fpAcYn;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpEfFrDt
	 **/
	public static volatile SingularAttribute<FeePolicy, LocalDateTime> fpEfFrDt;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpDes
	 **/
	public static volatile SingularAttribute<FeePolicy, String> fpDes;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy#fpCalcTy
	 **/
	public static volatile SingularAttribute<FeePolicy, FeeCalculationType> fpCalcTy;
	
	/**
	 * @see com.goodee.beedan.entity.FeePolicy
	 **/
	public static volatile EntityType<FeePolicy> class_;

}

