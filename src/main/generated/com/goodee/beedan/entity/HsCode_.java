package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;

@StaticMetamodel(HsCode.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class HsCode_ {

	public static final String CAT_ID = "catId";
	public static final String DES = "des";
	public static final String HS_ID = "hsId";
	public static final String HS_DU_RA = "hsDuRa";
	public static final String HS_NM = "hsNm";
	public static final String HS_CD = "hsCd";

	
	/**
	 * @see com.goodee.beedan.entity.HsCode#catId
	 **/
	public static volatile SingularAttribute<HsCode, Long> catId;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode#des
	 **/
	public static volatile SingularAttribute<HsCode, String> des;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode#hsId
	 **/
	public static volatile SingularAttribute<HsCode, Long> hsId;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode#hsDuRa
	 **/
	public static volatile SingularAttribute<HsCode, BigDecimal> hsDuRa;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode#hsNm
	 **/
	public static volatile SingularAttribute<HsCode, String> hsNm;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode#hsCd
	 **/
	public static volatile SingularAttribute<HsCode, String> hsCd;
	
	/**
	 * @see com.goodee.beedan.entity.HsCode
	 **/
	public static volatile EntityType<HsCode> class_;

}

