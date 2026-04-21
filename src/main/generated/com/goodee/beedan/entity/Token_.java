package com.goodee.beedan.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Token.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Token_ {

	public static final String TK_CRE_DT = "tkCreDt";
	public static final String TK_ID = "tkId";
	public static final String MEMBER = "member";
	public static final String TK_USE_YN = "tkUseYn";
	public static final String TK_TY = "tkTy";
	public static final String TK_VL = "tkVl";
	public static final String TK_EXP_DT = "tkExpDt";

	
	/**
	 * @see com.goodee.beedan.entity.Token#tkCreDt
	 **/
	public static volatile SingularAttribute<Token, LocalDateTime> tkCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Token#tkId
	 **/
	public static volatile SingularAttribute<Token, Long> tkId;
	
	/**
	 * @see com.goodee.beedan.entity.Token#member
	 **/
	public static volatile SingularAttribute<Token, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.Token#tkUseYn
	 **/
	public static volatile SingularAttribute<Token, Boolean> tkUseYn;
	
	/**
	 * @see com.goodee.beedan.entity.Token#tkTy
	 **/
	public static volatile SingularAttribute<Token, String> tkTy;
	
	/**
	 * @see com.goodee.beedan.entity.Token#tkVl
	 **/
	public static volatile SingularAttribute<Token, String> tkVl;
	
	/**
	 * @see com.goodee.beedan.entity.Token#tkExpDt
	 **/
	public static volatile SingularAttribute<Token, LocalDateTime> tkExpDt;
	
	/**
	 * @see com.goodee.beedan.entity.Token
	 **/
	public static volatile EntityType<Token> class_;

}

