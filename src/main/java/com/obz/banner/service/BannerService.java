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
                               LocalDateTime startDate , LocalDateTime endDate , String website ,MultipartFile image) throws IOException {
        String imageUrl = s3Service.uploadFile(image);
        Banner banner = new Banner();
                banner.setVendor(vendor);
                banner.setDescription(description);
                banner.setStartDate(startDate);
                banner.setEndDate(endDate);
                banner.setWebsite(website);
                banner.setStatus(determineStatus(startDate , endDate));
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
        return bannerRepository.findAll();
    }

    public Banner getBannerById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Banner not found with this ID"));
        autoUpdateStatus(banner);
        return bannerRepository.save(banner);
    }

    public List<Banner> getLiveBanners() {
        return bannerRepository.findByStatus(BannerStatus.LIVE);
    }


public Banner updateBanner(Long id, String vendor, String description , LocalDateTime
        startDate , LocalDateTime endDate , String website) throws IOException {
        boolean dateChanged = false;
    Banner existing = bannerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Banner not found"));

    if (vendor != null) existing.setVendor(vendor);
    if (description != null) existing.setDescription(description);
    if (startDate != null) existing.setStartDate(startDate);
    if (endDate != null) existing.setEndDate(endDate);
    if (website != null) existing.setWebsite(website);

    existing.setStatus(determineStatus(existing.getStartDate(), existing.getEndDate()));
//    if (startDate != null && !startDate.equals(existing.getStartDate()))
//    {
//        existing.setStartDate(startDate);
//        dateChanged = true;
//    }
//
//    if (endDate != null && !endDate.equals(existing.getEndDate())) {
//        existing.setEndDate(endDate);
//        dateChanged = true;
//    }
//    if (dateChanged) {
//        if (existing.getEndDate() != null && existing.getEndDate().isBefore(LocalDate.now())) {
//            existing.setStatus(BannerStatus.valueOf("EXPIRED"));
//        } else {
//            existing.setStatus(BannerStatus.valueOf("LIVE"));
//        }
//    }
    return bannerRepository.save(existing);
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

    private void autoUpdateStatus(Banner banner) {
        if (banner.getStatus() != BannerStatus.ENDED) {
            banner.setStatus(determineStatus(banner.getStartDate(), banner.getEndDate()));
        }
    }
}
