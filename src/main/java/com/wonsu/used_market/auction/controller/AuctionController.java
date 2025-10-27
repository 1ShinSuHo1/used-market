package com.wonsu.used_market.auction.controller;

import com.wonsu.used_market.auction.dto.AuctionResponseDto;
import com.wonsu.used_market.auction.dto.CreateAuctionRequestDto;
import com.wonsu.used_market.auction.dto.PlaceFreeBidRequestDto;
import com.wonsu.used_market.auction.service.AuctionService;
import com.wonsu.used_market.common.auth.CurrentUser;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class    AuctionController {

    private final AuctionService auctionService;

    //경매 생성
    @PostMapping
    public ResponseEntity<AuctionResponseDto> createAuction(
            @RequestBody CreateAuctionRequestDto requestDto,
            @CurrentUser User currentUser
    ){
        AuctionResponseDto responseDto =  auctionService.createAuction(requestDto, currentUser);
        return ResponseEntity.ok(responseDto);
    }

    //자유 입찰
    @PostMapping("/{auctionId}/bid/free")
    public ResponseEntity<AuctionResponseDto> freeBid(
            @PathVariable Long auctionId,
            @RequestBody PlaceFreeBidRequestDto requestDto,
            @CurrentUser User bidder
    ){
        requestDto.setAuctionId(auctionId);
        AuctionResponseDto response = auctionService.placeFreeBid(requestDto, bidder);
        return ResponseEntity.ok(response);
    }

    //빠른 입찰
    @PostMapping("/{auctionId}/bid/quick")
    public ResponseEntity<AuctionResponseDto> quickBid(
            @PathVariable Long auctionId,
            @CurrentUser User bidder
    ) {
        AuctionResponseDto response = auctionService.placeQuickBid(auctionId, bidder);
        return ResponseEntity.ok(response);
    }

    //경매 취소
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> cancelAuction(
            @PathVariable Long auctionId,
            @CurrentUser User currentUser
    ) {
        auctionService.cancelAuction(auctionId, currentUser);
        return ResponseEntity.noContent().build();
    }

    //단일 경매 조회
    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponseDto> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuction(auctionId));
    }

    //전체 경매 조회
    @GetMapping
    public ResponseEntity<Page<AuctionResponseDto>> getAllAuctions(
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC)
            Pageable pageable
    ) {
        Page<AuctionResponseDto> page = auctionService.getAllAuctions(pageable);
        return ResponseEntity.ok(page);
    }


}
