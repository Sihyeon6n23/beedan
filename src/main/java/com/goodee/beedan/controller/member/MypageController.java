package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.buyer.BuyerGradePolicyResponse;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final MemberService memberService;
    private final BuyerService buyerService;
    private final BuyerGradePolicyService buyerGradePolicyService;
    @GetMapping("")
    public String getMainRedirect() {
        return "redirect:/mypage/main";
    }

    @GetMapping("/main")
    public String getMain() {
        return "/member/mypage/mypage-main";
    }

    @GetMapping("/detail")
    public String getDetail(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 회원 및 구매자(고객사) 정보 조회
        String memberLgnId = userDetails.getUsername();
        Member member = memberService.getLoginId(memberLgnId);
        Buyer buyer = buyerService.findByBizNo(member.getMemBizNo());

        // 2. 내림차순(VIP -> PREMIUM -> STANDARD) 정렬된 정책 리스트 조회
        List<BuyerGradePolicy> policies = buyerGradePolicyService.findAllActiveOrdered();
        BuyerGradePolicy nextPolicy = null;

        // 3. 다음 등급 찾기 로직 (내림차순이므로 현재 등급의 인덱스 - 1 이 다음 등급)
        for (int i = 0; i < policies.size(); i++) {
            if (policies.get(i).getBgpGr().equals(buyer.getBgpGr())) {
                if (i > 0) {
                    nextPolicy = policies.get(i - 1);
                }
                break;
            }
        }

        // 4. 다음 등급까지 남은 조건 계산
        int neededCnt = 0;
        BigDecimal neededAmt = BigDecimal.ZERO;

        if (nextPolicy != null) {
            // 남은 구매 횟수 (음수가 나오지 않도록 Math.max 처리)
            neededCnt = Math.max(0, nextPolicy.getBgpMinOrdCnt() - buyer.getByOrdCnt());

            // 남은 구매 금액 계산
            if (nextPolicy.getBgpMinTtAm() != null && buyer.getByTtlAm() != null) {
                neededAmt = nextPolicy.getBgpMinTtAm().subtract(buyer.getByTtlAm());
                if (neededAmt.compareTo(BigDecimal.ZERO) < 0) {
                    neededAmt = BigDecimal.ZERO;
                }
            }
        }

        // 5. 전체 등급 혜택 리스트 DTO 변환 (수정하신 BuyerGradePolicyResponse 적용)
        List<BuyerGradePolicyResponse> policyDtos = policies.stream()
                .map(BuyerGradePolicyResponse::from)
                .collect(Collectors.toList());

        // 6. View(HTML)로 데이터 전달
        model.addAttribute("member", member); // 기존 detail 로직 유지
        model.addAttribute("Buyer", buyer);   // 파란 박스 및 모달에서 사용할 구매자 정보
        model.addAttribute("nextPolicy", nextPolicy);
        model.addAttribute("neededCnt", neededCnt);
        model.addAttribute("neededAmt", neededAmt);
        model.addAttribute("policyList", policyDtos);

        return "/member/mypage/mypage-detail";
    }

    @GetMapping("/changepw")
    public String getChangePw() {
        return "/member/mypage/mypage-changepw";
    }

    @GetMapping("/changebiz")
    public String getChangeBiz() {
        return "/member/mypage/mypage-changebiz";
    }
}
