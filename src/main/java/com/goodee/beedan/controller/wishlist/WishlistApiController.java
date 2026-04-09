package com.goodee.beedan.controller.wishlist;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.service.stock.StockService;
import com.goodee.beedan.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    private final WishlistService wishlistService;
    private final StockService stockService;

    @PostMapping("/{stId}")
    public ResponseEntity<Void> add(@PathVariable Long stId,
                                    @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();
        wishlistService.addItem(stId, memId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stId}")
    public ResponseEntity<Void> delete(@PathVariable Long stId,
                                       @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();
        wishlistService.deleteItem(stId, memId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public Page<StockListDto> list(
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "popularity") String sort,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();
        return stockService.findWishedFiltered(brands, categories, keyword, sort, page, memId);
    }
}
