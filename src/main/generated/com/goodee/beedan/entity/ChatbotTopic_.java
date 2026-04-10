package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(ChatbotTopic.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ChatbotTopic_ {

	public static final String CB_TP_LVL = "cbTpLvl";
	public static final String CB_TP_ORD = "cbTpOrd";
	public static final String CB_TP_CRE_DT = "cbTpCreDt";
	public static final String CB_TP_NM = "cbTpNm";
	public static final String CB_TP_USE_YN = "cbTpUseYn";
	public static final String CB_TP_UPD_DT = "cbTpUpdDt";
	public static final String CB_TP_ID = "cbTpId";
	public static final String CB_TP_PRN_ID = "cbTpPrnId";

	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpLvl
	 **/
	public static volatile SingularAttribute<ChatbotTopic, Integer> cbTpLvl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpOrd
	 **/
	public static volatile SingularAttribute<ChatbotTopic, Integer> cbTpOrd;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpCreDt
	 **/
	public static volatile SingularAttribute<ChatbotTopic, LocalDateTime> cbTpCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpNm
	 **/
	public static volatile SingularAttribute<ChatbotTopic, String> cbTpNm;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpUseYn
	 **/
	public static volatile SingularAttribute<ChatbotTopic, Boolean> cbTpUseYn;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpUpdDt
	 **/
	public static volatile SingularAttribute<ChatbotTopic, LocalDateTime> cbTpUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpId
	 **/
	public static volatile SingularAttribute<ChatbotTopic, Long> cbTpId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic
	 **/
	public static volatile EntityType<ChatbotTopic> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotTopic#cbTpPrnId
	 **/
	public static volatile SingularAttribute<ChatbotTopic, Long> cbTpPrnId;

}

