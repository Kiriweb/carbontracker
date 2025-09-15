package com.carbontracker.dto;

public record QuickEmissionRequest(
        String category,

        // Vehicle
        String vehicleType,
        String vehicleFuel,
        Double distanceKm,

        // Electricity
        String electricityCountry,
        Double kwh,

        // Waste
        String wasteType,
        String wasteMethod,
        Double wasteKg,

        // Fuel
        String fuelType,
        String fuelUnit,
        Double fuelQuantity
) {}
