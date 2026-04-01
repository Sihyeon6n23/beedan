package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.buyer.BuyerGradePolicyResponse;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MypageController {
    private final MemberService memberService;
    private final BuyerService buyerService;
    private final BuyerGradePolicyService buyerGradePolicyService;
    private final BuyerRepository buyerRepository;

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
        if (userDetails == null) return "redirect:/login";

        String memberLgnId = userDetails.getUsername();
        Member member = memberService.getLoginId(memberLgnId);
        model.addAttribute("member", member); // 멤버 정보는 미리 담아둠

        Buyer buyer = null;
        String bizNo = member.getMemBizNo();

        // MypageController getDetail 내부
        String testNo = member.getMemBizNo().trim();
        log.info("조회 직전 번호 확인: [{}] (길이: {})", testNo, testNo.length());

// 서비스 호출 대신 레포지토리 직접 호출 테스트 (원인 파악용)
        Optional<Buyer> testBuyer = buyerRepository.findByMemBizNo(testNo);
        log.info("레포지토리 직접 조회 결과 존재여부: {}", testBuyer.isPresent());

        if (bizNo != null && !bizNo.isBlank()) {
            try {
                // [핵심] 조회 전 모든 하이픈과 공백을 제거하여 DB와 형식을 맞춤
                String cleanBizNo = bizNo.replace("-", "").trim();
                buyer = buyerService.findByBizNo(cleanBizNo);

                log.info("조회 성공: {} (ID: {})", buyer.getMemBizNo(), buyer.getById());
            } catch (Exception e) {
                log.warn("Buyer 정보를 찾을 수 없습니다. BizNo: {}", bizNo);
                // 예외가 발생하면 buyer는 null로 유지됩니다.
            }
        }

        // Buyer가 있을 때만 계산 로직 수행
        if (buyer != null) {
            List<BuyerGradePolicy> policies = buyerGradePolicyService.findAllActiveOrdered();
            BuyerGradePolicy nextPolicy = null;

            for (int i = 0; i < policies.size(); i++) {
                if (policies.get(i).getBgpGr().equals(buyer.getBgpGr())) {
                    if (i > 0) nextPolicy = policies.get(i - 1);
                    break;
                }
            }

            // 잔여 조건 계산
            int neededCnt = (nextPolicy != null) ? Math.max(0, nextPolicy.getBgpMinOrdCnt() - buyer.getByOrdCnt()) : 0;
            BigDecimal neededAmt = BigDecimal.ZERO;
            if (nextPolicy != null && nextPolicy.getBgpMinTtAm() != null && buyer.getByTtlAm() != null) {
                neededAmt = nextPolicy.getBgpMinTtAm().subtract(buyer.getByTtlAm());
                if (neededAmt.compareTo(BigDecimal.ZERO) < 0) neededAmt = BigDecimal.ZERO;
            }

            model.addAttribute("nextPolicy", nextPolicy);
            model.addAttribute("neededCnt", neededCnt);
            model.addAttribute("neededAmt", neededAmt);
            model.addAttribute("policyList", policies.stream().map(BuyerGradePolicyResponse::from).toList());
        }

        // 최종적으로 찾은(혹은 null인) buyer를 전달
        model.addAttribute("buyer", buyer);

        return "member/mypage/mypage-detail";
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
