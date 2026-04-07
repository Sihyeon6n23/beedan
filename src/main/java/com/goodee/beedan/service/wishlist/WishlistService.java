package com.goodee.beedan.service.wishlist;

import com.goodee.beedan.entity.Wishlist;
import com.goodee.beedan.repository.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    // 관심 상품 추가
    public void addItem(Long stId, Long memId) {
        if(wishlistRepository.findByStIdAndMemId(stId, memId) != null) {
            throw new IllegalStateException("이미 관심 상품으로 등록된 상품입니다.");
        }
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

    // 관심 상품 id 추출
    public List<Wishlist> findAllWishedItems(Long memId) {
        return wishlistRepository.findAllByMemId(memId);
    }
}
