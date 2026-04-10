package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ShipmentStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Shipment.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Shipment_ {

	public static final String SH_CAR_CD = "shCarCd";
	public static final String SH_UPD_DT = "shUpdDt";
	public static final String SH_ID = "shId";
	public static final String SH_CUS_NO = "shCusNo";
	public static final String SH_MSG = "shMsg";
	public static final String SH_MBL_NO = "shMblNo";
	public static final String SHIPMENT_ITEMS = "shipmentItems";
	public static final String SH_ADR_DT = "shAdrDt";
	public static final String SH_CUS_STT = "shCusStt";
	public static final String SH_CAN_YN = "shCanYn";
	public static final String SH_TRA_NO = "shTraNo";
	public static final String SH_HBL_NO = "shHblNo";
	public static final String SH_RCV_NM = "shRcvNm";
	public static final String SH_STT = "shStt";
	public static final String SH_CRE_DT = "shCreDt";
	public static final String SH_ADR = "shAdr";
	public static final String ORDER = "order";

	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shCarCd
	 **/
	public static volatile SingularAttribute<Shipment, String> shCarCd;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shUpdDt
	 **/
	public static volatile SingularAttribute<Shipment, LocalDateTime> shUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shId
	 **/
	public static volatile SingularAttribute<Shipment, Long> shId;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shCusNo
	 **/
	public static volatile SingularAttribute<Shipment, String> shCusNo;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shMsg
	 **/
	public static volatile SingularAttribute<Shipment, String> shMsg;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shMblNo
	 **/
	public static volatile SingularAttribute<Shipment, String> shMblNo;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shipmentItems
	 **/
	public static volatile ListAttribute<Shipment, ShipmentItem> shipmentItems;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shAdrDt
	 **/
	public static volatile SingularAttribute<Shipment, String> shAdrDt;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shCusStt
	 **/
	public static volatile SingularAttribute<Shipment, String> shCusStt;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shCanYn
	 **/
	public static volatile SingularAttribute<Shipment, Boolean> shCanYn;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shTraNo
	 **/
	public static volatile SingularAttribute<Shipment, String> shTraNo;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shHblNo
	 **/
	public static volatile SingularAttribute<Shipment, String> shHblNo;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shRcvNm
	 **/
	public static volatile SingularAttribute<Shipment, String> shRcvNm;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shStt
	 **/
	public static volatile SingularAttribute<Shipment, ShipmentStatus> shStt;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shCreDt
	 **/
	public static volatile SingularAttribute<Shipment, LocalDateTime> shCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment
	 **/
	public static volatile EntityType<Shipment> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#shAdr
	 **/
	public static volatile SingularAttribute<Shipment, String> shAdr;
	
	/**
	 * @see com.goodee.beedan.entity.Shipment#order
	 **/
	public static volatile SingularAttribute<Shipment, Order> order;

}

