package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(DomesticDeliveryRate.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class DomesticDeliveryRate_ {

	public static final String DDR_EAM = "ddrEAm";
	public static final String DDR_YN = "ddrYn";
	public static final String DDR_DES = "ddrDes";
	public static final String DDR_CR_DT = "ddrCrDt";
	public static final String DDR_ID = "ddrId";
	public static final String DDR_RGN = "ddrRgn";
	public static final String DDR_AM = "ddrAm";
	public static final String DDR_UP_DT = "ddrUpDt";

	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrEAm
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, BigDecimal> ddrEAm;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrYn
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, Boolean> ddrYn;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrDes
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, String> ddrDes;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrCrDt
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, LocalDateTime> ddrCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate
	 **/
	public static volatile EntityType<DomesticDeliveryRate> class_;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrId
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, Long> ddrId;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrRgn
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, String> ddrRgn;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrAm
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, BigDecimal> ddrAm;
	
	/**
	 * @see com.goodee.beedan.entity.DomesticDeliveryRate#ddrUpDt
	 **/
	public static volatile SingularAttribute<DomesticDeliveryRate, LocalDateTime> ddrUpDt;

}

