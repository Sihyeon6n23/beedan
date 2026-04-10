package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(HitStock.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class HitStock_ {

	public static final String HIT_ID = "hitId";
	public static final String HIT_DT = "hitDt";
	public static final String ST_ID = "stId";

	
	/**
	 * @see com.goodee.beedan.entity.HitStock#hitId
	 **/
	public static volatile SingularAttribute<HitStock, Long> hitId;
	
	/**
	 * @see com.goodee.beedan.entity.HitStock#hitDt
	 **/
	public static volatile SingularAttribute<HitStock, LocalDateTime> hitDt;
	
	/**
	 * @see com.goodee.beedan.entity.HitStock#stId
	 **/
	public static volatile SingularAttribute<HitStock, Long> stId;
	
	/**
	 * @see com.goodee.beedan.entity.HitStock
	 **/
	public static volatile EntityType<HitStock> class_;

}

