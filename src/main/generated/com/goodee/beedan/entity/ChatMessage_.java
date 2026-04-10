package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ChatMessageSenderType;
import com.goodee.beedan.common.constant.ChatMessageType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(ChatMessage.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ChatMessage_ {

	public static final String CH_MS_CON = "chMsCon";
	public static final String CH_MS_LNK_TTL = "chMsLnkTtl";
	public static final String CH_MS_ID = "chMsId";
	public static final String CH_MS_CRE_DT = "chMsCreDt";
	public static final String CH_MS_SEN_TY = "chMsSenTy";
	public static final String CH_MS_TP = "chMsTp";
	public static final String CH_MS_LNK_URL = "chMsLnkUrl";
	public static final String CH_RO_ID = "chRoId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsCon
	 **/
	public static volatile SingularAttribute<ChatMessage, String> chMsCon;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsLnkTtl
	 **/
	public static volatile SingularAttribute<ChatMessage, String> chMsLnkTtl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsId
	 **/
	public static volatile SingularAttribute<ChatMessage, Long> chMsId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsCreDt
	 **/
	public static volatile SingularAttribute<ChatMessage, LocalDateTime> chMsCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsSenTy
	 **/
	public static volatile SingularAttribute<ChatMessage, ChatMessageSenderType> chMsSenTy;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsTp
	 **/
	public static volatile SingularAttribute<ChatMessage, ChatMessageType> chMsTp;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chMsLnkUrl
	 **/
	public static volatile SingularAttribute<ChatMessage, String> chMsLnkUrl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage
	 **/
	public static volatile EntityType<ChatMessage> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#chRoId
	 **/
	public static volatile SingularAttribute<ChatMessage, Long> chRoId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatMessage#memId
	 **/
	public static volatile SingularAttribute<ChatMessage, Long> memId;

}

