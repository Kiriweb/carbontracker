const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

export type QuickPayload = {
  category: "vehicle trip" | "electricity use" | "waste disposal" | "fuel combustion";
  // vehicle
  vehicleType?: string;
  vehicleFuel?: string;
  distanceKm?: number;
  // electricity
  electricityCountry?: string;
  kwh?: number;
  // waste
  wasteType?: string;
  wasteMethod?: string;
  wasteKg?: number;
  // fuel
  fuelType?: string;
  fuelUnit?: string;
  fuelQuantity?: number;
};

export async function saveQuickEntry(payload: QuickPayload) {
  const r = await fetch(`${API_BASE}/api/emission-logs/quick`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!r.ok) {
    const msg = await r.text();
    throw new Error(msg || "Quick entry failed");
  }
  return r.json(); // EmissionLogDTO from backend
}
