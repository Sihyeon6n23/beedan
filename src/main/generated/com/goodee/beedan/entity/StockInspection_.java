package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(StockInspection.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class StockInspection_ {

	public static final String STI_CR_DT = "stiCrDt";
	public static final String STI_YN = "stiYn";
	public static final String STI_ID = "stiId";
	public static final String STI_DES = "stiDes";
	public static final String STI_NM = "stiNm";
	public static final String STI_UP_DT = "stiUpDt";
	public static final String STI_AM = "stiAm";

	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiCrDt
	 **/
	public static volatile SingularAttribute<StockInspection, LocalDateTime> stiCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiYn
	 **/
	public static volatile SingularAttribute<StockInspection, Boolean> stiYn;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiId
	 **/
	public static volatile SingularAttribute<StockInspection, Long> stiId;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiDes
	 **/
	public static volatile SingularAttribute<StockInspection, String> stiDes;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection
	 **/
	public static volatile EntityType<StockInspection> class_;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiNm
	 **/
	public static volatile SingularAttribute<StockInspection, String> stiNm;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiUpDt
	 **/
	public static volatile SingularAttribute<StockInspection, LocalDateTime> stiUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.StockInspection#stiAm
	 **/
	public static volatile SingularAttribute<StockInspection, BigDecimal> stiAm;

}

