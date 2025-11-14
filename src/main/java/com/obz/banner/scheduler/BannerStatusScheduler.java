package com.obz.banner.scheduler;

import com.obz.banner.model.Banner;
import com.obz.banner.model.BannerStatus;
import com.obz.banner.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BannerStatusScheduler {
    @Autowired
    private final BannerRepository bannerRepository;

    public BannerStatusScheduler(BannerRepository bannerRepository){
        this.bannerRepository = bannerRepository;
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void updateStatus(){
        LocalDateTime now = LocalDateTime.now();
        List<Banner> banners = bannerRepository.findAll();

        for (Banner b : banners) {
            if (b.getStatus() == BannerStatus.ENDED) {
                continue;
            }
            if (b.getEndDate().isBefore(now)) {
                b.setStatus(BannerStatus.EXPIRED);
            }
            else {
                b.setStatus(BannerStatus.LIVE);
            }
        }

        bannerRepository.saveAll(banners);
        System.out.println("Banner status updated at " + now);
    }
}
