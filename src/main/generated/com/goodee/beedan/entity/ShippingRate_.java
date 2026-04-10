package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.TransportType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(ShippingRate.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ShippingRate_ {

	public static final String SR_LG_AM = "srLgAm";
	public static final String SR_YN = "srYn";
	public static final String SR_UP_DT = "srUpDt";
	public static final String SR_MD_AM = "srMdAm";
	public static final String SR_CCD = "srCCd";
	public static final String SR_SM_AM = "srSmAm";
	public static final String SR_ID = "srId";
	public static final String SR_LG_QN = "srLgQn";
	public static final String SR_DES = "srDes";
	public static final String SR_MD_QN = "srMdQn";
	public static final String SR_SM_QN = "srSmQn";
	public static final String SR_CR_DT = "srCrDt";
	public static final String SR_TRSP_TY = "srTrspTy";

	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srLgAm
	 **/
	public static volatile SingularAttribute<ShippingRate, BigDecimal> srLgAm;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srYn
	 **/
	public static volatile SingularAttribute<ShippingRate, Boolean> srYn;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srUpDt
	 **/
	public static volatile SingularAttribute<ShippingRate, LocalDateTime> srUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srMdAm
	 **/
	public static volatile SingularAttribute<ShippingRate, BigDecimal> srMdAm;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srCCd
	 **/
	public static volatile SingularAttribute<ShippingRate, String> srCCd;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srSmAm
	 **/
	public static volatile SingularAttribute<ShippingRate, BigDecimal> srSmAm;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srId
	 **/
	public static volatile SingularAttribute<ShippingRate, Long> srId;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srLgQn
	 **/
	public static volatile SingularAttribute<ShippingRate, Integer> srLgQn;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srDes
	 **/
	public static volatile SingularAttribute<ShippingRate, String> srDes;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srMdQn
	 **/
	public static volatile SingularAttribute<ShippingRate, Integer> srMdQn;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate
	 **/
	public static volatile EntityType<ShippingRate> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srSmQn
	 **/
	public static volatile SingularAttribute<ShippingRate, Integer> srSmQn;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srCrDt
	 **/
	public static volatile SingularAttribute<ShippingRate, LocalDateTime> srCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingRate#srTrspTy
	 **/
	public static volatile SingularAttribute<ShippingRate, TransportType> srTrspTy;

}

