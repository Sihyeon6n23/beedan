package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(QuoteInfo.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class QuoteInfo_ {

	public static final String QU_INFO_ID = "quInfoId";
	public static final String QU_INFO_DD_AM = "quInfoDdAm";
	public static final String QU_INFO_TP = "quInfoTp";
	public static final String QU_INFO_DSR_DT = "quInfoDsrDt";
	public static final String SI_ID = "siId";
	public static final String QU_INFO_DIS_TP = "quInfoDisTp";
	public static final String NG_ID = "ngId";
	public static final String QU_INFO_DOM_SHI_FE = "quInfoDomShiFe";
	public static final String STI_ID = "stiId";
	public static final String QU_ID = "quId";
	public static final String QU_INFO_INT_SHI_FE = "quInfoIntShiFe";
	public static final String QU_INFO_SRV_FE_AM = "quInfoSrvFeAm";
	public static final String QU_INFO_SRV_FE = "quInfoSrvFe";
	public static final String QU_INFO_DD_EX_AM = "quInfoDdExAm";
	public static final String QU_INFO_EXC_RT = "quInfoExcRt";
	public static final String QU_INFO_TAX = "quInfoTax";
	public static final String BGP_ID = "bgpId";
	public static final String FP_ID = "fpId";
	public static final String QU_INFO_PS = "quInfoPs";
	public static final String QU_INFO_CUR_CD = "quInfoCurCd";
	public static final String QU_INFO_SRV_FE_R = "quInfoSrvFeR";
	public static final String QU_INFO_TTL_SHI_FE = "quInfoTtlShiFe";

	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> quInfoId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoDdAm
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoDdAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoTp
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoTp;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoDsrDt
	 **/
	public static volatile SingularAttribute<QuoteInfo, LocalDateTime> quInfoDsrDt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#siId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> siId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoDisTp
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoDisTp;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#ngId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoDomShiFe
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoDomShiFe;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#stiId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> stiId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoIntShiFe
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoIntShiFe;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoSrvFeAm
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoSrvFeAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoSrvFe
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoSrvFe;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoDdExAm
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoDdExAm;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoExcRt
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoExcRt;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoTax
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoTax;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#bgpId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> bgpId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#fpId
	 **/
	public static volatile SingularAttribute<QuoteInfo, Long> fpId;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoPs
	 **/
	public static volatile SingularAttribute<QuoteInfo, String> quInfoPs;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoCurCd
	 **/
	public static volatile SingularAttribute<QuoteInfo, String> quInfoCurCd;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo
	 **/
	public static volatile EntityType<QuoteInfo> class_;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoSrvFeR
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoSrvFeR;
	
	/**
	 * @see com.goodee.beedan.entity.QuoteInfo#quInfoTtlShiFe
	 **/
	public static volatile SingularAttribute<QuoteInfo, BigDecimal> quInfoTtlShiFe;

}

