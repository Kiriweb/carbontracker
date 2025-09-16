package com.carbontracker.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Central calculator that loads all factor datasets from classpath resources and
 * provides methods to compute CO2e. All keys are normalized to lowercase with underscores.
 */
@Component
public class EmissionCalculatorImpl implements EmissionCalculator {

    private static final String VEHICLE_FACTORS_PATH = "data/passenger_and_motorbike_emission_factors.json";
    private static final String ELECTRICITY_FACTORS_PATH = "data/eu27_electricity_emission_factors.json";
    private static final String WASTE_FACTORS_PATH = "data/waste_disposal_emission_factors_extended.json";
    private static final String FUEL_FACTORS_PATH = "data/fuel_combustion_emission_factors.json";

    private final ObjectMapper mapper = new ObjectMapper();

    /** key: "<vehicle>_<fuel>" in lower-case */
    private final Map<String, Double> vehicleFactors = new HashMap<>();
    /** key: country code in UPPER-CASE; plus ELECTRICITY_DEFAULT */
    private final Map<String, Double> electricityFactors = new HashMap<>();
    /** key: "<wasteType>_<method>" in lower-case (e.g., "clothing_landfill") */
    private final Map<String, Double> wasteFactors = new HashMap<>();
    /** key: "<fuel>_<unit>" in lower-case (e.g., "diesel_litre") */
    private final Map<String, Double> fuelFactors = new HashMap<>();

    @PostConstruct
    public void loadEmissionFactors() {
        try {
            loadVehicleFactors();
            loadElectricityFactors();
            loadWasteFactors();
            loadFuelFactors();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load emission factor JSON files", e);
        }
    }

    // ---------- Loaders ----------

    private void loadVehicleFactors() throws Exception {
        try (InputStream is = openClasspath(VEHICLE_FACTORS_PATH)) {
            Map<String, Number> json = mapper.readValue(is, new TypeReference<Map<String, Number>>() {});
            json.forEach((k, v) -> vehicleFactors.put(
                    k == null ? "" : k.toLowerCase(),
                    v == null ? 0.0 : v.doubleValue()
            ));
        }
    }

    private void loadElectricityFactors() throws Exception {
        try (InputStream is = openClasspath(ELECTRICITY_FACTORS_PATH)) {
            Map<String, Object> json = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            Object perCountryRaw = json.get("electricity_per_country");
            if (perCountryRaw instanceof Map<?, ?> perCountry) {
                for (Map.Entry<?, ?> e : perCountry.entrySet()) {
                    String cc = e.getKey() == null ? "" : e.getKey().toString().toUpperCase();
                    Number n = coerceNumber(e.getValue());
                    electricityFactors.put(cc, n == null ? 0.0 : n.doubleValue());
                }
            }
            Number def = coerceNumber(json.get("electricity_default"));
            electricityFactors.put("ELECTRICITY_DEFAULT", def == null ? 0.5 : def.doubleValue());
        }
    }

    /**
     * Your JSON is flat under "waste_disposal_factors", e.g.:
     * { "clothing_landfill": 496.78, "paper_board_composting": 8.98, ... }
     * We load that inner object directly as a flat map.
     */
    private void loadWasteFactors() throws Exception {
        try (InputStream is = openClasspath(WASTE_FACTORS_PATH)) {
            JsonNode root = mapper.readTree(is);
            JsonNode inner = root.get("waste_disposal_factors");
            if (inner != null && inner.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = inner.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> e = it.next();
                    String key = normalizeKey(e.getKey()); // defensive normalization
                    double val = e.getValue().asDouble(0.0);
                    wasteFactors.put(key, val);
                }
            } else {
                throw new IllegalStateException("Missing object 'waste_disposal_factors' in " + WASTE_FACTORS_PATH);
            }
        }
    }

    private void loadFuelFactors() throws Exception {
        try (InputStream is = openClasspath(FUEL_FACTORS_PATH)) {
            Map<String, Object> root = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            Object payload = root.containsKey("fuel_combustion_factors") ? root.get("fuel_combustion_factors") : root;

            if (!(payload instanceof Map<?, ?> map)) {
                throw new IllegalStateException("Unexpected JSON shape for fuel factors; expected object map.");
            }

            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = normalizeKey(String.valueOf(e.getKey()));
                Number num = coerceNumber(e.getValue());
                if (num == null) {
                    throw new IllegalStateException("Non-numeric value for fuel factor '" + key + "'.");
                }
                fuelFactors.put(key, num.doubleValue());
            }
        }
    }

    // ---------- Helpers ----------

    /** Open a classpath resource or throw a clear error showing which file is missing. */
    private InputStream openClasspath(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new IllegalStateException("Classpath resource not found: " + path + " (place it under src/main/resources/)");
        }
        return is;
    }

    /** Accept various numeric JSON representations (Integer, Double, BigDecimal, String) safely. */
    private Number coerceNumber(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n;
        if (o instanceof String s && !s.isBlank()) return Double.parseDouble(s.trim());
        if (o instanceof BigDecimal bd) return bd.doubleValue();
        return null;
    }

    /** normalize to lowercase with underscores; collapse non [a-z0-9] to '_' and trim leading/trailing underscores */
    private String normalizeKey(String raw) {
        if (raw == null) return "";
        return raw.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    // ---------- API (calculations) ----------

    @Override
    public double calculateVehicleEmissions(String vehicleType, String fuelType, double distanceKm) {
        String key = normalizeKey(vehicleType + "_" + fuelType);
        double factor = vehicleFactors.getOrDefault(key, 0.0);
        return factor * distanceKm;
    }

    @Override
    public double calculateElectricityEmissions(String countryCode, double kwh) {
        String cc = countryCode == null ? "" : countryCode.trim().toUpperCase();
        double factor = electricityFactors.getOrDefault(cc,
                electricityFactors.getOrDefault("ELECTRICITY_DEFAULT", 0.5));
        return factor * kwh;
    }

    /**
     * Waste factors are per tonne; payload weight is in kg.
     * We build "<type>_<method>" (lowercase, underscored) to look up the factor.
     */
    @Override
    public double calculateWasteEmissions(String wasteType, String method, double weightKg) {
        if (weightKg <= 0) return 0.0;

        String key = normalizeKey(
                (wasteType == null ? "" : wasteType) + "_" + (method == null ? "" : method)
        );

        Double perTonne = wasteFactors.get(key);
        if (perTonne == null) {
            // Optional: log warn here if you have a logger
            // LoggerFactory.getLogger(getClass()).warn("Missing waste factor for key: {}", key);
            return 0.0;
        }
        return perTonne * (weightKg / 1000.0);
    }

    @Override
    public double calculateFuelEmissions(String fuelType, String unit, double quantity) {
        String key = normalizeKey(fuelType + "_" + unit);
        double factor = fuelFactors.getOrDefault(key, 0.0);
        return factor * quantity;
    }

    // ---------- Catalog getters for UI ----------

    @Override
    public List<String> getVehicleKeys() {
        List<String> list = new ArrayList<>(vehicleFactors.keySet());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<String> getElectricityCountryCodes() {
        List<String> list = new ArrayList<>();
        for (String k : electricityFactors.keySet()) {
            if (!"ELECTRICITY_DEFAULT".equalsIgnoreCase(k)) {
                list.add(k.toUpperCase());
            }
        }
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    /**
     * Reconstruct waste types and supported methods from the flat keys:
     * split at the last underscore → left = type, right = method.
     * (Types may contain underscores, e.g. "household_residual_waste".)
     */
    @Override
    public Map<String, List<String>> getWasteTypesWithMethods() {
        Map<String, Set<String>> tmp = new HashMap<>();

        for (String flat : wasteFactors.keySet()) {
            int idx = flat.lastIndexOf('_');
            if (idx <= 0 || idx >= flat.length() - 1) continue; // skip malformed
            String type = flat.substring(0, idx);
            String method = flat.substring(idx + 1);
            tmp.computeIfAbsent(type, k -> new TreeSet<>()).add(method);
        }

        Map<String, List<String>> out = new TreeMap<>();
        for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
            out.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    @Override
    public List<String> getFuelKeys() {
        List<String> list = new ArrayList<>(fuelFactors.keySet());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }
}
