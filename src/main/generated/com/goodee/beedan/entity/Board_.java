package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Board.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Board_ {

	public static final String BRD_CON = "brdCon";
	public static final String BRD_TY = "brdTy";
	public static final String BRD_UPD_DT = "brdUpdDt";
	public static final String BRD_INQ_STT = "brdInqStt";
	public static final String BRD_DEL_YN = "brdDelYn";
	public static final String BRD_TTL = "brdTtl";
	public static final String BRD_PRN_ID = "brdPrnId";
	public static final String BRD_UPD_MEM_ID = "brdUpdMemId";
	public static final String BRD_CRE_DT = "brdCreDt";
	public static final String BRD_CAN_RE = "brdCanRe";
	public static final String BRD_VST_CNT = "brdVstCnt";
	public static final String BRD_ID = "brdId";
	public static final String MEMBER = "member";
	public static final String BRD_FIX_YN = "brdFixYn";

	
	/**
	 * @see com.goodee.beedan.entity.Board#brdCon
	 **/
	public static volatile SingularAttribute<Board, String> brdCon;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdTy
	 **/
	public static volatile SingularAttribute<Board, BoardType> brdTy;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdUpdDt
	 **/
	public static volatile SingularAttribute<Board, LocalDateTime> brdUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdInqStt
	 **/
	public static volatile SingularAttribute<Board, InquiryStatus> brdInqStt;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdDelYn
	 **/
	public static volatile SingularAttribute<Board, Boolean> brdDelYn;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdTtl
	 **/
	public static volatile SingularAttribute<Board, String> brdTtl;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdPrnId
	 **/
	public static volatile SingularAttribute<Board, Long> brdPrnId;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdUpdMemId
	 **/
	public static volatile SingularAttribute<Board, Long> brdUpdMemId;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdCreDt
	 **/
	public static volatile SingularAttribute<Board, LocalDateTime> brdCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdCanRe
	 **/
	public static volatile SingularAttribute<Board, String> brdCanRe;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdVstCnt
	 **/
	public static volatile SingularAttribute<Board, Long> brdVstCnt;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdId
	 **/
	public static volatile SingularAttribute<Board, Long> brdId;
	
	/**
	 * @see com.goodee.beedan.entity.Board#member
	 **/
	public static volatile SingularAttribute<Board, Member> member;
	
	/**
	 * @see com.goodee.beedan.entity.Board#brdFixYn
	 **/
	public static volatile SingularAttribute<Board, Boolean> brdFixYn;
	
	/**
	 * @see com.goodee.beedan.entity.Board
	 **/
	public static volatile EntityType<Board> class_;

}

