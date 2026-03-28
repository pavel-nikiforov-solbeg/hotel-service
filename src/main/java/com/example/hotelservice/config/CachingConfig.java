package com.example.hotelservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Caffeine-based cache configuration.
 *
 * <ul>
 *   <li>{@code hotels} — individual hotel details keyed by id; evicted on amenity update.</li>
 *   <li>{@code histograms} — grouped counts keyed by {@link com.example.hotelservice.dto.HistogramParam};
 *       evicted on hotel create or amenity update.</li>
 * </ul>
 *
 * For multi-instance deployment replace Caffeine with Redis
 * ({@code spring-boot-starter-data-redis} + {@code @EnableCaching}).
 */
@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Individual hotel details keyed by id — can grow with data set
        manager.registerCustomCache("hotels",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(1000)
                        .build());

        // Histogram counts keyed by HistogramParam — at most 4 entries (one per enum value)
        manager.registerCustomCache("histograms",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(10)
                        .build());

        return manager;
    }
}
