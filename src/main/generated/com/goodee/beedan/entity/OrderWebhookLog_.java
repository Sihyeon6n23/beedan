package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(OrderWebhookLog.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class OrderWebhookLog_ {

	public static final String PY_ID = "pyId";
	public static final String OWL_DIR = "owlDir";
	public static final String OWL_URL = "owlUrl";
	public static final String OWL_ERR_MSG = "owlErrMsg";
	public static final String OWL_STT = "owlStt";
	public static final String OWL_HTTP_STT = "owlHttpStt";
	public static final String QU_ID = "quId";
	public static final String OWL_CRE_DT = "owlCreDt";
	public static final String OWL_REQ_BODY = "owlReqBody";
	public static final String OWL_ID = "owlId";
	public static final String OWL_RES_BODY = "owlResBody";

	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#pyId
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, Long> pyId;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlDir
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlDir;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlUrl
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlUrl;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlErrMsg
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlErrMsg;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlStt
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlStt;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlHttpStt
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, Integer> owlHttpStt;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#quId
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlCreDt
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, LocalDateTime> owlCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlReqBody
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlReqBody;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlId
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, Long> owlId;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog
	 **/
	public static volatile EntityType<OrderWebhookLog> class_;
	
	/**
	 * @see com.goodee.beedan.entity.OrderWebhookLog#owlResBody
	 **/
	public static volatile SingularAttribute<OrderWebhookLog, String> owlResBody;

}

