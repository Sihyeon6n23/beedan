package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(ExchangeRate.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ExchangeRate_ {

	public static final String ER_CR = "erCr";
	public static final String ER_RA = "erRa";
	public static final String ER_BA = "erBa";
	public static final String ER_ID = "erId";
	public static final String ER_CR_DT = "erCrDt";
	public static final String ER_FDT = "erFDt";

	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erCr
	 **/
	public static volatile SingularAttribute<ExchangeRate, String> erCr;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erRa
	 **/
	public static volatile SingularAttribute<ExchangeRate, BigDecimal> erRa;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erBa
	 **/
	public static volatile SingularAttribute<ExchangeRate, String> erBa;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erId
	 **/
	public static volatile SingularAttribute<ExchangeRate, Long> erId;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erCrDt
	 **/
	public static volatile SingularAttribute<ExchangeRate, LocalDateTime> erCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate
	 **/
	public static volatile EntityType<ExchangeRate> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ExchangeRate#erFDt
	 **/
	public static volatile SingularAttribute<ExchangeRate, LocalDateTime> erFDt;

}

