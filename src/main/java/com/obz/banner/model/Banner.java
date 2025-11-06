package com.obz.banner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = true) //changed from mandatory to not mandatory
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "start_date", nullable = false) // added Time also
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "website" , nullable = true) // newly added
    private String website;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerStatus status; // LIVE, EXPIRED, ENDED

    public Banner() {}

    public Banner(Long id, String vendor, String description, String imageUrl, LocalDateTime startDate, LocalDateTime endDate, String website, BannerStatus status) {
        this.id = id;
        this.vendor = vendor;
        this.description = description;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.website = website;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public BannerStatus getStatus() { return status; }
    public void setStatus(BannerStatus status) { this.status = status; }


    public static BannerBuilder builder() {
        return new BannerBuilder();
    }

    public static class BannerBuilder {
        private Long id;
        private String vendor;
        private String description;
        private String imageUrl;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String website;
        private BannerStatus status;

        public BannerBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BannerBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public BannerBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BannerBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public BannerBuilder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public BannerBuilder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public BannerBuilder website(String website) {
            this.website = website;
            return this;
        }

        public BannerBuilder status(BannerStatus status) {
            this.status = status;
            return this;
        }

        public Banner build() {
            return new Banner(id, vendor, description, imageUrl, startDate, endDate, website ,status);
        }
    }

    // Auto-status check before persisting/updating
//    @PrePersist
//    @PreUpdate
//    private void updateStatusAutomatically() {
//        if (endDate != null && endDate.isBefore(LocalDate.now())) {
//            this.status = BannerStatus.EXPIRED;
//        } else if (this.status == null) {
//            this.status = BannerStatus.LIVE;
//        }
//    }
}
