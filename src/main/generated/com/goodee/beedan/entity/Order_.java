package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.OrderStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(Order.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Order_ {

	public static final String ORD_BASE_ID = "ordBaseId";
	public static final String ORD_BASE_ADR_DT = "ordBaseAdrDt";
	public static final String ORD_BASE_MSG = "ordBaseMsg";
	public static final String ORD_BASE_CRE_DT = "ordBaseCreDt";
	public static final String ORD_BASE_TT_AM = "ordBaseTtAm";
	public static final String ORD_BASE_UPD_DT = "ordBaseUpdDt";
	public static final String ORD_BASE_STT = "ordBaseStt";
	public static final String SHIPMENTS = "shipments";
	public static final String ORDER_ITEMS = "orderItems";
	public static final String ORD_BASE_RCV_NM = "ordBaseRcvNm";
	public static final String ORD_BASE_ADR = "ordBaseAdr";
	public static final String MEMBER = "member";
	public static final String ORD_BASE_NO = "ordBaseNo";

	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseId
	 **/
	public static volatile SingularAttribute<Order, Long> ordBaseId;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseAdrDt
	 **/
	public static volatile SingularAttribute<Order, String> ordBaseAdrDt;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseMsg
	 **/
	public static volatile SingularAttribute<Order, String> ordBaseMsg;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseCreDt
	 **/
	public static volatile SingularAttribute<Order, LocalDateTime> ordBaseCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseTtAm
	 **/
	public static volatile SingularAttribute<Order, BigDecimal> ordBaseTtAm;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseUpdDt
	 **/
	public static volatile SingularAttribute<Order, LocalDateTime> ordBaseUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseStt
	 **/
	public static volatile SingularAttribute<Order, OrderStatus> ordBaseStt;
	
	/**
	 * @see com.goodee.beedan.entity.Order#shipments
	 **/
	public static volatile ListAttribute<Order, Shipment> shipments;
	
	/**
	 * @see com.goodee.beedan.entity.Order#orderItems
	 **/
	public static volatile ListAttribute<Order, OrderItem> orderItems;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseRcvNm
	 **/
	public static volatile SingularAttribute<Order, String> ordBaseRcvNm;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseAdr
	 **/
	public static volatile SingularAttribute<Order, String> ordBaseAdr;
	
	/**
	 * @see com.goodee.beedan.entity.Order#member
	 **/
	public static volatile SingularAttribute<Order, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.Order
	 **/
	public static volatile EntityType<Order> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Order#ordBaseNo
	 **/
	public static volatile SingularAttribute<Order, String> ordBaseNo;

}

