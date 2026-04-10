package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(ChatRoom.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ChatRoom_ {

	public static final String CH_RO_ASG_DT = "chRoAsgDt";
	public static final String CH_RO_CLS_RSN = "chRoClsRsn";
	public static final String CH_RO_TTL = "chRoTtl";
	public static final String CH_RO_CLS_DT = "chRoClsDt";
	public static final String CH_RO_LAST_MS_DT = "chRoLastMsDt";
	public static final String CH_RO_STT = "chRoStt";
	public static final String CH_RO_CRE_DT = "chRoCreDt";
	public static final String MEM_AD_ID = "memAdId";
	public static final String CH_RO_ID = "chRoId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoAsgDt
	 **/
	public static volatile SingularAttribute<ChatRoom, LocalDateTime> chRoAsgDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoClsRsn
	 **/
	public static volatile SingularAttribute<ChatRoom, ChatRoomCloseReason> chRoClsRsn;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoTtl
	 **/
	public static volatile SingularAttribute<ChatRoom, String> chRoTtl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoClsDt
	 **/
	public static volatile SingularAttribute<ChatRoom, LocalDateTime> chRoClsDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoLastMsDt
	 **/
	public static volatile SingularAttribute<ChatRoom, LocalDateTime> chRoLastMsDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoStt
	 **/
	public static volatile SingularAttribute<ChatRoom, ChatRoomStatus> chRoStt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoCreDt
	 **/
	public static volatile SingularAttribute<ChatRoom, LocalDateTime> chRoCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#memAdId
	 **/
	public static volatile SingularAttribute<ChatRoom, Long> memAdId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom
	 **/
	public static volatile EntityType<ChatRoom> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#chRoId
	 **/
	public static volatile SingularAttribute<ChatRoom, Long> chRoId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoom#memId
	 **/
	public static volatile SingularAttribute<ChatRoom, Long> memId;

}

