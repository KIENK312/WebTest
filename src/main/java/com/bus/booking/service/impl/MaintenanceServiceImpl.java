package com.bus.booking.service.impl;

import com.bus.booking.dto.request.ScheduleMaintenanceRequest;
import com.bus.booking.dto.response.ScheduleMaintenanceResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.MaintenanceLogEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.BusStatus;
import com.bus.booking.enums.MaintenanceStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.exception.BusInMaintenanceException;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.BusRepository;
import com.bus.booking.repository.MaintenanceLogRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;

    private static final long BUFFER_HOURS = 48L;

    @Override
    @Transactional
    public ScheduleMaintenanceResponse scheduleMaintenance(ScheduleMaintenanceRequest request) {
        log.info("Yêu cầu lên lịch bảo dưỡng: busId={}, start={}, end={}, type={}",
                request.getBusId(), request.getStartTime(), request.getEndTime(), request.getMaintenanceType());

        // 1. Kiểm tra xe tồn tại trong hệ thống
        BusEntity bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getBusId()));

        // 2. Kiểm tra tính hợp lệ của mốc thời gian
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc bảo trì phải diễn ra sau thời gian bắt đầu!");
        }

        // 3. Quy tắc nghiệp vụ Luồng bảo trì 48h:
        // Khoảng an toàn đệm: [Start - 48h, End + 48h]
        LocalDateTime bufferStart = request.getStartTime().minusHours(BUFFER_HOURS);
        LocalDateTime bufferEnd = request.getEndTime().plusHours(BUFFER_HOURS);

        log.debug("Kiểm tra xung đột chuyến đi SCHEDULED trong khoảng đệm 48h: {} -> {}", bufferStart, bufferEnd);

        List<TripEntity> conflictingTrips = tripRepository.findConflictingTrips(
                bus.getId(),
                TripStatus.SCHEDULED,
                bufferStart,
                bufferEnd
        );

        // 4. Nếu có chuyến đi bị ảnh hưởng -> Hủy thao tác và ném ngoại lệ kèm danh sách ID chuyến
        if (!conflictingTrips.isEmpty()) {
            List<Long> conflictingTripIds = conflictingTrips.stream()
                    .map(TripEntity::getId)
                    .toList();

            log.warn("Phát hiện xung đột lịch bảo trì với {} chuyến đi: {}",
                    conflictingTripIds.size(), conflictingTripIds);

            throw new BusInMaintenanceException(
                    bus.getId(),
                    conflictingTripIds,
                    String.format(
                            "Không thể xếp lịch bảo trì: Xe '%s' (ID: %d) có %d chuyến đi trạng thái SCHEDULED " +
                            "trong khoảng thời gian đệm 48h trước và sau bảo trì (Từ %s đến %s).",
                            bus.getPlateNumber(), bus.getId(), conflictingTripIds.size(), bufferStart, bufferEnd
                    )
            );
        }

        // 5. Kiểm tra xe có đang dính một lịch bảo trì khác trùng lặp không
        boolean hasOverlap = maintenanceLogRepository.hasOverlappingMaintenance(
                bus.getId(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (hasOverlap) {
            throw new BusInMaintenanceException(
                    bus.getId(),
                    List.of(),
                    String.format("Xe '%s' (ID: %d) đã có một lịch bảo dưỡng khác trùng lặp khoảng thời gian này.",
                            bus.getPlateNumber(), bus.getId())
            );
        }

        // 6. Tạo và lưu bản ghi nhật ký bảo trì
        MaintenanceLogEntity maintenanceLog = MaintenanceLogEntity.builder()
                .bus(bus)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maintenanceType(request.getMaintenanceType().trim().toUpperCase())
                .cost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO)
                .description(request.getDescription())
                .status(MaintenanceStatus.IN_PROGRESS)
                .build();

        // Nếu lịch bảo dưỡng bắt đầu ngay bây giờ hoặc trước đó, cập nhật trạng thái xe thành MAINTENANCE
        if (!request.getStartTime().isAfter(LocalDateTime.now())) {
            bus.setStatus(BusStatus.MAINTENANCE);
            busRepository.save(bus);
            log.info("Đã cập nhật trạng thái xe ID={} sang MAINTENANCE", bus.getId());
        }

        MaintenanceLogEntity savedLog = maintenanceLogRepository.save(maintenanceLog);
        log.info("Lên lịch bảo dưỡng thành công: logId={}, busId={}", savedLog.getId(), bus.getId());

        return ScheduleMaintenanceResponse.builder()
                .maintenanceLogId(savedLog.getId())
                .busId(bus.getId())
                .plateNumber(bus.getPlateNumber())
                .busType(bus.getBusType())
                .startTime(savedLog.getStartTime())
                .endTime(savedLog.getEndTime())
                .maintenanceType(savedLog.getMaintenanceType())
                .cost(savedLog.getCost())
                .description(savedLog.getDescription())
                .status(savedLog.getStatus())
                .message("Lên lịch bảo trì thành công và đã vượt qua kiểm tra an toàn đệm 48h.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleMaintenanceResponse> getMaintenanceLogsByBus(Long busId) {
        log.info("Lấy danh sách nhật ký bảo trì của xe ID: {}", busId);

        BusEntity bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        return maintenanceLogRepository.findByBusId(busId).stream()
                .map(logItem -> ScheduleMaintenanceResponse.builder()
                        .maintenanceLogId(logItem.getId())
                        .busId(bus.getId())
                        .plateNumber(bus.getPlateNumber())
                        .busType(bus.getBusType())
                        .startTime(logItem.getStartTime())
                        .endTime(logItem.getEndTime())
                        .maintenanceType(logItem.getMaintenanceType())
                        .cost(logItem.getCost())
                        .description(logItem.getDescription())
                        .status(logItem.getStatus())
                        .message("Thông tin nhật ký bảo dưỡng")
                        .build())
                .toList();
    }
}
