package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.QuoteStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(QuoteBase.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteBase_ {

	public static final String QU_STT = "quStt";
	public static final String QU_EXP_DT = "quExpDt";
	public static final String QU_SID = "quSid";
	public static final String NG_ID = "ngId";
	public static final String QU_RID = "quRid";
	public static final String QU_ID = "quId";
	public static final String QU_CRE_DT = "quCreDt";
	public static final String QU_CD = "quCd";
	public static final String QU_CON = "quCon";
	public static final String QU_AD_OP_YN = "quAdOpYn";
	public static final String QU_US_OP_YN = "quUsOpYn";
	public static final String QU_UPD_DT = "quUpdDt";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quStt
	 **/
	public static volatile SingularAttribute<QuoteBase, QuoteStatus> quStt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quExpDt
	 **/
	public static volatile SingularAttribute<QuoteBase, LocalDateTime> quExpDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quSid
	 **/
	public static volatile SingularAttribute<QuoteBase, Long> quSid;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#ngId
	 **/
	public static volatile SingularAttribute<QuoteBase, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quRid
	 **/
	public static volatile SingularAttribute<QuoteBase, Long> quRid;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quId
	 **/
	public static volatile SingularAttribute<QuoteBase, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quCreDt
	 **/
	public static volatile SingularAttribute<QuoteBase, LocalDateTime> quCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quCd
	 **/
	public static volatile SingularAttribute<QuoteBase, String> quCd;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quCon
	 **/
	public static volatile SingularAttribute<QuoteBase, String> quCon;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quAdOpYn
	 **/
	public static volatile SingularAttribute<QuoteBase, Boolean> quAdOpYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quUsOpYn
	 **/
	public static volatile SingularAttribute<QuoteBase, Boolean> quUsOpYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase#quUpdDt
	 **/
	public static volatile SingularAttribute<QuoteBase, LocalDateTime> quUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteBase
	 **/
	public static volatile EntityType<QuoteBase> class_;

}

