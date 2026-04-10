package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Factory.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Factory_ {

	public static final String FA_AD = "faAd";
	public static final String FA_CCD = "faCCd";
	public static final String BR_ID = "brId";
	public static final String FA_NM = "faNm";
	public static final String FA_CTY = "faCty";
	public static final String FA_ID = "faId";
	public static final String FA_UP_DT = "faUpDt";
	public static final String FA_YN = "faYn";
	public static final String FA_CR_DT = "faCrDt";

	
	/**
	 * @see com.goodee.beedan.entity.Factory#faAd
	 **/
	public static volatile SingularAttribute<Factory, String> faAd;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faCCd
	 **/
	public static volatile SingularAttribute<Factory, String> faCCd;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#brId
	 **/
	public static volatile SingularAttribute<Factory, Long> brId;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faNm
	 **/
	public static volatile SingularAttribute<Factory, String> faNm;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faCty
	 **/
	public static volatile SingularAttribute<Factory, String> faCty;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faId
	 **/
	public static volatile SingularAttribute<Factory, Long> faId;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faUpDt
	 **/
	public static volatile SingularAttribute<Factory, LocalDateTime> faUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.Factory
	 **/
	public static volatile EntityType<Factory> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faYn
	 **/
	public static volatile SingularAttribute<Factory, Boolean> faYn;
	
	/**
	 * @see com.goodee.beedan.entity.Factory#faCrDt
	 **/
	public static volatile SingularAttribute<Factory, LocalDateTime> faCrDt;

}

