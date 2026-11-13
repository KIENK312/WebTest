package com.bus.booking.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory quản lý và phân phối Strategy đặt vé xe khách theo loại hình phương tiện
 */
@Slf4j
@Component
public class TransportStrategyFactory {

    private final Map<String, TransportBookingStrategy> strategyMap = new ConcurrentHashMap<>();
    private final TransportBookingStrategy defaultStrategy;

    public TransportStrategyFactory(List<TransportBookingStrategy> strategies,
                                   StandardBusBookingStrategy standardBusBookingStrategy) {
        for (TransportBookingStrategy strategy : strategies) {
            strategyMap.put(strategy.getTransportType().toUpperCase(), strategy);
        }
        this.defaultStrategy = standardBusBookingStrategy;
        log.info("Khởi tạo TransportStrategyFactory với {} strategies: {}",
                strategyMap.size(), strategyMap.keySet());
    }

    /**
     * Lấy Strategy phù hợp dựa trên chuỗi loại xe khách (ví dụ: LIMOUSINE, VIP, STANDARD...)
     */
    public TransportBookingStrategy getStrategy(String busType) {
        if (busType == null || busType.isBlank()) {
            return defaultStrategy;
        }

        String normalized = busType.trim().toUpperCase();

        if (normalized.contains("LIMO") || normalized.contains("VIP") || normalized.contains("CABIN")) {
            TransportBookingStrategy limo = strategyMap.get(LimousineBusBookingStrategy.TYPE);
            if (limo != null) return limo;
        }

        return strategyMap.getOrDefault(normalized, defaultStrategy);
    }

    /**
     * Danh sách toàn bộ chiến lược đặt vé xe khách đang hoạt động
     */
    public List<TransportBookingStrategy> getAllStrategies() {
        return Collections.unmodifiableList(List.copyOf(strategyMap.values()));
    }
}
