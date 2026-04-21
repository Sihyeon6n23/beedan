package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(OrderItem.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class OrderItem_ {

	public static final String ORD_ITM_CRE_DT = "ordItmCreDt";
	public static final String ORD_ITM_THUMB_KEY = "ordItmThumbKey";
	public static final String ORD_ITM_ID = "ordItmId";
	public static final String ORD_ITM_ST_URL = "ordItmStUrl";
	public static final String ORD_ITM_AM = "ordItmAm";
	public static final String ORD_ITM_QN = "ordItmQn";
	public static final String ORD_ITM_NM = "ordItmNm";
	public static final String ORD_ITM_UPD_DT = "ordItmUpdDt";
	public static final String ORDER = "order";

	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmCreDt
	 **/
	public static volatile SingularAttribute<OrderItem, LocalDateTime> ordItmCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmThumbKey
	 **/
	public static volatile SingularAttribute<OrderItem, String> ordItmThumbKey;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmId
	 **/
	public static volatile SingularAttribute<OrderItem, Long> ordItmId;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmStUrl
	 **/
	public static volatile SingularAttribute<OrderItem, String> ordItmStUrl;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmAm
	 **/
	public static volatile SingularAttribute<OrderItem, BigDecimal> ordItmAm;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmQn
	 **/
	public static volatile SingularAttribute<OrderItem, Integer> ordItmQn;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmNm
	 **/
	public static volatile SingularAttribute<OrderItem, String> ordItmNm;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#ordItmUpdDt
	 **/
	public static volatile SingularAttribute<OrderItem, LocalDateTime> ordItmUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem
	 **/
	public static volatile EntityType<OrderItem> class_;
	
	/**
	 * @see com.goodee.beedan.entity.OrderItem#order
	 **/
	public static volatile SingularAttribute<OrderItem, Order> order;

}

