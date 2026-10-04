package com.samadhan.entity;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_version_config")
@Data
public class AppVersionConfig {

    /** Package name (Android) or bundle identifier (iOS) — e.g. com.transfereaze.user */
    @Id
    @Column(name = "app_id", length = 100)
    private String appId;

    @Column(name = "min_version", nullable = false, length = 20)
    private String minVersion;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
