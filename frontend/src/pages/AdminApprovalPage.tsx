import { useEffect, useMemo, useState } from "react";

type WasteMap = Record<string, string[]>;

const API = "http://localhost:8080";

async function getJSON<T>(path: string): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    method: "GET",
    credentials: "include", // send JWT cookie
    headers: { Accept: "application/json" },
  });
  if (!res.ok) {
    const msg = await res.text().catch(() => "");
    throw new Error(`${res.status} ${res.statusText} ${msg}`);
  }
  return res.json();
}

export default function AdminApprovalPage() {
  // Pending users (admin section)
  const [pending, setPending] = useState<{ id: number; email: string }[]>([]);
  const [loadingPending, setLoadingPending] = useState(false);
  const [pendingError, setPendingError] = useState<string | null>(null);

  // Factors
  const [wasteMap, setWasteMap] = useState<WasteMap>({});
  const [vehicleKeys, setVehicleKeys] = useState<string[]>([]);
  const [elecCountries, setElecCountries] = useState<string[]>([]);
  const [fuelKeys, setFuelKeys] = useState<string[]>([]);
  const [factorsError, setFactorsError] = useState<string | null>(null);

  // Quick-entry UI
  const categories = ["Vehicle Trip", "Electricity Use", "Waste Disposal", "Fuel Combustion"] as const;
  type Category = typeof categories[number];
  const [cat, setCat] = useState<Category>("Waste Disposal");

  // Waste selection
  const wasteTypes = useMemo(() => Object.keys(wasteMap).sort(), [wasteMap]);
  const [wasteType, setWasteType] = useState<string>("");
  const methods = useMemo(() => (wasteType ? wasteMap[wasteType] ?? [] : []), [wasteMap, wasteType]);
  const [method, setMethod] = useState<string>("");

  // Vehicle selection (“<vehicle>_<fuel>”)
  const [vehicleKey, setVehicleKey] = useState<string>("");

  // Electricity selection
  const [country, setCountry] = useState<string>("");

  // Fuel selection (“<fuel>_<unit>”)
  const [fuelKey, setFuelKey] = useState<string>("");

  // Common numeric input
  const [amount, setAmount] = useState<string>("");

  // helper
  const amountNumber = () => {
    const n = parseFloat(amount);
    return Number.isFinite(n) ? n : 0;
  };

  // Load pending users
  useEffect(() => {
    (async () => {
      try {
        setLoadingPending(true);
        setPendingError(null);
        const data = await getJSON<{ id: number; email: string }[]>("/api/users/pending");
        setPending(data);
      } catch (e: any) {
        console.error("Error loading pending users:", e);
        setPendingError(e?.message ?? "Failed to load");
      } finally {
        setLoadingPending(false);
      }
    })();
  }, []);

  // Load factors (vehicles, electricity, waste, fuels)
  useEffect(() => {
    (async () => {
      try {
        setFactorsError(null);
        const [waste, vehicles, elec, fuels] = await Promise.all([
          getJSON<WasteMap>("/api/factors/waste"),
          getJSON<string[]>("/api/factors/vehicles"),
          getJSON<string[]>("/api/factors/electricity-countries"),
          getJSON<string[]>("/api/factors/fuels"),
        ]);
        setWasteMap(waste);
        setVehicleKeys(vehicles ?? []);
        setElecCountries(elec ?? []);
        setFuelKeys(fuels ?? []);
      } catch (e: any) {
        console.error("Error loading factors:", e);
        setFactorsError(e?.message ?? "Failed to load");
      }
    })();
  }, []);

  // Reset dependent selections when category changes
  useEffect(() => {
    setAmount("");
    if (cat === "Waste Disposal") {
      setWasteType("");
      setMethod("");
    } else if (cat === "Vehicle Trip") {
      setVehicleKey("");
    } else if (cat === "Electricity Use") {
      setCountry("");
    } else if (cat === "Fuel Combustion") {
      setFuelKey("");
    }
  }, [cat]);

  // When waste type changes, reset method
  useEffect(() => setMethod(""), [wasteType]);

  // Admin actions
  const approveUser = async (id: number) => {
    try {
      const res = await fetch(`${API}/api/users/${id}/approve`, {
        method: "PUT",
        credentials: "include",
      });
      if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
      setPending((prev) => prev.filter((u) => u.id !== id));
    } catch (e: any) {
      alert("Failed to approve: " + (e?.message ?? "Unknown error"));
    }
  };

  // === Submit quick entry ===
  const submitQuickEntry = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      let payload: any = { category: cat };

      if (cat === "Vehicle Trip") {
        if (!vehicleKey) return alert("Select vehicle + fuel.");
        const [veh, fuel] = vehicleKey.split("_");
        payload.vehicleType = veh;
        payload.vehicleFuel = fuel;
        payload.distanceKm = amountNumber();
      } else if (cat === "Electricity Use") {
        if (!country) return alert("Select a country.");
        payload.electricityCountry = country;
        payload.kwh = amountNumber();
      } else if (cat === "Waste Disposal") {
        if (!wasteType || !method) return alert("Select waste type and method.");
        payload.wasteType = wasteType;
        payload.wasteMethod = method;
        payload.wasteKg = amountNumber();
      } else if (cat === "Fuel Combustion") {
        if (!fuelKey) return alert("Select fuel + unit.");
        const [fuelType, unit] = fuelKey.split("_");
        payload.fuelType = fuelType;
        payload.fuelUnit = unit;
        payload.fuelQuantity = amountNumber();
      }

      const res = await fetch(`${API}/api/emission-logs/quick`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => "");
        throw new Error(`${res.status} ${res.statusText} ${msg}`);
      }

      const saved = await res.json();
      alert(`Saved! CO₂e = ${
        typeof saved.co2e === "number" ? saved.co2e.toFixed(3) : "—"
      } kg`);
      setAmount("");
    } catch (err: any) {
      console.error(err);
      alert(`Save failed: ${err?.message ?? "Unknown error"}`);
    }
  };

  // Small helpers to display split parts nicely
  const splitKV = (key: string, sep = "_") => {
    const [a, b] = key.split(sep);
    return { a, b };
  };

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-8">
      <h1 className="text-3xl font-extrabold">Admin Dashboard</h1>

      {/* Pending approvals */}
      <section className="space-y-3">
        <h2 className="text-xl font-bold">Pending User Approvals</h2>
        {loadingPending && <p>Loading…</p>}
        {pendingError && <p className="text-red-600">{pendingError}</p>}
        {!loadingPending && pending.length === 0 && <p>No pending users 🎉</p>}
        <ul className="divide-y border rounded">
          {pending.map((u) => (
            <li key={u.id} className="flex items-center justify-between p-3">
              <span>{u.email}</span>
              <button
                onClick={() => approveUser(u.id)}
                className="px-3 py-1 rounded bg-green-600 text-white hover:bg-green-700"
              >
                Approve
              </button>
            </li>
          ))}
        </ul>
      </section>

      {/* Quick emission entry */}
      <section className="space-y-4">
        <h2 className="text-xl font-bold">Quick Emission Entry</h2>
        {factorsError && <p className="text-red-600">{factorsError}</p>}
        <p className="text-sm text-gray-500">
          Loaded: vehicles {vehicleKeys.length}, electricity countries {elecCountries.length}, fuels {fuelKeys.length},{" "}
          waste types {Object.keys(wasteMap).length}
        </p>

        <form onSubmit={submitQuickEntry} className="border rounded p-4 space-y-4">
          {/* Category */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">Category</label>
              <select
                value={cat}
                onChange={(e) => setCat(e.target.value as Category)}
                className="w-full border rounded px-3 py-2"
              >
                {categories.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>

            {/* Column 2 & 3 depend on category */}
            {cat === "Waste Disposal" && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-1">Waste Type</label>
                  <select
                    value={wasteType}
                    onChange={(e) => setWasteType(e.target.value)}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="">— select —</option>
                    {wasteTypes.map((wt) => (
                      <option key={wt} value={wt}>
                        {wt}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Method</label>
                  <select
                    value={method}
                    onChange={(e) => setMethod(e.target.value)}
                    className="w-full border rounded px-3 py-2"
                    disabled={!wasteType}
                  >
                    <option value="">— select —</option>
                    {methods.map((m) => (
                      <option key={m} value={m}>
                        {m}
                      </option>
                    ))}
                  </select>
                </div>
              </>
            )}

            {cat === "Vehicle Trip" && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-1">Vehicle + Fuel</label>
                  <select
                    value={vehicleKey}
                    onChange={(e) => setVehicleKey(e.target.value)}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="">— select —</option>
                    {vehicleKeys.map((vk) => {
                      const { a, b } = splitKV(vk);
                      return (
                        <option key={vk} value={vk}>
                          {a} — {b}
                        </option>
                      );
                    })}
                  </select>
                </div>
                <div />
              </>
            )}

            {cat === "Electricity Use" && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-1">Country</label>
                  <select
                    value={country}
                    onChange={(e) => setCountry(e.target.value)}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="">— select —</option>
                    {elecCountries.map((cc) => (
                      <option key={cc} value={cc}>
                        {cc}
                      </option>
                    ))}
                  </select>
                </div>
                <div />
              </>
            )}

            {cat === "Fuel Combustion" && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-1">Fuel + Unit</label>
                  <select
                    value={fuelKey}
                    onChange={(e) => setFuelKey(e.target.value)}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="">— select —</option>
                    {fuelKeys.map((fk) => {
                      const { a, b } = splitKV(fk);
                      return (
                        <option key={fk} value={fk}>
                          {a} — {b}
                        </option>
                      );
                    })}
                  </select>
                </div>
                <div />
              </>
            )}
          </div>

          {/* Amount */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">Amount</label>
              <input
                type="number"
                step="any"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full border rounded px-3 py-2"
                placeholder={
                  cat === "Vehicle Trip"
                    ? "Distance (km)"
                    : cat === "Electricity Use"
                    ? "kWh"
                    : cat === "Waste Disposal"
                    ? "Weight (kg)"
                    : "Quantity"
                }
              />
            </div>
          </div>

          <button type="submit" className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700">
            Save Quick Entry
          </button>
        </form>
      </section>

      {/* OpenAI Key section can follow here */}
    </div>
  );
}
