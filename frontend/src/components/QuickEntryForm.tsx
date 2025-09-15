import { useState } from "react";
import { saveQuickEntry, QuickPayload } from "../lib/quickApi";

type Props = {
  onSaved: (logDto: any) => void; // parent will merge into logs
};

export default function QuickEntryForm({ onSaved }: Props) {
  const [category, setCategory] =
    useState<QuickPayload["category"]>("electricity use");

  // minimal inputs for each category
  const [vehicleType, setVehicleType] = useState("");
  const [vehicleFuel, setVehicleFuel] = useState("");
  const [distanceKm, setDistanceKm] = useState<number | "">("");

  const [electricityCountry, setElectricityCountry] = useState("");
  const [kwh, setKwh] = useState<number | "">("");

  const [wasteType, setWasteType] = useState("");
  const [wasteMethod, setWasteMethod] = useState("");
  const [wasteKg, setWasteKg] = useState<number | "">("");

  const [fuelType, setFuelType] = useState("");
  const [fuelUnit, setFuelUnit] = useState("");
  const [fuelQuantity, setFuelQuantity] = useState<number | "">("");

  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  function buildPayload(): QuickPayload {
    switch (category) {
      case "vehicle trip":
        return {
          category,
          vehicleType,
          vehicleFuel,
          distanceKm: distanceKm === "" ? 0 : Number(distanceKm),
        };
      case "electricity use":
        return {
          category,
          electricityCountry,
          kwh: kwh === "" ? 0 : Number(kwh),
        };
      case "waste disposal":
        return {
          category,
          wasteType,
          wasteMethod,
          wasteKg: wasteKg === "" ? 0 : Number(wasteKg),
        };
      case "fuel combustion":
        return {
          category,
          fuelType,
          fuelUnit,
          fuelQuantity: fuelQuantity === "" ? 0 : Number(fuelQuantity),
        };
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setErr(null);
    try {
      const dto = await saveQuickEntry(buildPayload());
      onSaved(dto); // let parent update the table
    } catch (ex: any) {
      setErr(ex.message || "Failed to save");
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="bg-white shadow rounded p-4 space-y-3">
      <div className="flex gap-2 items-center">
        <label className="font-medium">Category</label>
        <select
          className="border rounded p-1"
          value={category}
          onChange={(e) =>
            setCategory(e.target.value as QuickPayload["category"])
          }
        >
          <option>electricity use</option>
          <option>vehicle trip</option>
          <option>waste disposal</option>
          <option>fuel combustion</option>
        </select>
      </div>

      {category === "electricity use" && (
        <div className="grid grid-cols-2 gap-2">
          <input
            className="border rounded p-2"
            placeholder="Country (e.g. Greece)"
            value={electricityCountry}
            onChange={(e) => setElectricityCountry(e.target.value)}
          />
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

      {category === "vehicle trip" && (
        <div className="grid grid-cols-3 gap-2">
          <input
            className="border rounded p-2"
            placeholder="Vehicle type (e.g. car)"
            value={vehicleType}
            onChange={(e) => setVehicleType(e.target.value)}
          />
          <input
            className="border rounded p-2"
            placeholder="Fuel (e.g. diesel)"
            value={vehicleFuel}
            onChange={(e) => setVehicleFuel(e.target.value)}
          />
          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Distance km"
            value={distanceKm}
            onChange={(e) =>
              setDistanceKm(e.target.value === "" ? "" : Number(e.target.value))
            }
          />
        </div>
      )}

      {category === "waste disposal" && (
        <div className="grid grid-cols-3 gap-2">
          <input
            className="border rounded p-2"
            placeholder="Waste type"
            value={wasteType}
            onChange={(e) => setWasteType(e.target.value)}
          />
          <input
            className="border rounded p-2"
            placeholder="Method (e.g. landfill)"
            value={wasteMethod}
            onChange={(e) => setWasteMethod(e.target.value)}
          />
          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Weight kg"
            value={wasteKg}
            onChange={(e) =>
              setWasteKg(e.target.value === "" ? "" : Number(e.target.value))
            }
          />
        </div>
      )}

      {category === "fuel combustion" && (
        <div className="grid grid-cols-3 gap-2">
          <input
            className="border rounded p-2"
            placeholder="Fuel type"
            value={fuelType}
            onChange={(e) => setFuelType(e.target.value)}
          />
          <input
            className="border rounded p-2"
            placeholder="Unit (e.g. litre, kwh)"
            value={fuelUnit}
            onChange={(e) => setFuelUnit(e.target.value)}
          />
          <input
            className="border rounded p-2"
            type="number"
            step="0.01"
            placeholder="Quantity"
            value={fuelQuantity}
            onChange={(e) =>
              setFuelQuantity(e.target.value === "" ? "" : Number(e.target.value))
            }
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
