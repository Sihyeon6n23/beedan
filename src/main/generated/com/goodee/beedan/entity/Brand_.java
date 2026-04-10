package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Brand.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Brand_ {

	public static final String BR_NM = "brNm";
	public static final String BR_ID = "brId";

	
	/**
	 * @see com.goodee.beedan.entity.Brand#brNm
	 **/
	public static volatile SingularAttribute<Brand, String> brNm;
	
	/**
	 * @see com.goodee.beedan.entity.Brand#brId
	 **/
	public static volatile SingularAttribute<Brand, Long> brId;
	
	/**
	 * @see com.goodee.beedan.entity.Brand
	 **/
	public static volatile EntityType<Brand> class_;

}

