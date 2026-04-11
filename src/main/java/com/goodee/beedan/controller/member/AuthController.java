package com.goodee.beedan.controller.member;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.common.constant.SnsType;
import com.goodee.beedan.dto.member.*;
import com.goodee.beedan.dto.member.sns.SnsIntegrateRequest;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.auth.TokenService;
import com.goodee.beedan.service.auth.biz.BizValidateService;
import com.goodee.beedan.service.auth.phone.PortOneService;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.member.SnsIntegrateService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final FileService fileService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

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

        MultipartFile file = memberForm.getNewFiles();
        if (file != null && !file.isEmpty()) {

            String originalFileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            String ext = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
            }

            List<String> allowedImages = Arrays.asList("jpg", "jpeg", "png", "pdf");

            if (ext.isEmpty() || !allowedImages.contains(ext) || contentType == null) {
                bindingResult.rejectValue("newFiles", "fileInvalid", "파일 확장자를 확인해주세요.");
                return "/member/auth/signup";
            }

            String mimeType = fileService.getMimeType(file);
            if (!fileService.isMimeExtensionMatched(mimeType, ext)) {
                bindingResult.rejectValue("newFiles", "fileInvalid", "파일의 데이터 규격이 확장자 정보와 다릅니다. 원본 파일을 확인해 주세요.");
                return "/member/auth/signup";
            }
        }
        // 휴대폰 번호 API 검증(백엔드검증)
        Mono<Map<String, Object>> verifyMono = portOneService.verify(memberForm.getImpUid());
        PhoneVerificationDto phoneVerificationDto = portOneService.MonoToPhoneVerificationDto(verifyMono);

        // 사업자등록번호 재인증(백엔드검증)
        BizDto bizDto = BizDto.builder()
                .bNo(memberForm.getBusinessRegNum())
                .bNm(memberForm.getCompanyName())
                .pNm(memberForm.getCeoName())
                .startDt(memberForm.getEstablishmentDate().replace("-", ""))
                .build();

        Mono<Map<String, Object>> bizValidateMono = bizValidateService.validate(bizDto);
        BizDto validateBizDto = bizValidateService.monoToBizDto(bizValidateMono);

        if (validateBizDto.getValid().equals("02")) {
            log.info("사업자 정보 입력값이 올바르지 않습니다. Valid: {}", validateBizDto.getValid());
            // 예외처리
        }

        try {
            memberService.insertMember(memberForm, phoneVerificationDto, bizDto);
        } catch (Exception e) {
            bindingResult.reject("signup.fail", e.getMessage());
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

    @GetMapping("/passwd/change")
    public String getPasswdChange(@RequestParam("token_id") String tokenId,
                                  Model model) {
        // 예외처리 먼저 실행
        memberService.findMemberByToken(tokenId);

        model.addAttribute("tokenId", tokenId);
        model.addAttribute("passwordResetDto", new PasswordResetDto());
        return "member/auth/password-reset";
    }

    @PostMapping("/passwd/change")
    public String postPasswdChange(@RequestParam("token_id") String tokenId,
                                   @Valid @ModelAttribute PasswordResetDto passwordResetDto,
                                   BindingResult bindingResult,
                                   Model model) {

        // 1. 기본 필드 검증 (Size, NotBlank 등) 및 비밀번호 일치 확인
        if (bindingResult.hasErrors() || !passwordResetDto.isPasswordMatching()) {

            // 비밀번호 불일치 시 커스텀 에러 추가
            if (!passwordResetDto.isPasswordMatching()) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "비밀번호가 일치하지 않습니다.");
            }

            // 중요: 원래 페이지로 돌아갈 때 필요한 데이터를 다시 모델에 담음
            model.addAttribute("tokenId", tokenId);
            // passwordResetDto는 @ModelAttribute에 의해 자동으로 모델에 유지됩니다.

            return "member/auth/password-reset"; // 원래 HTML 파일 경로 (forward)
        }

        try {
            // 2. 서비스 로직 수행 (토큰 확인 및 비밀번호 업데이트)
            memberService.resetPassword(tokenId, passwordResetDto);
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            model.addAttribute("tokenId", tokenId);
            return "member/auth/password-reset";
        }

        // 3. 성공 시 리다이렉트 (PRG 패턴)
        return "redirect:/auth/login?resetSuccess=true";
    }


    @GetMapping("/kakao/link")
    public String initiateKakaoLink(HttpSession session) {
        String state = UUID.randomUUID().toString(); // 1회용 암호 생성
        session.setAttribute("kakao_state", state); // 세션에 저장

        return "redirect:https://kauth.kakao.com/oauth/authorize?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&state=" + state; // URL에 포함
    }

    @GetMapping("/kakao/callback")
    public String getSnsIntegrateCallback(@RequestParam("code")String code,
                                          @RequestParam("state") String state,
                                          HttpSession session,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        // 1. sns 서비스 호출 -> id값으로 member 조회 후 인증정보 조회(방어) -> 있으면 return
        Member member = memberService.getMemberByUsername(principal.getName());
        if (snsIntegrateService.isSnsIntegrate(member)) {
            log.info("이미 연동중인 계정입니다.");
            return "redirect:/mypage/sns";
        }

        String savedState = (String) session.getAttribute("kakao_state");
        if (savedState == null || !savedState.equals(state)) {
            log.error("CSRF 공격 의심: state 불일치");
            return "redirect:/mypage/sns?error=invalid_state";
        }
        session.removeAttribute("kakao_state"); // 검증 후 즉시 파기

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
