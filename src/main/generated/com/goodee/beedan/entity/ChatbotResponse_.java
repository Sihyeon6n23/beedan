package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(ChatbotResponse.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class ChatbotResponse_ {

	public static final String CB_RES_TTL = "cbResTtl";
	public static final String CB_RES_USE_YN = "cbResUseYn";
	public static final String CB_RES_CRE_DT = "cbResCreDt";
	public static final String CB_RES_UPD_DT = "cbResUpdDt";
	public static final String CB_RES_CON = "cbResCon";
	public static final String CB_RES_LNK_BTN_NM = "cbResLnkBtnNm";
	public static final String CB_RES_LNK_URL = "cbResLnkUrl";
	public static final String CB_TP_ID = "cbTpId";
	public static final String CB_RES_ID = "cbResId";

	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResTtl
	 **/
	public static volatile SingularAttribute<ChatbotResponse, String> cbResTtl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResUseYn
	 **/
	public static volatile SingularAttribute<ChatbotResponse, Boolean> cbResUseYn;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResCreDt
	 **/
	public static volatile SingularAttribute<ChatbotResponse, LocalDateTime> cbResCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResUpdDt
	 **/
	public static volatile SingularAttribute<ChatbotResponse, LocalDateTime> cbResUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResCon
	 **/
	public static volatile SingularAttribute<ChatbotResponse, String> cbResCon;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResLnkBtnNm
	 **/
	public static volatile SingularAttribute<ChatbotResponse, String> cbResLnkBtnNm;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResLnkUrl
	 **/
	public static volatile SingularAttribute<ChatbotResponse, String> cbResLnkUrl;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbTpId
	 **/
	public static volatile SingularAttribute<ChatbotResponse, Long> cbTpId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse#cbResId
	 **/
	public static volatile SingularAttribute<ChatbotResponse, Long> cbResId;
	
	/**
	 * @see com.goodee.beedan.entity.ChatbotResponse
	 **/
	public static volatile EntityType<ChatbotResponse> class_;

}

