package com.bus.booking.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class BusInMaintenanceException extends RuntimeException {

    private final Long busId;
    private final List<Long> conflictingTripIds;

    public BusInMaintenanceException(String message) {
        super(message);
        this.busId = null;
        this.conflictingTripIds = Collections.emptyList();
    }

    public BusInMaintenanceException(Long busId, String message) {
        super(message);
        this.busId = busId;
        this.conflictingTripIds = Collections.emptyList();
    }

    public BusInMaintenanceException(Long busId, List<Long> conflictingTripIds, String message) {
        super(message);
        this.busId = busId;
        this.conflictingTripIds = conflictingTripIds != null ? conflictingTripIds : Collections.emptyList();
    }
}
