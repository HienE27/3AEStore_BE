package com.nguyenviethien.exercise201.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenviethien.exercise201.service.CardItemService;

@RestController
@RequestMapping("/cardItems")
public class CardItemController {
    @Autowired
    private CardItemService cardItemService;

@DeleteMapping("/{cardItemId}")
    public ResponseEntity<?> deleteCardItem(
        @PathVariable UUID cardItemId,
        @RequestParam UUID customerId // Truyền kèm ID khách hàng để xác thực quyền xóa
    ) {
        cardItemService.deleteById(cardItemId, customerId);
        return ResponseEntity.ok().build();
    }


    
}

