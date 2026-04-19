package com.goodee.beedan.devUtils;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.goodee.beedan.config.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.NoSuchElementException;


@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElement(NoSuchElementException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            Model model) {
        model.addAttribute("message", "파일 업로드 용량이 초과되었습니다.");
        return "error/common";
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(
            NoResourceFoundException e,
            Model model) {
        model.addAttribute("message", "파일 경로가 올바르지 않습니다.");
        return "error/common";
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(
            EntityNotFoundException e,
            Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("message", "요청을 처리하는 중 오류가 발생했습니다.");
        return "error/common";
    }

}