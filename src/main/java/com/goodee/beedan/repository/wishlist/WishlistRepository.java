package com.goodee.beedan.repository.wishlist;

import com.goodee.beedan.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Wishlist findByStIdAndMemId(Long stId, Long memId);

    void deleteByStIdAndMemId(Long stId, Long memId);
    List<Wishlist> findAllByMemId(Long memId);
}
