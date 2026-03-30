package com.goodee.beedan.controller.receiver;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.receiver.ReceiverDto;
import com.goodee.beedan.service.receiver.ReceiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<List<ReceiverDto>> getReceiverList(@AuthenticationPrincipal MemberUserDetails userDetails){
        List<ReceiverDto> receiverDtoList = receiverService.getReceiverList(userDetails.getMemberId());

        log.info(receiverDtoList.toString());

        return ResponseEntity.ok(receiverDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiverDto> getReciever(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId){
        ReceiverDto receiverDto = receiverService.getReceiver(userDetails.getMemberId(), rcId);

        return ResponseEntity.ok(receiverDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<List<ReceiverDto>> updateReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId,
            @RequestBody ReceiverDto receiverDto){
        receiverService.updateReceiver(receiverDto);

        return ResponseEntity.ok(receiverService.getReceiverList(userDetails.getMemberId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReceiverDto> deleteReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable("id") Long rcId){
        receiverService.deleteReceiver(rcId);

        return ResponseEntity.ok(receiverService.getReceiver(userDetails.getMemberId(), rcId));
    }

    @PostMapping
    public ResponseEntity<List<ReceiverDto>> addReceiver(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody ReceiverDto receiverDto){
        receiverDto.setMem_id(userDetails.getMemberId());
        receiverService.addReceiverAddr(receiverDto);

        return ResponseEntity.ok(receiverService.getReceiverList(userDetails.getMemberId()));
    }
}
