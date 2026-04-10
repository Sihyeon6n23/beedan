package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Category.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Category_ {

	public static final String CAT_ID = "catId";
	public static final String CAT_NM = "catNm";

	
	/**
	 * @see com.goodee.beedan.entity.Category#catId
	 **/
	public static volatile SingularAttribute<Category, Long> catId;
	
	/**
	 * @see com.goodee.beedan.entity.Category#catNm
	 **/
	public static volatile SingularAttribute<Category, String> catNm;
	
	/**
	 * @see com.goodee.beedan.entity.Category
	 **/
	public static volatile EntityType<Category> class_;

}

