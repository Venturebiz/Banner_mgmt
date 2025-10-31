package com.obz.banner.repository;

import com.obz.banner.model.Banner;
import com.obz.banner.model.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByStatus(BannerStatus Status);
}
