package com.obz.banner.controller;

import com.obz.banner.service.BannerService;
import com.obz.banner.model.Banner;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins="*")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<Banner> uploadBanner(
            @RequestParam("vendor") String vendor,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        Banner banner = bannerService.createBanner(
                vendor,
                description,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                image
        );
        return ResponseEntity.ok(banner);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }


    @PutMapping(value = "/{id}" , consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBanner(
            @PathVariable Long id,
            @RequestParam("vendor") String vendor,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate,
            @RequestPart(value = "image", required = false) MultipartFile image

    ) {
        try {
            Banner updatedBanner = bannerService.updateBanner(id, vendor, description, startDate, endDate);
            return ResponseEntity.ok(updatedBanner);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("Banner deleted Successfully");
    }

    @PutMapping("/{id}/force-end")
    public ResponseEntity<Map<String , String>> forceEndBanner(@PathVariable Long id) {
        String message = bannerService.forceEndBanner(id);
//        return ResponseEntity.ok(banner);

        Map<String, String> response = new HashMap<>();
        response.put("message", message);

        if (message.equals("Banner ended successfully")) {
            return ResponseEntity.ok(response);
        } else if (message.equals("Banner is already expired or ended")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

//    @PutMapping("/{id}/make-live-again")
//    public ResponseEntity<Banner> makeLiveAgain(
//            @PathVariable Long id,
//            @RequestParam("newStartDate") String newStartDate,
//            @RequestParam("newEndDate") String newEndDate
//    ) {
//        Banner banner = bannerService.makeBannerLiveAgain(
//                id,
//                LocalDate.parse(newStartDate),
//                LocalDate.parse(newEndDate)
//        );
//        return ResponseEntity.ok(banner);
//    }

    //sample - to test health
    @GetMapping("/health")
    public String health(){
        return "UP";
    }
}
