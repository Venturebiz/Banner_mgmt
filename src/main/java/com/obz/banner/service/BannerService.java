package com.obz.banner.service;

import com.obz.banner.model.Banner;
import com.obz.banner.model.BannerStatus;
import com.obz.banner.repository.BannerRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BannerService {

    private final S3Service s3Service;

    @Autowired
    private final BannerRepository bannerRepository;

//    @Value("${aws.s3.bucket}")
//    private String bucketName;

    public BannerService(BannerRepository bannerRepository, S3Service s3Service) {
        this.bannerRepository =bannerRepository;
        this.s3Service = s3Service;
    }


    public Banner createBanner(String vendor, String description ,
                                LocalDateTime endDate , String website ,MultipartFile image) throws IOException {
        String imageUrl = s3Service.uploadFile(image);
        Banner banner = new Banner();
                banner.setVendor(vendor);
                banner.setDescription(description);
//                banner.setStartDate(startDate);
                banner.setEndDate(endDate);
                banner.setWebsite(website);
                banner.setStatus(determineStatus(LocalDateTime.now() , endDate));
                banner.setImageUrl(imageUrl);
        return bannerRepository.save(banner);
    }

public void deleteBanner(Long id) {
    Banner banner = bannerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Banner not found"));
    String imageUrl = banner.getImageUrl();
    String key = imageUrl.substring(imageUrl.indexOf(".com/")+5);
    s3Service.deleteFile(key);

    bannerRepository.delete(banner);
}

    public List<Banner> getAllBanners() {
        List<Banner> banners = bannerRepository.findAll();

        return banners.stream()
                .sorted((b1, b2) -> {
                    int p1 = getStatusPriority(b1.getStatus());
                    int p2 = getStatusPriority(b2.getStatus());

                    if (p1 != p2) return Integer.compare(p1, p2);

                    return b2.getStartDate().compareTo(b1.getStartDate());
                })
                .toList();
    }

    public Banner getBannerById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Banner not found with this ID"));
        autoUpdateStatus(banner);
        return bannerRepository.save(banner);
    }

    public List<Banner> getLiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        return bannerRepository.findAll().stream()
                .filter(b -> b.getStartDate().isBefore(now) && b.getEndDate().isAfter(now) && b.getStatus() != BannerStatus.ENDED)
                .toList();
    }


    public Banner updateBanner(Long id,
                               String vendor,
                               String description,
                               LocalDateTime endDate,
                               String website,
                               MultipartFile image) throws IOException {

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));


        if (vendor != null) banner.setVendor(vendor);
        if (description != null) banner.setDescription(description);
        if (website != null) banner.setWebsite(website);

        if (endDate != null) {
            banner.setEndDate(endDate);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image);
            banner.setImageUrl(imageUrl);
        }


        LocalDateTime now = LocalDateTime.now();

        if (banner.getStatus() != BannerStatus.ENDED) {
            if (banner.getEndDate().isBefore(now)) {
                banner.setStatus(BannerStatus.EXPIRED);
            } else {
                banner.setStatus(BannerStatus.LIVE);
            }
        }

        return bannerRepository.save(banner);
    }


    public List<Banner> getUpcomingBanners() {
        return bannerRepository.findByStatus(BannerStatus.UPCOMING);
    }


public String forceEndBanner(Long id) {

    Optional<Banner> optionalBanner = bannerRepository.findById(id);

    if (optionalBanner.isEmpty()) {
        return "Banner not found";
    }

    Banner banner = optionalBanner.get();

    if (banner.getStatus() == BannerStatus.EXPIRED || banner.getStatus() == BannerStatus.ENDED) {
        return "Banner is already expired or ended";
    }

    banner.setStatus(BannerStatus.ENDED);
    banner.setEndDate(LocalDateTime.now());
    bannerRepository.save(banner);

    return "Banner ended successfully";
}
//Utility classes
private BannerStatus determineStatus(LocalDateTime start, LocalDateTime end) {
    LocalDateTime now = LocalDateTime.now();

    if (end.isBefore(now)) {
        return BannerStatus.EXPIRED;
    } else if (start.isAfter(now)) {
        return BannerStatus.UPCOMING;
    } else {
        return BannerStatus.LIVE;
    }
}

    private int getStatusPriority(BannerStatus status) {
        return switch (status) {
            case LIVE -> 1;
            case EXPIRED -> 2;
            case ENDED -> 3;
            case UPCOMING -> 4;
        };
    }

private void autoUpdateStatus(Banner banner) {
   if (banner.getStatus() != BannerStatus.ENDED) {
       banner.setStatus(determineStatus(banner.getStartDate(), banner.getEndDate()));
   }
   }
}
