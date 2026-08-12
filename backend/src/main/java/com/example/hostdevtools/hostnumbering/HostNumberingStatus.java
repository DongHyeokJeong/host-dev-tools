package com.example.hostdevtools.hostnumbering;

public record HostNumberingStatus(
        boolean testDbConnected,
        String testDbMode,
        String testDbLastSyncAt,
        int testDbEntryCount,
        boolean productionUploaded,
        String productionLastUploadedAt,
        int productionEntryCount
) {}
