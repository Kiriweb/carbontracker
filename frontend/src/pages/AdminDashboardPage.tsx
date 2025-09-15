import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";
// Match your seeded admin email from SQL:
const ADMIN_EMAIL = "admin@carbontracker.com";

type UserLite = { id: number; email: string; enabled?: boolean };
type EmissionLog = {
  id: number;
  totalEmissionsKg: number | string;
  date?: string;
  createdAt?: string;
  category?: string;
  description?: string;
  co2e?: number | string;
};

export default function AdminDashboardPage() {
  const nav = useNavigate();

  const [me, setMe] = useState<UserLite | null>(null);
  const [pending, setPending] = useState<UserLite[]>([]);
  const [allUsers, setAllUsers] = useState<UserLite[]>([]);
  const [logs, setLogs] = useState<EmissionLog[]>([]);
  const [ai, setAi] = useState<string>("");

  useEffect(() => {
    (async () => {
      const r = await fetch(`${API_BASE}/api/users/me`, { credentials: "include" });
      if (!r.ok) return nav("/login");

      const u = await r.json();
      if (u.email !== ADMIN_EMAIL || !u.enabled) return nav("/dashboard");
      setMe(u);

      const [p, all, l] = await Promise.all([
        fetch(`${API_BASE}/api/users/pending`, { credentials: "include" }),
        fetch(`${API_BASE}/api/users`, { credentials: "include" }),
        fetch(`${API_BASE}/api/logs`, { credentials: "include" }),
      ]);

      if (p.ok) setPending(await p.json());
      if (all.ok) setAllUsers(await all.json());
      if (l.ok) setLogs(await l.json());
    })();
  }, []);

  async function approveUser(id: number) {
    const r = await fetch(`${API_BASE}/api/users/${id}/approve`, {
      method: "PUT",
      credentials: "include",
    });
    if (r.ok) {
      setPending((s) => s.filter((u) => u.id !== id));
      setAllUsers((s) => s.map((u) => (u.id === id ? { ...u, enabled: true } : u)));
    }
  }

  async function deleteUser(id: number) {
    if (!confirm("Delete this user?")) return;
    const r = await fetch(`${API_BASE}/api/users/${id}`, {
      method: "DELETE",
      credentials: "include",
    });
    if (r.ok) {
      setPending((s) => s.filter((u) => u.id !== id));
      setAllUsers((s) => s.filter((u) => u.id !== id));
    }
  }

  async function aiSuggestionsFor(logId: number) {
    const r = await fetch(`${API_BASE}/api/ai/suggestions/${logId}`, {
      credentials: "include",
    });
    setAi(await r.text());
  }

  if (!me) return null;

  const fmtDate = (l: EmissionLog) => {
    const d = l.createdAt ?? l.date;
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
    <div className="max-w-6xl mx-auto p-6 space-y-8">
      <h1 className="text-2xl font-bold">Admin Dashboard</h1>

      {/* Pending approvals */}
      <section>
        <h2 className="text-xl font-semibold mb-2">Pending Users</h2>
        {pending.length === 0 ? (
          <p>No pending users.</p>
        ) : (
          <table className="w-full bg-white shadow rounded">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="p-2">Email</th>
                <th className="p-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {pending.map((u) => (
                <tr key={u.id} className="border-t">
                  <td className="p-2">{u.email}</td>
                  <td className="p-2 space-x-2">
                    <button
                      className="bg-green-600 text-white px-3 py-1 rounded"
                      onClick={() => approveUser(u.id)}
                    >
                      Approve
                    </button>
                    <button
                      className="bg-red-600 text-white px-3 py-1 rounded"
                      onClick={() => deleteUser(u.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* All users */}
      <section>
        <h2 className="text-xl font-semibold mb-2">All Users</h2>
        {allUsers.length === 0 ? (
          <p>No users.</p>
        ) : (
          <table className="w-full bg-white shadow rounded">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="p-2">Email</th>
                <th className="p-2">Enabled</th>
                <th className="p-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {allUsers.map((u) => (
                <tr key={u.id} className="border-t">
                  <td className="p-2">{u.email}</td>
                  <td className="p-2">{u.enabled ? "Yes" : "No"}</td>
                  <td className="p-2">
                    {u.email !== ADMIN_EMAIL && (
                      <button
                        className="bg-red-600 text-white px-3 py-1 rounded"
                        onClick={() => deleteUser(u.id)}
                      >
                        Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* Admin emissions + AI */}
      <section>
        <h2 className="text-xl font-semibold mb-2">My Emission Logs</h2>
        {logs.length === 0 ? (
          <p>No logs yet.</p>
        ) : (
          <table className="w-full bg-white shadow rounded">
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
                  <td className="p-2">{fmtNumber(l.totalEmissionsKg)}</td>
                  <td className="p-2">{l.category ?? "-"}</td>
                  <td className="p-2">
                    <button
                      className="bg-blue-600 text-white px-3 py-1 rounded"
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
          <div className="mt-3 bg-green-50 border border-green-200 rounded p-3">
            <h3 className="font-semibold text-green-700 mb-1">AI Suggestions</h3>
            <pre className="whitespace-pre-wrap">{ai}</pre>
          </div>
        )}
      </section>
    </div>
  );
}
