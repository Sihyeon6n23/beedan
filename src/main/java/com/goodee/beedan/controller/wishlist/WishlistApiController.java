package com.goodee.beedan.controller.wishlist;

import com.goodee.beedan.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    private final WishlistService wishlistService;

    @PostMapping("/{stId}")
    public ResponseEntity<Void> add(@PathVariable Long stId
//                                  @AuthenticationPrincipal MemberUserDetails userDetails
    )                                           {
//        Long memId = UserDetails.getMemId();
        Long memId = 1L;
        wishlistService.addItem(stId, memId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stId}")
    public ResponseEntity<Void> delete(@PathVariable Long stId
//                                  @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
//        Long memId = UserDetails.getMemId();
        Long memId = 1L;
        wishlistService.deleteItem(stId, memId);
        return ResponseEntity.ok().build();
    }
}
