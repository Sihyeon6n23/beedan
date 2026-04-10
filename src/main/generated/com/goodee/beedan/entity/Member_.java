package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.MemberAuthority;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Member.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Member_ {

	public static final String MEM_CEO_NM = "memCeoNm";
	public static final String MEM_LOC_DT = "memLocDt";
	public static final String MEM_CEO_PHN = "memCeoPhn";
	public static final String MEM_LGN_TR = "memLgnTr";
	public static final String MEM_POS_CD = "memPosCd";
	public static final String MEM_UPD_DT = "memUpdDt";
	public static final String MEM_BIZ_NO = "memBizNo";
	public static final String MEM_UPD_ID = "memUpdId";
	public static final String MEM_NM = "memNm";
	public static final String MEM_STT = "memStt";
	public static final String MEM_CMP_TEL = "memCmpTel";
	public static final String MEM_BIZ_CRE_DT = "memBizCreDt";
	public static final String MEM_AUT = "memAut";
	public static final String MEM_CRE_DT = "memCreDt";
	public static final String MEM_LGN_ID = "memLgnId";
	public static final String MEM_MB_PHN = "memMbPhn";
	public static final String MEM_EML = "memEml";
	public static final String MEM_BIZ_TTL = "memBizTtl";
	public static final String MEM_LGN_PW = "memLgnPw";
	public static final String MEM_BIZ_ADR = "memBizAdr";
	public static final String MEM_CI = "memCi";
	public static final String MEM_UPD_PW_DT = "memUpdPwDt";
	public static final String MEM_BIZ_DT_ADR = "memBizDtAdr";
	public static final String MEM_ID = "memId";

	
	/**
	 * @see com.goodee.beedan.entity.Member#memCeoNm
	 **/
	public static volatile SingularAttribute<Member, String> memCeoNm;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memLocDt
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> memLocDt;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memCeoPhn
	 **/
	public static volatile SingularAttribute<Member, String> memCeoPhn;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memLgnTr
	 **/
	public static volatile SingularAttribute<Member, Long> memLgnTr;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memPosCd
	 **/
	public static volatile SingularAttribute<Member, String> memPosCd;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memUpdDt
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> memUpdDt;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memBizNo
	 **/
	public static volatile SingularAttribute<Member, String> memBizNo;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memUpdId
	 **/
	public static volatile SingularAttribute<Member, Long> memUpdId;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memNm
	 **/
	public static volatile SingularAttribute<Member, String> memNm;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memStt
	 **/
	public static volatile SingularAttribute<Member, String> memStt;
	
	/**
	 * @see com.goodee.beedan.entity.Member
	 **/
	public static volatile EntityType<Member> class_;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memCmpTel
	 **/
	public static volatile SingularAttribute<Member, String> memCmpTel;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memBizCreDt
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> memBizCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memAut
	 **/
	public static volatile SingularAttribute<Member, MemberAuthority> memAut;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memCreDt
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> memCreDt;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memLgnId
	 **/
	public static volatile SingularAttribute<Member, String> memLgnId;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memMbPhn
	 **/
	public static volatile SingularAttribute<Member, String> memMbPhn;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memEml
	 **/
	public static volatile SingularAttribute<Member, String> memEml;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memBizTtl
	 **/
	public static volatile SingularAttribute<Member, String> memBizTtl;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memLgnPw
	 **/
	public static volatile SingularAttribute<Member, String> memLgnPw;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memBizAdr
	 **/
	public static volatile SingularAttribute<Member, String> memBizAdr;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memCi
	 **/
	public static volatile SingularAttribute<Member, String> memCi;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memUpdPwDt
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> memUpdPwDt;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memBizDtAdr
	 **/
	public static volatile SingularAttribute<Member, String> memBizDtAdr;
	
	/**
	 * @see com.goodee.beedan.entity.Member#memId
	 **/
	public static volatile SingularAttribute<Member, Long> memId;

}

