package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(CrawlingUrl.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class CrawlingUrl_ {

	public static final String URL_SEL_ITEM = "urlSelItem";
	public static final String BR_ID = "brId";
	public static final String URL_AT_YN = "urlAtYn";
	public static final String URL_SEL_IMG = "urlSelImg";
	public static final String URL_ID = "urlId";
	public static final String URL_CUR = "urlCur";
	public static final String CAT_ID = "catId";
	public static final String URL_SEL_PR = "urlSelPr";
	public static final String URL_TY = "urlTy";
	public static final String URL_USE_YN = "urlUseYn";
	public static final String URL_SEL_NM = "urlSelNm";
	public static final String URL_UPD_DT = "urlUpdDt";
	public static final String URL_DEL_YN = "urlDelYn";
	public static final String URL_URL = "urlUrl";

	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlSelItem
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlSelItem;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#brId
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Long> brId;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlAtYn
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Boolean> urlAtYn;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlSelImg
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlSelImg;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlId
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Long> urlId;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlCur
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlCur;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#catId
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Long> catId;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlSelPr
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlSelPr;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlTy
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlTy;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlUseYn
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Boolean> urlUseYn;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlSelNm
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlSelNm;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlUpdDt
	 **/
	public static volatile SingularAttribute<CrawlingUrl, LocalDateTime> urlUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlDelYn
	 **/
	public static volatile SingularAttribute<CrawlingUrl, Boolean> urlDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl
	 **/
	public static volatile EntityType<CrawlingUrl> class_;
	
	/**
	 * @see com.goodee.beedan.entity.CrawlingUrl#urlUrl
	 **/
	public static volatile SingularAttribute<CrawlingUrl, String> urlUrl;

}

