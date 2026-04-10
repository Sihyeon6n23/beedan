package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(QuoteShipFee.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteShipFee_ {

	public static final String QU_INFO_ID = "quInfoId";
	public static final String NG_ID = "ngId";
	public static final String QSF_UP_DT = "qsfUpDt";
	public static final String QSF_INS_AM = "qsfInsAm";
	public static final String QSF_TRSP_TY = "qsfTrspTy";
	public static final String QSF_DSC_AM = "qsfDscAm";
	public static final String QSF_ID = "qsfId";
	public static final String QSF_DTY = "qsfDty";
	public static final String QSF_VAT = "qsfVat";
	public static final String FA_ID = "faId";
	public static final String QSF_UN_QN = "qsfUnQn";
	public static final String QSF_DSC_R = "qsfDscR";
	public static final String QSF_CR_DT = "qsfCrDt";
	public static final String QSF_SR_YN = "qsfSrYn";
	public static final String QSF_TTL = "qsfTtl";
	public static final String QSF_FA_NM = "qsfFaNm";
	public static final String QSF_FA_CCD = "qsfFaCCd";
	public static final String QSF_PRT_AM = "qsfPrtAm";
	public static final String QSF_INS_YN = "qsfInsYn";
	public static final String QSF_SR_DES = "qsfSrDes";
	public static final String QSF_CST_AM = "qsfCstAm";
	public static final String QSF_CIF_AM = "qsfCifAm";
	public static final String QU_ID = "quId";
	public static final String QSF_SR_AM = "qsfSrAm";
	public static final String QSF_HS_CD = "qsfHsCd";
	public static final String QSF_DTY_R = "qsfDtyR";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#quInfoId
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Long> quInfoId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#ngId
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfUpDt
	 **/
	public static volatile SingularAttribute<QuoteShipFee, LocalDateTime> qsfUpDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfInsAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfInsAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfTrspTy
	 **/
	public static volatile SingularAttribute<QuoteShipFee, String> qsfTrspTy;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfDscAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfDscAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfId
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Long> qsfId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfDty
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfDty;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfVat
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfVat;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#faId
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Long> faId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfUnQn
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Integer> qsfUnQn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfDscR
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfDscR;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfCrDt
	 **/
	public static volatile SingularAttribute<QuoteShipFee, LocalDateTime> qsfCrDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfSrYn
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Boolean> qsfSrYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee
	 **/
	public static volatile EntityType<QuoteShipFee> class_;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfTtl
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfTtl;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfFaNm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, String> qsfFaNm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfFaCCd
	 **/
	public static volatile SingularAttribute<QuoteShipFee, String> qsfFaCCd;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfPrtAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfPrtAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfInsYn
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Boolean> qsfInsYn;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfSrDes
	 **/
	public static volatile SingularAttribute<QuoteShipFee, String> qsfSrDes;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfCstAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfCstAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfCifAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfCifAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#quId
	 **/
	public static volatile SingularAttribute<QuoteShipFee, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfSrAm
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfSrAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfHsCd
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfHsCd;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteShipFee#qsfDtyR
	 **/
	public static volatile SingularAttribute<QuoteShipFee, BigDecimal> qsfDtyR;

}

