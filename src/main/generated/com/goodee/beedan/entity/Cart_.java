package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Cart.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Cart_ {

	public static final String CA_ST_QN = "caStQn";
	public static final String CA_ID = "caId";
	public static final String STOCK = "stock";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Cart#caStQn
	 **/
	public static volatile SingularAttribute<Cart, Long> caStQn;
	
	/**
	 * @see com.goodee.beedan.entity.Cart#caId
	 **/
	public static volatile SingularAttribute<Cart, Long> caId;
	
	/**
	 * @see com.goodee.beedan.entity.Cart#stock
	 **/
	public static volatile SingularAttribute<Cart, Stock> stock;
	
	/**
	 * @see com.goodee.beedan.entity.Cart
	 **/
	public static volatile EntityType<Cart> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Cart#memId
	 **/
	public static volatile SingularAttribute<Cart, Long> memId;

}

