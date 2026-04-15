package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(PageView.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class PageView_ {

	public static final String PV_REF_ID = "pvRefId";
	public static final String PV_ID = "pvId";
	public static final String PV_PAGE = "pvPage";
	public static final String PV_MEM_ID = "pvMemId";
	public static final String PV_DWELL_SEC = "pvDwellSec";
	public static final String PV_CRE_DT = "pvCreDt";

	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvRefId
	 **/
	public static volatile SingularAttribute<PageView, Long> pvRefId;
	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvId
	 **/
	public static volatile SingularAttribute<PageView, Long> pvId;
	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvPage
	 **/
	public static volatile SingularAttribute<PageView, String> pvPage;
	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvMemId
	 **/
	public static volatile SingularAttribute<PageView, Long> pvMemId;
	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvDwellSec
	 **/
	public static volatile SingularAttribute<PageView, Integer> pvDwellSec;
	
	/**
	 * @see com.goodee.beedan.entity.PageView#pvCreDt
	 **/
	public static volatile SingularAttribute<PageView, LocalDateTime> pvCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.PageView
	 **/
	public static volatile EntityType<PageView> class_;

}

