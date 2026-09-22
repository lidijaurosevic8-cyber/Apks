package com.example.model

data class SensorInfo(
    val name: String,
    val vendor: String,
    val typeName: String,
    val powerMa: Float,
    val resolution: Float
)

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val deviceName: String,
    val board: String,
    val hardware: String,
    val socModel: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val securityPatch: String,
    val buildId: String,
    val kernelVersion: String,
    val cpuArchitecture: String,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val totalRamBytes: Long,
    val totalInternalStorageBytes: Long,
    val freeInternalStorageBytes: Long,
    val displayResolution: String,
    val refreshRateHz: Float,
    val supportedRefreshRates: List<Float>,
    val screenDensityDpi: Int,
    val screenDiagonalInches: String,
    val gpuVendor: String,
    val gpuRenderer: String,
    val openGlVersion: String,
    val batteryDesignCapacityMah: Int,
    val thermalSupportDetails: String,
    val sensors: List<SensorInfo>
)
