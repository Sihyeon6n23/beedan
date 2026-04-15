package com.goodee.beedan.controller.receiver;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.receiver.ReceiverDto;
import com.goodee.beedan.service.receiver.ReceiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receiver")
@RequiredArgsConstructor
@Slf4j
public class ReceiverApiController {
    private final ReceiverService receiverService;

    @GetMapping
    public ResponseEntity<List<ReceiverDto>> getReceiverList(@AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(receiverService.getReceiverList(userDetails.getMemberId()));
    }

    @PostMapping
    public ResponseEntity<List<ReceiverDto>> addReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody ReceiverDto receiverDto) {

        receiverDto.setMemId(userDetails.getMemberId());
        receiverService.addReceiverAddr(receiverDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(receiverService.getReceiverList(userDetails.getMemberId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiverDto> getReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId) {

        ReceiverDto receiverDto = receiverService.getReceiver(userDetails.getMemberId(), rcId);
        return ResponseEntity.ok(receiverDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<List<ReceiverDto>> updateReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId,
            @RequestBody ReceiverDto receiverDto) {
        receiverDto.setRcId(rcId);
        receiverDto.setMemId(userDetails.getMemberId());

        receiverService.updateReceiver(receiverDto);
        return ResponseEntity.ok(receiverService.getReceiverList(userDetails.getMemberId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<ReceiverDto>> deleteReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId) {
        receiverService.deleteReceiver(userDetails.getMemberId(), rcId);


        return ResponseEntity.ok(receiverService.getReceiverList(userDetails.getMemberId()));
    }
}
