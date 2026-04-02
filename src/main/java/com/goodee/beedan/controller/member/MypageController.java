package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.buyer.BuyerGradePolicyResponse;
import com.goodee.beedan.dto.member.PasswordChangeDto;
import com.goodee.beedan.dto.member.PhoneVerificationDto;
import com.goodee.beedan.dto.member.mypage.UpdateMemberRequest;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import com.goodee.beedan.service.auth.phone.PortOneService;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.member.MypageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MypageController {
    private final MemberService memberService;
    private final BuyerService buyerService;
    private final BuyerGradePolicyService buyerGradePolicyService;
    private final BuyerRepository buyerRepository;
    private final MypageService mypageService;
    private final PortOneService portOneService;

    @Value("${kakao.map.appkey}")
    private String kakaoAppKey;

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
        Member member = memberService.getMemberByUsername(memberLgnId);
        model.addAttribute("member", member); // 멤버 정보는 미리 담아둠

        Buyer buyer = null;
        String bizNo = member.getMemBizNo();

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
        model.addAttribute("kakaoAppKey", kakaoAppKey);
        model.addAttribute("buyer", buyer);

        return "member/mypage/mypage-detail";
    }

    @GetMapping("/changepw")
    public String getChangePw(
            Model model) {
        model.addAttribute("passwordForm", new PasswordChangeDto());
        return "/member/mypage/mypage-changepw";
    }

    @PostMapping("/changepw")
    public String postChangePw(@Valid @ModelAttribute("passwordForm") PasswordChangeDto request,
                               BindingResult bindingResult,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/mypage/mypage-changepw";
        }

        if (!mypageService.matchPassword(principal.getName(), request.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "curPasswordIncorrect", "현재 비밀번호가 일치하지 않습니다.");
            return "member/mypage/mypage-changepw";
        }

        if (!request.isPasswordConfirm()) {
                bindingResult.rejectValue("confirmPassword", "conPasswordIncorrect", "확인 비밀번호가 일치하지 않습니다.");
            return "member/mypage/mypage-changepw";
        }

        try {
            mypageService.changPassword(principal.getName(), request);
            redirectAttributes.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", "비밀번호 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage/detail";
    }

    @GetMapping("/modify")
    public String modifyProfileForm(Model model, Principal principal) {
        Member member = memberService.getMemberByUsername(principal.getName());

        model.addAttribute("member", member);
        model.addAttribute("kakaoAppKey", kakaoAppKey);

        return "member/mypage/mypage-modify";
    }

    @PostMapping("/modify")
    public String modifyProfileUpdate(@Valid @ModelAttribute UpdateMemberRequest request,
                                      BindingResult bindingResult, // ❗️반드시 @ModelAttribute 바로 다음에 와야 합니다.
                                      Principal principal,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        // 1. DTO 유효성 검사 (@NotBlank, @Email 등) 실패 시 처리
        if (bindingResult.hasErrors()) {
            // 에러가 발생하면 수정 폼 화면을 다시 렌더링합니다.
            // Redirect하지 않고 뷰를 바로 리턴해야 사용자가 입력하던 값과 에러 메시지가 유지됩니다.
            return "member/mypage/mypage-modify";
        }

        try {
            // 2. 본인인증 impUid가 넘어왔는지 확인 (휴대폰 번호를 변경하여 인증을 진행한 경우)
            if (request.getImpUid() != null && !request.getImpUid().isBlank()) {

                // 휴대폰 번호 API 검증 (백엔드 검증)
                Mono<Map<String, Object>> verifyMono = portOneService.verify(request.getImpUid());
                PhoneVerificationDto phoneVerificationDto = portOneService.MonoToPhoneVerificationDto(verifyMono);


                // [수정된 부분] 1. 뷰로 돌아갈 때마다 쓸 수 있게 member 객체를 미리 조회해 둡니다.
                Member member = memberService.getMemberByUsername(principal.getName());

                // 2. DTO 유효성 검사 실패 시
                if (bindingResult.hasErrors()) {
                    model.addAttribute("member", member); // 뷰에서 쓸 수 있게 담아줌
                    return "member/mypage/mypage-modify";
                }

                // [핵심] 포트원에서 받은 확실한 데이터로 교체 (위조 방지)
                request.setName(phoneVerificationDto.getName());
                request.setPhone(phoneVerificationDto.getPhoneNumber());
                request.setCi(phoneVerificationDto.getCi());

                // 🔍 객체 내부 데이터 뜯어보기 (콘솔 확인)
                log.info("🔥 [포트원 본인인증 찐 데이터]: {}", phoneVerificationDto);
            }

            // 3. 서비스 계층에서 업데이트 로직 실행
            mypageService.updateMember(principal.getName(), request);

            // 4. 성공 시 마이페이지나 수정 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 변경되었습니다.");
            return "redirect:/mypage/detail";

        } catch (Exception e) {
            // 기타 서버 에러 발생 시
            redirectAttributes.addFlashAttribute("error", "정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/modify";
        }
    }

    @PostMapping("/withdrawal")
    public String postWithdrawal(Principal principal) {
        mypageService.withdraw(principal.getName());
        return "redirect:/auth/signin";
    }
}
