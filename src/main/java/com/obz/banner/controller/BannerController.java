package com.obz.banner.controller;

import com.obz.banner.service.BannerService;
import com.obz.banner.model.Banner;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
    public ResponseEntity<?> uploadBanner(
            @RequestParam("vendor") String vendor,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "website", required = false) String website,
            @RequestPart("image") MultipartFile image
    ) {
        try {
            LocalDateTime end = LocalDateTime.parse(endDate);

            Banner banner = bannerService.createBanner(
                    vendor,
                    description,
                    end,
                    website,
                    image
            );

            return ResponseEntity.ok(banner);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid end date format"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Image upload failed"));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }


    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBanner(
            @PathVariable Long id,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "website", required = false) String website,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {

            boolean noUpdates =
                    (vendor == null || vendor.isBlank()) &&
                            (description == null || description.isBlank()) &&
                            (endDateStr == null || endDateStr.isBlank()) &&
                            (website == null || website.isBlank()) &&
                            (image == null || image.isEmpty());

            if (noUpdates) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No fields provided to update")
                );
            }

            LocalDateTime endDate = null;
            if (endDateStr != null && !endDateStr.isBlank()) {
                endDate = LocalDateTime.parse(endDateStr);
            }

            Banner updated = bannerService.updateBanner(
                    id,
                    vendor,
                    description,
                    endDate,
                    website,
                    image
            );

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> getLiveBanners() {
        List<Banner> liveBanners = bannerService.getLiveBanners();

        if (liveBanners.isEmpty()) {
            return buildErrorResponse("No live banners found", HttpStatus.NOT_FOUND);
        }

        return buildListResponse("Live banners fetched successfully", liveBanners);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingBanners() {
        List<Banner> upcomingBanners = bannerService.getUpcomingBanners();
        return buildListResponse( "Upcoming banners fetched successfully", upcomingBanners);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Banner deleted successfully");
        return ResponseEntity.ok(response);
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

    private ResponseEntity<Map<String, Object>> buildListResponse(String message, List<Banner> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    //sample - to test health
    @GetMapping("/health")
    public String health(){
        return "UP";
    }
}
