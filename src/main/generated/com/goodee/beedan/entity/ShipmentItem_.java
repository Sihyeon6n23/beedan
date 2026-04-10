package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ShipmentItem.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ShipmentItem_ {

	public static final String SH_ITEM_ID = "shItemId";
	public static final String SHIPMENT = "shipment";
	public static final String ORDER_ITEM = "orderItem";
	public static final String SH_QN = "shQn";
	public static final String ORD_ITM_NM = "ordItmNm";

	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem#shItemId
	 **/
	public static volatile SingularAttribute<ShipmentItem, Long> shItemId;
	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem#shipment
	 **/
	public static volatile SingularAttribute<ShipmentItem, Shipment> shipment;
	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem#orderItem
	 **/
	public static volatile SingularAttribute<ShipmentItem, OrderItem> orderItem;
	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem#shQn
	 **/
	public static volatile SingularAttribute<ShipmentItem, Integer> shQn;
	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem#ordItmNm
	 **/
	public static volatile SingularAttribute<ShipmentItem, String> ordItmNm;
	
	/**
	 * @see com.goodee.beedan.entity.ShipmentItem
	 **/
	public static volatile EntityType<ShipmentItem> class_;

}

