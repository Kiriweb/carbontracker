package com.carbontracker.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Component
public class EmissionCalculatorImpl implements EmissionCalculator {

    private static final String VEHICLE_FACTORS_PATH = "data/passenger_and_motorbike_emission_factors.json";
    private static final String ELECTRICITY_FACTORS_PATH = "data/eu27_electricity_emission_factors.json";
    private static final String WASTE_FACTORS_PATH = "data/waste_disposal_emission_factors_extended.json";
    private static final String FUEL_FACTORS_PATH = "data/fuel_combustion_emission_factors.json";

    private final ObjectMapper mapper = new ObjectMapper();

    // Internal stores
    /** key: "<vehicle>_<fuel>" in lower-case */
    private final Map<String, Double> vehicleFactors = new HashMap<>();
    /** key: country code in UPPER-CASE + "ELECTRICITY_DEFAULT" */
    private final Map<String, Double> electricityFactors = new HashMap<>();
    /** outer: waste type (lower), inner: method (lower) */
    private final Map<String, Map<String, Double>> wasteFactors = new HashMap<>();
    /** key: "<fuel>_<unit>" in lower-case */
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
                    k == null ? null : k.toLowerCase(),
                    v == null ? 0.0 : v.doubleValue()
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadElectricityFactors() throws Exception {
        try (InputStream is = openClasspath(ELECTRICITY_FACTORS_PATH)) {
            Map<String, Object> json = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            Object perCountryRaw = json.get("electricity_per_country");
            if (perCountryRaw instanceof Map<?, ?> perCountry) {
                for (Map.Entry<?, ?> e : perCountry.entrySet()) {
                    String cc = e.getKey() == null ? "" : e.getKey().toString().toUpperCase();
                    Number n = coerceNumber(e.getValue());
                    electricityFactors.put(cc, n.doubleValue());
                }
            }
            Number def = coerceNumber(json.get("electricity_default"));
            electricityFactors.put("ELECTRICITY_DEFAULT", def == null ? 0.5 : def.doubleValue());
        }
    }

    private void loadWasteFactors() throws Exception {
        try (InputStream is = openClasspath(WASTE_FACTORS_PATH)) {
            Map<String, Object> json = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            Object wasteData = json.get("waste_disposal_factors");
            if (wasteData instanceof Map<?, ?> wasteMap) {
                for (Map.Entry<?, ?> entry : wasteMap.entrySet()) {
                    String type = entry.getKey() == null ? "" : entry.getKey().toString().toLowerCase();
                    Object value = entry.getValue();

                    Map<String, Double> methodMap = new HashMap<>();
                    if (value instanceof Map<?, ?> subMap) {
                        for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
                            String method = subEntry.getKey() == null ? "" : subEntry.getKey().toString().toLowerCase();
                            Number n = coerceNumber(subEntry.getValue());
                            methodMap.put(method, n == null ? 0.0 : n.doubleValue());
                        }
                    } else {
                        // single number -> store under "default"
                        Number n = coerceNumber(value);
                        methodMap.put("default", n == null ? 0.0 : n.doubleValue());
                    }
                    wasteFactors.put(type, methodMap);
                }
            }
        }
    }

    private void loadFuelFactors() throws Exception {
        try (InputStream is = openClasspath(FUEL_FACTORS_PATH)) {
            // Read as a generic map first so we can detect the shape
            Map<String, Object> root = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            Object payload = root.containsKey("fuel_combustion_factors") ? root.get("fuel_combustion_factors") : root;

            if (!(payload instanceof Map<?, ?> map)) {
                throw new IllegalStateException("Unexpected JSON shape for fuel factors; expected object map.");
            }

            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = e.getKey().toString().toLowerCase();  // normalize
                Object val = e.getValue();
                if (!(val instanceof Number num)) {
                    throw new IllegalStateException("Non-numeric value for fuel factor '" + key + "'.");
                }
                fuelFactors.put(key, num.doubleValue());
            }

            org.slf4j.LoggerFactory.getLogger(EmissionCalculatorImpl.class)
                    .info("Loaded {} fuel factors.", fuelFactors.size());
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

    // ---------- API (calculations) ----------

    @Override
    public double calculateVehicleEmissions(String vehicleType, String fuelType, double distanceKm) {
        String key = (vehicleType + "_" + fuelType).toLowerCase();
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

    @Override
    public double calculateWasteEmissions(String wasteType, String method, double weightKg) {
        String wt = wasteType == null ? "" : wasteType.toLowerCase();
        String m = method == null ? "" : method.toLowerCase();
        Map<String, Double> methods = wasteFactors.getOrDefault(wt, Map.of());
        double factor = methods.getOrDefault(m, methods.getOrDefault("default", 0.0));
        // Assuming factors are in kg CO2e per tonne -> convert kg to tonnes:
        return factor * (weightKg / 1000.0);
    }

    @Override
    public double calculateFuelEmissions(String fuelType, String unit, double quantity) {
        String key = (fuelType + "_" + unit).toLowerCase();
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

    @Override
    public Map<String, List<String>> getWasteTypesWithMethods() {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : wasteFactors.entrySet()) {
            List<String> methods = new ArrayList<>(entry.getValue().keySet());
            Collections.sort(methods);
            out.put(entry.getKey(), Collections.unmodifiableList(methods));
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
