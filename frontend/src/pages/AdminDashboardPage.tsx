import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import QuickEntryForm from "../components/QuickEntryForm";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";
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
type KeyInfo = { hasKey: boolean; masked?: string | null };

export default function AdminDashboardPage() {
  const nav = useNavigate();

  const [me, setMe] = useState<UserLite | null>(null);
  const [pending, setPending] = useState<UserLite[]>([]);
  const [allUsers, setAllUsers] = useState<UserLite[]>([]);
  const [logs, setLogs] = useState<EmissionLog[]>([]);
  const [ai, setAi] = useState<string>("");
  const [keyInfo, setKeyInfo] = useState<KeyInfo | null>(null);
  const [newKey, setNewKey] = useState("");

  useEffect(() => {
    (async () => {
      const r = await fetch(`${API_BASE}/api/users/me`, { credentials: "include" });
      if (!r.ok) return nav("/login");

      const u = await r.json();
      if (u.email !== ADMIN_EMAIL || !u.enabled) return nav("/dashboard");
      setMe(u);

      const [p, all, l, k] = await Promise.all([
        fetch(`${API_BASE}/api/users/pending`, { credentials: "include" }),
        fetch(`${API_BASE}/api/users`, { credentials: "include" }),
        fetch(`${API_BASE}/api/logs`, { credentials: "include" }),
        fetch(`${API_BASE}/api/ai/key`, { credentials: "include" }),
      ]);

      if (p.ok) setPending(await p.json());
      if (all.ok) setAllUsers(await all.json());
      if (l.ok) setLogs(await l.json());
      if (k.ok) setKeyInfo(await k.json());
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

  async function saveKey() {
    const r = await fetch(`${API_BASE}/api/ai/key`, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ apiKey: newKey }),
    });
    if (r.ok) {
      setNewKey("");
      setKeyInfo({ hasKey: true, masked: "****" + newKey.slice(-4) });
    }
  }

  async function removeKey() {
    const r = await fetch(`${API_BASE}/api/ai/key`, {
      method: "DELETE",
      credentials: "include",
    });
    if (r.ok) {
      setKeyInfo({ hasKey: false, masked: "" });
    }
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

      {/* API Key Panel */}
      <section>
        <h2 className="text-xl font-semibold mb-2">OpenAI API Key</h2>
        {keyInfo?.hasKey ? (
          <div className="space-y-2">
            <p>Current key: {keyInfo.masked}</p>
            <button
              onClick={removeKey}
              className="bg-red-600 text-white px-3 py-1 rounded"
            >
              Remove
            </button>
          </div>
        ) : (
          <div className="space-y-2">
            <input
              type="text"
              value={newKey}
              onChange={(e) => setNewKey(e.target.value)}
              placeholder="Enter API Key"
              className="border p-2 rounded w-96"
            />
            <button
              onClick={saveKey}
              className="bg-blue-600 text-white px-3 py-1 rounded"
            >
              Save
            </button>
          </div>
        )}
      </section>

      {/* Pending approvals */}
      {/* ... keep rest of your existing sections here (users, quick entry, logs, AI suggestions) ... */}
    </div>
  );
}
