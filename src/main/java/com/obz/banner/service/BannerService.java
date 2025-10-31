package com.obz.banner.service;

import com.obz.banner.model.Banner;
import com.obz.banner.model.BannerStatus;
import com.obz.banner.repository.BannerRepository;
import jakarta.persistence.EntityNotFoundException;
//import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
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
                            LocalDate startDate , LocalDate endDate , MultipartFile image) throws IOException {
        String imageUrl = s3Service.uploadFile(image);
        Banner banner = new Banner();
                banner.setVendor(vendor);
                banner.setDescription(description);
                banner.setStartDate(startDate);
                banner.setEndDate(endDate);
                banner.setStatus(BannerStatus.LIVE);
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
        return bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Banner not found with this ID"));
    }

    public List<Banner> getLiveBanners() {
        return bannerRepository.findByStatus(BannerStatus.LIVE);
    }


public Banner updateBanner(Long id, String vendor, String description , LocalDate startDate , LocalDate endDate) throws IOException {
        boolean dateChanged = false;
    Banner existing = bannerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Banner not found"));

    if (vendor != null) existing.setVendor(vendor);
    if (description != null) existing.setDescription(description);
    if (startDate != null && !startDate.equals(existing.getStartDate()))
    {
        existing.setStartDate(startDate);
        dateChanged = true;
    }

    if (endDate != null && !endDate.equals(existing.getEndDate())) {
        existing.setEndDate(endDate);
        dateChanged = true;
    }
    if (dateChanged) {
        if (existing.getEndDate() != null && existing.getEndDate().isBefore(LocalDate.now())) {
            existing.setStatus(BannerStatus.valueOf("EXPIRED"));
        } else {
            existing.setStatus(BannerStatus.valueOf("LIVE"));
        }
    }

    return bannerRepository.save(existing);
}

public String forceEndBanner(Long id) {
//        Banner banner = bannerRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Banner not found"));
//
//        if(banner.getStatus() == BannerStatus.ENDED||banner.getStatus() == BannerStatus.EXPIRED)
//        {
//            throw new RuntimeException("Banner already Expired or Ended");
//        }
//
//        banner.setStatus(BannerStatus.ENDED);
//        banner.setEndDate(LocalDate.now());
//        return bannerRepository.save(banner);

    Optional<Banner> optionalBanner = bannerRepository.findById(id);

    if (optionalBanner.isEmpty()) {
        return "Banner not found";
    }

    Banner banner = optionalBanner.get();

    if (banner.getStatus() == BannerStatus.EXPIRED) {
        return "Banner is already expired or ended";
    }

    banner.setStatus(BannerStatus.valueOf("EXPIRED"));
    banner.setEndDate(LocalDate.now());
    bannerRepository.save(banner);

    return "Banner ended successfully";
}

//    public Banner makeBannerLiveAgain(Long id, LocalDate newStartDate, LocalDate newEndDate) {
//        Banner banner = bannerRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Banner not found"));
//
//        if (banner.getStatus() != BannerStatus.EXPIRED || banner.getStatus() != BannerStatus.ENDED) {
//            throw new RuntimeException("Only expired or Ended banners can be reactivated");
//        }
//
//        if (newEndDate.isBefore(newStartDate)) {
//            throw new RuntimeException("End date cannot be before start date");
//        }
//
//        banner.setStartDate(newStartDate);
//        banner.setEndDate(newEndDate);
//        banner.setStatus(BannerStatus.LIVE);
//        return bannerRepository.save(banner);
//    }
}
