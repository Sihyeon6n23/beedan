package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Wishlist.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Wishlist_ {

	public static final String WI_CRE_DT = "wiCreDt";
	public static final String WI_ID = "wiId";
	public static final String ST_ID = "stId";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Wishlist#wiCreDt
	 **/
	public static volatile SingularAttribute<Wishlist, LocalDateTime> wiCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Wishlist#wiId
	 **/
	public static volatile SingularAttribute<Wishlist, Long> wiId;
	
	/**
	 * @see com.goodee.beedan.entity.Wishlist#stId
	 **/
	public static volatile SingularAttribute<Wishlist, Long> stId;
	
	/**
	 * @see com.goodee.beedan.entity.Wishlist
	 **/
	public static volatile EntityType<Wishlist> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Wishlist#memId
	 **/
	public static volatile SingularAttribute<Wishlist, Long> memId;

}

