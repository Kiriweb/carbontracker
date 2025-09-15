import { useState } from "react";
import { saveQuickEntry } from "../lib/quickApi";
import type { QuickPayload } from "../lib/quickApi";
import {
  VEHICLE_TYPES,
  VEHICLE_FUELS,
  ELECTRICITY_COUNTRIES,
  WASTE_TYPES,
  WASTE_METHODS,
  FUEL_TYPES,
  FUEL_UNITS,
  labelize,
} from "../lib/options";

type Props = { onSaved: (logDto: any) => void };

export default function QuickEntryForm({ onSaved }: Props) {
  const [category, setCategory] =
    useState<QuickPayload["category"]>("electricity use");

  // vehicle
  const [vehicleType, setVehicleType] = useState<typeof VEHICLE_TYPES[number] | "">("");
  const [vehicleFuel, setVehicleFuel] = useState<typeof VEHICLE_FUELS[number] | "">("");
  const [distanceKm, setDistanceKm] = useState<number | "">("");

  // electricity
  const [electricityCountry, setElectricityCountry] = useState<string>("Greece");
  const [kwh, setKwh] = useState<number | "">("");

  // waste
  const [wasteType, setWasteType] = useState<string>("");
  const [wasteMethod, setWasteMethod] = useState<string>("");
  const [wasteKg, setWasteKg] = useState<number | "">("");

  // fuel
  const [fuelType, setFuelType] = useState<string>("");
  const [fuelUnit, setFuelUnit] = useState<string>("");
  const [fuelQuantity, setFuelQuantity] = useState<number | "">("");

  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  function buildPayload(): QuickPayload {
    switch (category) {
      case "vehicle trip":
        return {
          category,
          vehicleType: vehicleType || undefined,
          vehicleFuel: vehicleFuel || undefined,
          distanceKm: distanceKm === "" ? 0 : Number(distanceKm),
        };
      case "electricity use":
        return {
          category,
          electricityCountry: electricityCountry || undefined,
          kwh: kwh === "" ? 0 : Number(kwh),
        };
      case "waste disposal":
        return {
          category,
          wasteType: wasteType || undefined,
          wasteMethod: wasteMethod || undefined,
          wasteKg: wasteKg === "" ? 0 : Number(wasteKg),
        };
      case "fuel combustion":
        return {
          category,
          fuelType: fuelType || undefined,
          fuelUnit: fuelUnit || undefined,
          fuelQuantity: fuelQuantity === "" ? 0 : Number(fuelQuantity),
        };
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setErr(null);
    try {
      // simple validation
      if (category === "vehicle trip" && (!vehicleType || !vehicleFuel)) {
        throw new Error("Please select vehicle type and fuel.");
      }
      if (category === "electricity use" && !electricityCountry) {
        throw new Error("Please select a country.");
      }
      if (category === "waste disposal" && (!wasteType || !wasteMethod)) {
        throw new Error("Please select waste type and method.");
      }
      if (category === "fuel combustion" && (!fuelType || !fuelUnit)) {
        throw new Error("Please select fuel type and unit.");
      }

      const dto = await saveQuickEntry(buildPayload());
      onSaved(dto);

      // reset numeric only
      setDistanceKm("");
      setKwh("");
      setWasteKg("");
      setFuelQuantity("");
    } catch (ex: any) {
      setErr(ex.message || "Failed to save");
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="bg-white shadow rounded p-4 space-y-3">
      {/* Category */}
      <div className="flex gap-2 items-center">
        <label className="font-medium min-w-[84px]">Category</label>
        <select
          className="border rounded p-1"
          value={category}
          onChange={(e) => setCategory(e.target.value as QuickPayload["category"])}
        >
          <option>electricity use</option>
          <option>vehicle trip</option>
          <option>waste disposal</option>
          <option>fuel combustion</option>
        </select>
      </div>

      {/* Vehicle */}
      {category === "vehicle trip" && (
        <div className="grid grid-cols-3 gap-2">
          <select
            className="border rounded p-2"
            value={vehicleType}
            onChange={(e) => setVehicleType(e.target.value as typeof VEHICLE_TYPES[number])}
          >
            <option value="">Select vehicle type…</option>
            {VEHICLE_TYPES.map((v) => (
              <option key={v} value={v}>{labelize(v)}</option>
            ))}
          </select>

          <select
            className="border rounded p-2"
            value={vehicleFuel}
            onChange={(e) => setVehicleFuel(e.target.value as typeof VEHICLE_FUELS[number])}
          >
            <option value="">Select fuel…</option>
            {VEHICLE_FUELS.map((f) => (
              <option key={f} value={f}>{labelize(f)}</option>
            ))}
          </select>

          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Distance km"
            value={distanceKm}
            onChange={(e) => setDistanceKm(e.target.value === "" ? "" : Number(e.target.value))}
          />
        </div>
      )}

      {/* Electricity */}
      {category === "electricity use" && (
        <div className="grid grid-cols-2 gap-2">
          <select
            className="border rounded p-2"
            value={electricityCountry}
            onChange={(e) => setElectricityCountry(e.target.value)}
          >
            {ELECTRICITY_COUNTRIES.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>

          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="kWh"
            value={kwh}
            onChange={(e) => setKwh(e.target.value === "" ? "" : Number(e.target.value))}
          />
        </div>
      )}

      {/* Waste */}
      {category === "waste disposal" && (
        <div className="grid grid-cols-3 gap-2">
          <select
            className="border rounded p-2"
            value={wasteType}
            onChange={(e) => setWasteType(e.target.value)}
          >
            <option value="">Select waste type…</option>
            {WASTE_TYPES.map((t) => (
              <option key={t} value={t}>{labelize(t)}</option>
            ))}
          </select>

          <select
            className="border rounded p-2"
            value={wasteMethod}
            onChange={(e) => setWasteMethod(e.target.value)}
          >
            <option value="">Select method…</option>
            {WASTE_METHODS.map((m) => (
              <option key={m} value={m}>{labelize(m)}</option>
            ))}
          </select>

          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Weight kg"
            value={wasteKg}
            onChange={(e) => setWasteKg(e.target.value === "" ? "" : Number(e.target.value))}
          />
        </div>
      )}

      {/* Fuel */}
      {category === "fuel combustion" && (
        <div className="grid grid-cols-3 gap-2">
          <select
            className="border rounded p-2"
            value={fuelType}
            onChange={(e) => setFuelType(e.target.value)}
          >
            <option value="">Select fuel type…</option>
            {FUEL_TYPES.map((t) => (
              <option key={t} value={t}>{labelize(t)}</option>
            ))}
          </select>

          <select
            className="border rounded p-2"
            value={fuelUnit}
            onChange={(e) => setFuelUnit(e.target.value)}
          >
            <option value="">Select unit…</option>
            {FUEL_UNITS.map((u) => (
              <option key={u} value={u}>{labelize(u)}</option>
            ))}
          </select>

          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Quantity"
            value={fuelQuantity}
            onChange={(e) => setFuelQuantity(e.target.value === "" ? "" : Number(e.target.value))}
          />
        </div>
      )}

      {err && <p className="text-red-600 text-sm">{err}</p>}

      <button
        type="submit"
        disabled={busy}
        className="bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1 rounded disabled:opacity-60"
      >
        {busy ? "Saving…" : "Save Quick Entry"}
      </button>
    </form>
  );
}
