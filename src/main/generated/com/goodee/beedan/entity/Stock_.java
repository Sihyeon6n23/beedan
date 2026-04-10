package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(Stock.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Stock_ {

	public static final String ST_CRE_DT = "stCreDt";
	public static final String ST_REQ_YN = "stReqYn";
	public static final String BR_ID = "brId";
	public static final String ST_REQ_MEM_ID = "stReqMemId";
	public static final String ST_CRA_DT = "stCraDt";
	public static final String ST_WIS_CNT = "stWisCnt";
	public static final String ST_CAT_NM = "stCatNm";
	public static final String ST_ID = "stId";
	public static final String ST_BR_NM = "stBrNm";
	public static final String CAT_ID = "catId";
	public static final String ST_EXP_YN = "stExpYn";
	public static final String ST_DEL_YN = "stDelYn";
	public static final String ST_CD = "stCd";
	public static final String ST_PR = "stPr";
	public static final String ST_CUR = "stCur";
	public static final String ST_NM = "stNm";
	public static final String ST_IMG_URL = "stImgUrl";
	public static final String ST_UPD_DT = "stUpdDt";
	public static final String ST_USE_YN = "stUseYn";
	public static final String ST_PUR_CNT = "stPurCnt";

	
	/**
	 * @see com.goodee.beedan.entity.Stock#stCreDt
	 **/
	public static volatile SingularAttribute<Stock, LocalDateTime> stCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stReqYn
	 **/
	public static volatile SingularAttribute<Stock, Boolean> stReqYn;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#brId
	 **/
	public static volatile SingularAttribute<Stock, Long> brId;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stReqMemId
	 **/
	public static volatile SingularAttribute<Stock, Long> stReqMemId;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stCraDt
	 **/
	public static volatile SingularAttribute<Stock, LocalDateTime> stCraDt;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stWisCnt
	 **/
	public static volatile SingularAttribute<Stock, Long> stWisCnt;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stCatNm
	 **/
	public static volatile SingularAttribute<Stock, String> stCatNm;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stId
	 **/
	public static volatile SingularAttribute<Stock, Long> stId;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stBrNm
	 **/
	public static volatile SingularAttribute<Stock, String> stBrNm;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#catId
	 **/
	public static volatile SingularAttribute<Stock, Long> catId;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stExpYn
	 **/
	public static volatile SingularAttribute<Stock, Boolean> stExpYn;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stDelYn
	 **/
	public static volatile SingularAttribute<Stock, Boolean> stDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stCd
	 **/
	public static volatile SingularAttribute<Stock, String> stCd;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stPr
	 **/
	public static volatile SingularAttribute<Stock, BigDecimal> stPr;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stCur
	 **/
	public static volatile SingularAttribute<Stock, String> stCur;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stNm
	 **/
	public static volatile SingularAttribute<Stock, String> stNm;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stImgUrl
	 **/
	public static volatile SingularAttribute<Stock, String> stImgUrl;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stUpdDt
	 **/
	public static volatile SingularAttribute<Stock, LocalDateTime> stUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stUseYn
	 **/
	public static volatile SingularAttribute<Stock, Boolean> stUseYn;
	
	/**
	 * @see com.goodee.beedan.entity.Stock
	 **/
	public static volatile EntityType<Stock> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Stock#stPurCnt
	 **/
	public static volatile SingularAttribute<Stock, Long> stPurCnt;

}

