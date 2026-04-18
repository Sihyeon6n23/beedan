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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.net.http.HttpRequest;
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
    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;

    @GetMapping("/signup")
    public String getSignUp(Model model) {
        model.addAttribute("memberForm", new MemberFormDto());
        model.addAttribute("portoneStoreId", storeId);
        model.addAttribute("portoneChannelKey", channelKey);
        return "/member/auth/signup";
    }

    @PostMapping("/signup")
    public String postSignUp(
            @Valid @ModelAttribute("memberForm") MemberFormDto memberForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.getFieldError() != null) {
                model.addAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            }
            return "/member/auth/signup";
        }

        if (!memberForm.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "passwordIncorret", "비밀번호가 일치하지 않습니다.");
            log.info("비밀번호가 일치하지 않습니다.");
            return "/member/auth/signup";
        }

        if (!fileService.validateFileCount(memberForm.getNewFiles(), 0,0,1L)) {
            bindingResult.rejectValue("newFiles", "fileInvalidCount", "파일 업로드 개수를 초과했습니다.");
            log.info("파일 업로드 개수를 초과했습니다.");
            return "/member/auth/signup";
        }

        if (!Boolean.TRUE.equals(memberForm.getIdCheckedInput())) {
            bindingResult.rejectValue("userLoginId", "idDuplicateCheck", "아이디 중복확인 버튼을 눌러주세요.");
            return "/member/auth/signup";
        }

        if (memberService.isDuplicatedLoginId(memberForm.getUserLoginId())) {
            bindingResult.rejectValue("userLoginId", "idAlreadyTaken", "해당 아이디로 먼저 가입한 사용자가 있습니다. 다시 시도해주세요.");
            return "/member/auth/signup";
        }

        if (!Boolean.TRUE.equals(memberForm.getEmailCheckedInput())) {
            bindingResult.rejectValue("email", "emailCheckRequired", "이메일 중복확인 버튼을 눌러주세요.");
            return "/member/auth/signup";
        }

        if (memberService.checkEmailDuplicate(memberForm.getEmail())) {
            bindingResult.rejectValue("email", "emailAlreadyTaken", "해당 이메일로 먼저 가입한 사용자가 있습니다. 다시 시도해주세요.");
            return "/member/auth/signup";
        }

        List<MultipartFile> files = memberForm.getNewFiles();
        MultipartFile file = files.getFirst();
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
        PhoneVerificationDto phoneVerificationDto = portOneService.MonoToPhoneVerificationDto(verifyMono).block();

        // [수정 1]: String 조작 전 null 참조 예외(NPE) 완벽 방어
        String estDate = memberForm.getEstablishmentDate();
        String formattedStartDt = (estDate != null) ? estDate.replace("-", "") : "";

        // 사업자등록번호 재인증(백엔드검증)
        BizDto bizDto = BizDto.builder()
                .bNo(memberForm.getBusinessRegNum())
                .bNm(memberForm.getCompanyName())
                .pNm(memberForm.getCeoName())
                .startDt(formattedStartDt) // null-safe 처리된 변수 주입
                .build();

        Mono<Map<String, Object>> bizValidateMono = bizValidateService.validate(bizDto);
        BizDto validateBizDto = bizValidateService.monoToBizDto(bizValidateMono);

        // [수정 5]: 검증 실패 시 상수를 기준으로 비교하고, return 문을 추가하여 흐름 차단
        if ("02".equals(validateBizDto.getValid())) {
            log.info("사업자 정보 입력값이 올바르지 않습니다. Valid: {}", validateBizDto.getValid());
            bindingResult.rejectValue("businessRegNum", "invalidBiz", "사업자 정보가 올바르지 않습니다. 다시 확인해주세요.");
            return "/member/auth/signup"; // 예외 발생 후 원래 폼으로 튕겨냄
        }

        try {
            memberService.insertMember(memberForm, phoneVerificationDto, bizDto);
        } catch (Exception e) {
            bindingResult.reject("signup.fail", e.getMessage());
            return "/member/auth/signup";
        }

        model.addAttribute("signupSuccess", true);
        return "/member/auth/signup";
    }

    @GetMapping("/signin")
    public String getSignIn(HttpServletRequest request,
                            Model model) {
        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("errorMessage") != null) {
            model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
            session.removeAttribute("errorMessage");
        }
        return "/member/auth/signin";
    }

    @GetMapping("/find")
    public String getFind() {
        return "/member/auth/find";
    }

    @GetMapping("/passwd/change")
    public String getPasswdChange(@RequestParam("token_id") String tokenId,
                                  Model model) {
        memberService.findMemberByToken(tokenId);

        model.addAttribute("tokenId", tokenId);
        model.addAttribute("passwordResetDto", new PasswordResetDto());
        return "member/auth/password-reset";
    }

    @PostMapping("/passwd/change")
    public String postPasswdChange(@RequestParam("token_id") String tokenId,
                                   @Valid @ModelAttribute PasswordResetDto passwordResetDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes rttr) {
        // 1. @Pattern 등에 의한 정규식 검증 결과 확인
        if (bindingResult.hasErrors()) {
            model.addAttribute("tokenId", tokenId);
            bindingResult.rejectValue("password", "error.password", "최소 9자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.");
            return "member/auth/password-reset";
        }

        // 2. 어노테이션으로 불가능한 로직(일치 여부)은 수동 확인
        if (!passwordResetDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("tokenId", tokenId);
            return "member/auth/password-reset";
        }

        try {
            // 2. 서비스 로직 수행 (토큰 확인 및 비밀번호 업데이트)
            memberService.resetPassword(tokenId, passwordResetDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다.");
            model.addAttribute("tokenId", tokenId);
            return "member/auth/password-reset";
        }

        rttr.addFlashAttribute("successMessage", "비밀번호 변경을 완료했습니다.");
        return "redirect:/auth/signin";
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

        // [수정 4]: Principal null 체크를 추가하여 비로그인 사용자의 접근 원천 차단
        if (principal == null) {
            log.warn("비로그인 사용자가 SNS 연동 콜백에 접근했습니다.");
            return "redirect:/auth/signin";
        }

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