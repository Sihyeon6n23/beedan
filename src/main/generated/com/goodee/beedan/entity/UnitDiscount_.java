package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.OverlapType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(UnitDiscount.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class UnitDiscount_ {

	public static final String UN_DDES = "unDDes";
	public static final String UN_DUP_DT = "unDUpDt";
	public static final String UN_DQN_DR = "unDQnDr";
	public static final String UN_DID = "unDId";
	public static final String UN_DOV_TY = "unDOvTy";
	public static final String UN_DCR_DT = "unDCrDt";
	public static final String UN_GID = "unGId";
	public static final String UN_DMIN_QN = "unDMinQn";
	public static final String UN_DMIN_AM = "unDMinAm";
	public static final String UN_DAM_DR = "unDAmDr";

	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDDes
	 **/
	public static volatile SingularAttribute<UnitDiscount, String> unDDes;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDUpDt
	 **/
	public static volatile SingularAttribute<UnitDiscount, LocalDateTime> unDUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDQnDr
	 **/
	public static volatile SingularAttribute<UnitDiscount, BigDecimal> unDQnDr;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDId
	 **/
	public static volatile SingularAttribute<UnitDiscount, Long> unDId;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDOvTy
	 **/
	public static volatile SingularAttribute<UnitDiscount, OverlapType> unDOvTy;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDCrDt
	 **/
	public static volatile SingularAttribute<UnitDiscount, LocalDateTime> unDCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unGId
	 **/
	public static volatile SingularAttribute<UnitDiscount, Long> unGId;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDMinQn
	 **/
	public static volatile SingularAttribute<UnitDiscount, Integer> unDMinQn;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount
	 **/
	public static volatile EntityType<UnitDiscount> class_;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDMinAm
	 **/
	public static volatile SingularAttribute<UnitDiscount, BigDecimal> unDMinAm;
	
	/**
	 * @see com.goodee.beedan.entity.UnitDiscount#unDAmDr
	 **/
	public static volatile SingularAttribute<UnitDiscount, BigDecimal> unDAmDr;

}

