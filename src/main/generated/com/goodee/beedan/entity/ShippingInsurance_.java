package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(ShippingInsurance.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ShippingInsurance_ {

	public static final String SI_NM = "siNm";
	public static final String SI_CR_DT = "siCrDt";
	public static final String SI_ID = "siId";
	public static final String SI_AM = "siAm";
	public static final String SI_DES = "siDes";
	public static final String SI_UP_DT = "siUpDt";
	public static final String SI_YN = "siYn";

	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siNm
	 **/
	public static volatile SingularAttribute<ShippingInsurance, String> siNm;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siCrDt
	 **/
	public static volatile SingularAttribute<ShippingInsurance, LocalDateTime> siCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siId
	 **/
	public static volatile SingularAttribute<ShippingInsurance, Long> siId;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siAm
	 **/
	public static volatile SingularAttribute<ShippingInsurance, BigDecimal> siAm;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siDes
	 **/
	public static volatile SingularAttribute<ShippingInsurance, String> siDes;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siUpDt
	 **/
	public static volatile SingularAttribute<ShippingInsurance, LocalDateTime> siUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance
	 **/
	public static volatile EntityType<ShippingInsurance> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ShippingInsurance#siYn
	 **/
	public static volatile SingularAttribute<ShippingInsurance, Boolean> siYn;

}

