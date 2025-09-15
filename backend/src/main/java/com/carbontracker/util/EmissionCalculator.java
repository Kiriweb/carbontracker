package com.carbontracker.util;

import java.util.List;
import java.util.Map;

public interface EmissionCalculator {

    // --- Calculations (existing) ---
    double calculateVehicleEmissions(String vehicleType, String fuelType, double distanceKm);
    double calculateElectricityEmissions(String countryCode, double kwh);
    double calculateWasteEmissions(String wasteType, String method, double weightKg);
    double calculateFuelEmissions(String fuelType, String unit, double quantity);

    // --- NEW: Factor catalogs for the UI ---
    /** e.g. ["car_petrol","car_diesel","motorbike_petrol", ...] */
    List<String> getVehicleKeys();

    /** Country codes only, e.g. ["GR","DE","FR", ...] (no ELECTRICITY_DEFAULT). */
    List<String> getElectricityCountryCodes();

    /** e.g. { "paper": ["recycle","landfill","default"], ... } — keys/methods are lower-case. */
    Map<String, List<String>> getWasteTypesWithMethods();

    /** e.g. ["diesel_litre","natural_gas_kwh", ...] */
    List<String> getFuelKeys();
}
