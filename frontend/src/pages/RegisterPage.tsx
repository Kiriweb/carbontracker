// src/pages/RegisterPage.tsx
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const RegisterPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== repeatPassword) {
      setError("Passwords don't match");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // Backend expects { email, password }
        body: JSON.stringify({ email, password }),
      });

      if (res.ok) {
        // Registration succeeds -> user is created with enabled=false.
        // Send them to Login so they can wait for admin approval.
        navigate("/login");
      } else {
        // Try to read error details
        let msg = "Registration failed";
        try {
          const ct = res.headers.get("content-type");
          if (ct && ct.includes("application/json")) {
            const data = await res.json();
            msg = (data.message as string) || msg;
          } else {
            msg = (await res.text()) || msg;
          }
        } catch {
          // ignore parse errors
        }
        setError(msg);
      }
    } catch (err) {
      console.error(err);
      setError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <form
        onSubmit={handleRegister}
        className="bg-white shadow-md rounded px-8 py-6 w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-4 text-center">Create account</h2>

        {error && (
          <p className="text-red-600 text-sm mb-3" role="alert">
            {error}
          </p>
        )}

        <label className="block text-gray-700 text-sm font-medium mb-1">
          Email
        </label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.currentTarget.value)}
          className="shadow appearance-none border rounded w-full py-2 px-3 mb-4 text-gray-700 leading-tight focus:outline-none"
          placeholder="you@example.com"
          required
          autoComplete="username"
        />

        <label className="block text-gray-700 text-sm font-medium mb-1">
          Password
        </label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.currentTarget.value)}
          className="shadow appearance-none border rounded w-full py-2 px-3 mb-4 text-gray-700 leading-tight focus:outline-none"
          placeholder="••••••••"
          required
          autoComplete="new-password"
          minLength={6}
        />

        <label className="block text-gray-700 text-sm font-medium mb-1">
          Repeat Password
        </label>
        <input
          type="password"
          value={repeatPassword}
          onChange={(e) => setRepeatPassword(e.currentTarget.value)}
          className="shadow appearance-none border rounded w-full py-2 px-3 mb-6 text-gray-700 leading-tight focus:outline-none"
          placeholder="••••••••"
          required
          autoComplete="new-password"
          minLength={6}
        />

        <button
          type="submit"
          disabled={loading}
          className="bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-bold py-2 px-4 rounded w-full"
        >
          {loading ? "Creating account…" : "Register"}
        </button>

        <div className="mt-4 text-center">
          <span className="text-sm text-gray-600">Already have an account? </span>
          <Link to="/login" className="text-sm text-blue-700 hover:underline">
            Sign in
          </Link>
        </div>

        <p className="text-xs text-gray-500 mt-3 text-center">
          After registration, an admin must approve your account before you can use the app.
        </p>
      </form>
    </div>
  );
};

export default RegisterPage;
