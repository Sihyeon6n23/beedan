package com.goodee.beedan.controller.member;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.common.constant.SnsType;
import com.goodee.beedan.dto.member.MemberFormDto;
import com.goodee.beedan.dto.member.PhoneVerificationDto;
import com.goodee.beedan.dto.member.BizDto;
import com.goodee.beedan.dto.member.sns.SnsIntegrateRequest;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.auth.biz.BizValidateService;
import com.goodee.beedan.service.auth.phone.PortOneService;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.member.SnsIntegrateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final BizValidateService bizValidateService;
    private final PortOneService portOneService;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final SnsIntegrateService snsIntegrateService;

    @GetMapping("/signup")
    public String getSignUp(Model model) {
        model.addAttribute("memberForm", new MemberFormDto());
        return "/member/auth/signup";
    }

    @PostMapping("/signup")
    public String postSignUp(
            @Valid @ModelAttribute("memberForm") MemberFormDto memberForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        // 검증 필요
        if (bindingResult.hasErrors()) {
            // 에러 메시지 중 첫 번째를 가져와서 전달 (예시)
            String defaultMessage = bindingResult.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", defaultMessage);
            return "/member/auth/signup";
        }

        if (!memberForm.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "passwordIncorret", "비밀번호가 일치하지 않습니다.");
            return "/member/auth/signup";
        }

        if (!memberForm.getIdCheckedInput()) {
            bindingResult.rejectValue("duplicateCheckLoginId", "idDuplicateCheck", "아이디 중복확인 버튼을 눌러주세요.");
            return "/member/auth/signup";
        }
        // 휴대폰 번호 API 검증(백엔드검증)
        Mono<Map<String, Object>> verifyMono = portOneService.verify(memberForm.getImpUid());
        PhoneVerificationDto phoneVerificationDto = portOneService.MonoToPhoneVerificationDto(verifyMono);

        // 사업자등록번호 재인증(백엔드검증)
        BizDto bizDto = BizDto.builder()
                .bNo(memberForm.getBusinessRegNum())
                .bNm(memberForm.getCompanyName())
                .pNm(memberForm.getCeoName())
                .startDt(memberForm.getEstablishmentDate())
                .build();

        Mono<Map<String, Object>> bizValidateMono = bizValidateService.validate(bizDto);
        BizDto validateBizDto = bizValidateService.monoToBizDto(bizValidateMono);

        if (validateBizDto.getValid().equals("02")) {
            log.info("사업자 정보 입력값이 올바르지 않습니다. Valid: {}", validateBizDto.getValid());
            // 예외처리
        }

        // 아이디, 비밀번호, 이메일, 우편번호, 주소, 상세주소 입력 - 완료
        // 이름, 휴대폰번호, CI값 입력
        // 재인증 후 사업자등록번호, 상호명, 대표자명, 설립연월일 입력
        Member member = Member.builder()
                .memLgnId(memberForm.getUserLoginId())
                .memLgnPw(passwordEncoder.encode(memberForm.getPassword()))
                .memEml(memberForm.getEmail())
                .memPosCd(memberForm.getPostCode())
                .memBizAdr(memberForm.getCompanyAddress())
                .memBizDtAdr(memberForm.getCompanyAddressDetail())
                .memStt(MemberStatus.PENDING.toString()) // 가입요청상태로 회원가입 요청
                .memAut(MemberAuthority.USER) // 회원가입 요청시 USER로 요청
                .memLgnTr(0L)
                .memMbPhn(phoneVerificationDto.getPhoneNumber())
                .memCi(phoneVerificationDto.getCi())
                .memNm(phoneVerificationDto.getName())
                .memBizNo(validateBizDto.getBNo())
                .memBizTtl(validateBizDto.getBNm())
                .memCeoNm(validateBizDto.getPNm())
                .memBizCreDt(LocalDate.parse(
                        validateBizDto.getStartDt(),
                        DateTimeFormatter.ofPattern("yyyyMMdd")
                ).atStartOfDay())
                .build();

        try {
            memberService.insertMember(member);
        } catch (Exception e) {
            return "/member/auth/signup";
        }

        return "redirect:/auth/signin";
    }

    @GetMapping("/signin")
    public String getSignIn() {
        return "/member/auth/signin";
    }

    @PostMapping("/signin")
    public String postSignIn() {
        return "redirect:/mypage/detail";
    }

    @PostMapping("/signout")
    public String postSignOut() {
        return "redirect:/login";
    }

    @GetMapping("/find")
    public String getFind() {
        return "/member/auth/find";
    }

    @GetMapping("/kakao/callback")
    public String getSnsIntegrateCallback(@RequestParam("code")String code,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        // 1. sns 서비스 호출 -> id값으로 member 조회 후 인증정보 조회(방어) -> 있으면 return
        Member member = memberService.getMemberByUsername(principal.getName());
        if (snsIntegrateService.isSnsIntegrate(member)) {
            log.info("이미 연동중인 계정입니다.");
            return "redirect:/mypage/sns";
        }

        SnsIntegrateRequest snsIntegrateRequest = SnsIntegrateRequest.builder()
                .snsTp(SnsType.KAKAO)
                .member(member)
                .build();

        snsIntegrateService.setSnsIntegrateRequest(snsIntegrateRequest, code);

        // 2. 없으면 반환된 코드로 카톡 API 호출해서 토큰값 확인

        // 3. 확인된 토큰을 DB에 저장하기 위해서 전달.
        // 4. 저장 후 반환값으로 redirect / sns 페이지 넘어갔을 때 1회용 메시지 전달을 위한 리다이렉트 flash에 저장 후 출력
        // 5. 최종 redirect 후 연동여부 표시.
        redirectAttributes.addFlashAttribute("messages", "카카오톡 연동을 성공했습니다.");

        return "redirect:/mypage/sns";
    }
}
