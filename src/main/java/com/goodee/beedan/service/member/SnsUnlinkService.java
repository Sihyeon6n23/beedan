package com.goodee.beedan.service.member;

import com.goodee.beedan.dto.member.sns.SnsDisconnectRequest;
import reactor.core.publisher.Mono;

public interface SnsUnlinkService {
    Mono<String> unlink(SnsDisconnectRequest snsDisconnectRequest);
}
