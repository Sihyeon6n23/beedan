package com.goodee.beedan.service.wishlist;

import com.goodee.beedan.entity.Wishlist;
import com.goodee.beedan.repository.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    // 관심 상품 추가
    public void addItem(Long stId, Long memId) {
        Wishlist wishlist = Wishlist.builder()
                .stId(stId)
                .memId(memId)
                .wiCreDt(LocalDateTime.now())
                .build();
        wishlistRepository.save(wishlist);
    }

    // 관심 상품 삭제
    public void deleteItem(Long stId, Long memId) {
        wishlistRepository.deleteByStIdAndMemId(stId, memId);
    }
}
