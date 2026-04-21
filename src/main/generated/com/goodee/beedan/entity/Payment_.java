package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.PaymentMethod;
import com.goodee.beedan.common.constant.PaymentStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(Payment.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Payment_ {

	public static final String PY_MT = "pyMt";
	public static final String PY_VB_NM = "pyVbNm";
	public static final String PY_CRE_DT = "pyCreDt";
	public static final String PY_TXB_YN = "pyTxbYn";
	public static final String NG_ID = "ngId";
	public static final String PY_STT = "pyStt";
	public static final String QU_ID = "quId";
	public static final String PY_INFO_TAX_NM = "pyInfoTaxNm";
	public static final String PY_ID = "pyId";
	public static final String PY_PG_NM = "pyPgNm";
	public static final String PY_SP_VL = "pySpVl";
	public static final String PY_TT_AM = "pyTtAm";
	public static final String PY_NM = "pyNm";
	public static final String PY_PD_AT = "pyPdAt";
	public static final String PY_RS = "pyRs";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyMt
	 **/
	public static volatile SingularAttribute<Payment, PaymentMethod> pyMt;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyVbNm
	 **/
	public static volatile SingularAttribute<Payment, String> pyVbNm;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyCreDt
	 **/
	public static volatile SingularAttribute<Payment, LocalDateTime> pyCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyTxbYn
	 **/
	public static volatile SingularAttribute<Payment, Boolean> pyTxbYn;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#ngId
	 **/
	public static volatile SingularAttribute<Payment, Long> ngId;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyStt
	 **/
	public static volatile SingularAttribute<Payment, PaymentStatus> pyStt;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#quId
	 **/
	public static volatile SingularAttribute<Payment, Long> quId;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyInfoTaxNm
	 **/
	public static volatile SingularAttribute<Payment, String> pyInfoTaxNm;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyId
	 **/
	public static volatile SingularAttribute<Payment, Long> pyId;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyPgNm
	 **/
	public static volatile SingularAttribute<Payment, String> pyPgNm;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pySpVl
	 **/
	public static volatile SingularAttribute<Payment, BigDecimal> pySpVl;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyTtAm
	 **/
	public static volatile SingularAttribute<Payment, BigDecimal> pyTtAm;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyNm
	 **/
	public static volatile SingularAttribute<Payment, Long> pyNm;
	
	/**
	 * @see com.goodee.beedan.entity.Payment
	 **/
	public static volatile EntityType<Payment> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyPdAt
	 **/
	public static volatile SingularAttribute<Payment, LocalDateTime> pyPdAt;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#pyRs
	 **/
	public static volatile SingularAttribute<Payment, String> pyRs;
	
	/**
	 * @see com.goodee.beedan.entity.Payment#memId
	 **/
	public static volatile SingularAttribute<Payment, Long> memId;

}

