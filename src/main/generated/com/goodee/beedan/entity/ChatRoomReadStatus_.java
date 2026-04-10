package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(ChatRoomReadStatus.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ChatRoomReadStatus_ {

	public static final String CH_RO_RE_ST_UNR_YN = "chRoReStUnrYn";
	public static final String CH_MS_LAST_ID = "chMsLastId";
	public static final String CH_RO_RE_ST_LAST_DT = "chRoReStLastDt";
	public static final String CH_RO_RE_ST_ID = "chRoReStId";
	public static final String CH_RO_ID = "chRoId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#chRoReStUnrYn
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, Boolean> chRoReStUnrYn;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#chMsLastId
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, Long> chMsLastId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#chRoReStLastDt
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, LocalDateTime> chRoReStLastDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus
	 **/
	public static volatile EntityType<ChatRoomReadStatus> class_;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#chRoReStId
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, Long> chRoReStId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#chRoId
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, Long> chRoId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatRoomReadStatus#memId
	 **/
	public static volatile SingularAttribute<ChatRoomReadStatus, Long> memId;

}

