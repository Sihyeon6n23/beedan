package com.goodee.beedan.controller.cart;

import com.goodee.beedan.dto.cart.CartUpdateDto;
import com.goodee.beedan.repository.cart.CartRepository;
import com.goodee.beedan.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart/api")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @DeleteMapping("/{caId}")
    public ResponseEntity<Void> delete(@PathVariable("caId") Long caId) {
        cartService.deleteItem(caId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> updateCart(@RequestBody List<CartUpdateDto> updates){
        cartService.updateCart(updates);
        return ResponseEntity.ok().build();
    }
}
