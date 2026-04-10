package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(UnitGroup.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class UnitGroup_ {

	public static final String UN_GYN = "unGYn";
	public static final String UN_GUP_DT = "unGUpDt";
	public static final String UN_GQN = "unGQn";
	public static final String UN_GID = "unGId";
	public static final String UN_GCR_DT = "unGCrDt";
	public static final String UN_GNM = "unGNm";

	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGYn
	 **/
	public static volatile SingularAttribute<UnitGroup, Boolean> unGYn;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGUpDt
	 **/
	public static volatile SingularAttribute<UnitGroup, LocalDateTime> unGUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGQn
	 **/
	public static volatile SingularAttribute<UnitGroup, Integer> unGQn;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGId
	 **/
	public static volatile SingularAttribute<UnitGroup, Long> unGId;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGCrDt
	 **/
	public static volatile SingularAttribute<UnitGroup, LocalDateTime> unGCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup
	 **/
	public static volatile EntityType<UnitGroup> class_;
	
	/**
	 * @see com.goodee.beedan.entity.UnitGroup#unGNm
	 **/
	public static volatile SingularAttribute<UnitGroup, String> unGNm;

}

