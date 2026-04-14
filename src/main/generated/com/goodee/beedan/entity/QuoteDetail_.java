package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;

@StaticMetamodel(QuoteDetail.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteDetail_ {

	public static final String QU_INFO_ID = "quInfoId";
	public static final String QU_UQN = "quUQn";
	public static final String QU_DT_RC_ADR = "quDtRcAdr";
	public static final String QU_DT_RC_MEMO = "quDtRcMemo";
	public static final String NG_ID = "ngId";
	public static final String QU_DT_ID = "quDtId";
	public static final String QU_ID = "quId";
	public static final String ST_ID = "stId";
	public static final String UN_GID = "unGId";
	public static final String QU_DT_GRP = "quDtGrp";
	public static final String UN_GNM = "unGNm";
	public static final String QU_DT_KR_PR = "quDtKrPr";
	public static final String QU_DT_RC_ADR_DT = "quDtRcAdrDt";
	public static final String QU_DT_PR = "quDtPr";
	public static final String QU_DT_QN = "quDtQn";
	public static final String QU_DT_RC_NM = "quDtRcNm";
	public static final String FA_NM = "faNm";
	public static final String ST_NM = "stNm";
	public static final String FA_ID = "faId";
	public static final String RC_ID = "rcId";
	public static final String QU_DT_RC_PHN = "quDtRcPhn";
	public static final String QU_DT_FG_PR = "quDtFgPr";
	public static final String QU_DT_RC_IAM_YN = "quDtRcIamYn";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quInfoId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> quInfoId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quUQn
	 **/
	public static volatile SingularAttribute<QuoteDetail, Integer> quUQn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcAdr
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> quDtRcAdr;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcMemo
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> quDtRcMemo;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#ngId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> quDtId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#stId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> stId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#unGId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> unGId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtGrp
	 **/
	public static volatile SingularAttribute<QuoteDetail, Integer> quDtGrp;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#unGNm
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> unGNm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtKrPr
	 **/
	public static volatile SingularAttribute<QuoteDetail, BigDecimal> quDtKrPr;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcAdrDt
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> quDtRcAdrDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtPr
	 **/
	public static volatile SingularAttribute<QuoteDetail, BigDecimal> quDtPr;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtQn
	 **/
	public static volatile SingularAttribute<QuoteDetail, Integer> quDtQn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcNm
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> quDtRcNm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#faNm
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> faNm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#stNm
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> stNm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#faId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> faId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#rcId
	 **/
	public static volatile SingularAttribute<QuoteDetail, Long> rcId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcPhn
	 **/
	public static volatile SingularAttribute<QuoteDetail, String> quDtRcPhn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtFgPr
	 **/
	public static volatile SingularAttribute<QuoteDetail, BigDecimal> quDtFgPr;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail
	 **/
	public static volatile EntityType<QuoteDetail> class_;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteDetail#quDtRcIamYn
	 **/
	public static volatile SingularAttribute<QuoteDetail, Boolean> quDtRcIamYn;

}

