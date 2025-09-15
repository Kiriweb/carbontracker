import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

type EmissionLog = {
  id: number;
  totalEmissionsKg: number | string;   // backend may serialize BigDecimal as string
  // support both until DTO is finalized
  date?: string;                        // e.g., "2025-09-15"
  createdAt?: string;                   // e.g., "2025-09-15T12:34:56"
  category?: string;
  description?: string;
  co2e?: number | string;
};

export default function DashboardPage() {
  const [logs, setLogs] = useState<EmissionLog[]>([]);
  const [userEmail, setUserEmail] = useState("");
  const [authChecked, setAuthChecked] = useState(false);
  const [authorized, setAuthorized] = useState(false);
  const [aiResponse, setAiResponse] = useState("");
  const nav = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const r = await fetch(`${API_BASE}/api/users/me`, { credentials: "include" });
        if (!r.ok) throw new Error("Unauthorized");

        const user = await r.json();
        if (!user.enabled) throw new Error("User not approved");
        setUserEmail(user.email);
        setAuthorized(true);
      } catch {
        nav("/login");
      } finally {
        setAuthChecked(true);
      }
    })();
  }, []);

  useEffect(() => {
    if (!authorized) return;
    (async () => {
      try {
        const r = await fetch(`${API_BASE}/api/logs`, { credentials: "include" });
        if (r.ok) setLogs(await r.json());
      } catch (e) {
        console.error("Error fetching logs:", e);
      }
    })();
  }, [authorized]);

  async function handleAISuggestions(logId: number) {
    const r = await fetch(`${API_BASE}/api/ai/suggestions/${logId}`, {
      credentials: "include",
    });
    setAiResponse(await r.text());
  }

  if (!authChecked) return null;

  const fmtDate = (l: EmissionLog) => {
    const d = l.createdAt ?? l.date; // prefer createdAt if present
    if (!d) return "-";
    const parsed = new Date(d);
    return isNaN(parsed.getTime()) ? d : parsed.toLocaleDateString();
  };

  const fmtNumber = (v: number | string | undefined) => {
    if (v === undefined || v === null) return "-";
    const n = typeof v === "string" ? Number(v) : v;
    return isNaN(n) ? String(v) : n.toFixed(2);
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Welcome, {userEmail}</h1>

      <h2 className="text-xl font-semibold mb-2">Your Emission Logs</h2>
      {logs.length === 0 ? (
        <p>No logs found.</p>
      ) : (
        <table className="w-full bg-white shadow-md rounded mb-6">
          <thead>
            <tr className="bg-gray-200 text-left">
              <th className="p-2">Date</th>
              <th className="p-2">Total Emissions (kg CO₂e)</th>
              <th className="p-2">Category</th>
              <th className="p-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id} className="border-t">
                <td className="p-2">{fmtDate(log)}</td>
                <td className="p-2">{fmtNumber(log.totalEmissionsKg)}</td>
                <td className="p-2">{log.category ?? "-"}</td>
                <td className="p-2">
                  <button
                    className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded"
                    onClick={() => handleAISuggestions(log.id)}
                  >
                    AI Suggestions
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {aiResponse && (
        <div className="bg-green-50 p-4 border border-green-200 rounded">
          <h3 className="font-semibold text-green-700 mb-2">AI Suggestions:</h3>
          <pre className="whitespace-pre-wrap">{aiResponse}</pre>
        </div>
      )}
    </div>
  );
}
