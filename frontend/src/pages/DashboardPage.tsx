import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import QuickEntryForm from "../components/QuickEntryForm";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

type EmissionLog = {
  id: number;
  totalEmissionsKg?: number | string | null;
  co2e?: number | string | null;
  date?: string;
  createdAt?: string;
  category?: string;
  description?: string;
};

export default function DashboardPage() {
  const nav = useNavigate();

  const [me, setMe] = useState<{ email: string; enabled?: boolean } | null>(
    null
  );
  const [logs, setLogs] = useState<EmissionLog[]>([]);
  const [ai, setAi] = useState<string>("");

  // --- auth & bootstrap ---
  useEffect(() => {
    (async () => {
      try {
        const r = await fetch(`${API_BASE}/api/users/me`, {
          credentials: "include",
        });
        if (!r.ok) return nav("/login");
        const u = await r.json();
        if (!u.enabled) return nav("/login");
        setMe(u);

        const l = await fetch(`${API_BASE}/api/logs`, {
          credentials: "include",
        });
        if (l.ok) setLogs(await l.json());
      } catch {
        nav("/login");
      }
    })();
  }, []);

  function fmtDate(l: EmissionLog) {
    const d = l.createdAt ?? l.date;
    if (!d) return "-";
    const parsed = new Date(d);
    return isNaN(parsed.getTime()) ? d : parsed.toLocaleDateString();
  }

  function num(v: number | string | null | undefined) {
    if (v === null || v === undefined) return undefined;
    const n = typeof v === "string" ? Number(v) : v;
    return isNaN(n as number) ? undefined : (n as number);
  }

  function fmtTotal(l: EmissionLog) {
    // Prefer totalEmissionsKg, fallback to co2e
    const n = num(l.totalEmissionsKg) ?? num(l.co2e);
    return n === undefined ? "-" : n.toFixed(2);
  }

  async function aiSuggestionsFor(logId: number) {
    const r = await fetch(`${API_BASE}/api/ai/suggestions/${logId}`, {
      credentials: "include",
    });
    setAi(await r.text());
  }

  if (!me) return null;

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-8">
      <h1 className="text-2xl font-bold">Welcome, {me.email}</h1>

      {/* Quick Entry for users */}
      <section>
        <h2 className="text-xl font-semibold mb-2">Quick Entry</h2>
        <QuickEntryForm
          onSaved={(dto) => {
            // Normalize and prepend to the table
            const newRow: EmissionLog = {
              id: dto.id,
              totalEmissionsKg: dto.totalEmissionsKg,
              co2e: dto.co2e,
              date: dto.date,
              createdAt: dto.createdAt,
              category: dto.category,
              description: dto.description,
            };
            setLogs((prev) => [newRow, ...prev]);

            // Optional: refetch from server to stay canonical
            // fetch(`${API_BASE}/api/logs`, { credentials: "include" })
            //   .then((r) => (r.ok ? r.json() : Promise.reject()))
            //   .then((data) => setLogs(data))
            //   .catch(() => {});
          }}
        />
      </section>

      {/* My logs */}
      <section>
        <h2 className="text-xl font-semibold mb-2">My Emission Logs</h2>
        {logs.length === 0 ? (
          <p>No logs yet.</p>
        ) : (
          <table className="w-full bg-white shadow-md rounded">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="p-2">Date</th>
                <th className="p-2">Total (kg CO₂e)</th>
                <th className="p-2">Category</th>
                <th className="p-2">AI</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l) => (
                <tr key={l.id} className="border-t">
                  <td className="p-2">{fmtDate(l)}</td>
                  <td className="p-2">{fmtTotal(l)}</td>
                  <td className="p-2">{l.category ?? "-"}</td>
                  <td className="p-2">
                    <button
                      className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded"
                      onClick={() => aiSuggestionsFor(l.id)}
                    >
                      Suggestions
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {ai && (
          <div className="bg-green-50 p-4 border border-green-200 rounded mt-3">
            <h3 className="font-semibold text-green-700 mb-2">AI Suggestions</h3>
            <pre className="whitespace-pre-wrap">{ai}</pre>
          </div>
        )}
      </section>
    </div>
  );
}
