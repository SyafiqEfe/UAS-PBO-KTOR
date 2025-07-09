import React, { useState } from "react";
import { login } from "../services/api";
import "./Login.css";

const roleList = [
  {
    value: "mahasiswa",
    label: "Mahasiswa",
    icon: <i className="fa fa-user-graduate"></i>,
  },
  {
    value: "dosen",
    label: "Dosen",
    icon: <i className="fa fa-chalkboard-teacher"></i>,
  },
  { value: "admin", label: "Admin", icon: <i className="fa fa-user-tie"></i> },
];

export default function Login({ onLogin, onShowRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("mahasiswa");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const res = await login(username, password, role);
    if (res.success) {
      onLogin({ username: res.username, role });
    } else {
      setError(res.message);
    }
  };

  return (
    <form className="login-card" onSubmit={handleSubmit}>
      <div className="login-icon">
        <i className="fa fa-graduation-cap"></i>
      </div>
      <div className="login-title">Sistem Akademik</div>
      <div className="login-subtitle">Selamat datang di portal akademik</div>
      <div className="role-tabs">
        {roleList.map((r) => (
          <button
            type="button"
            key={r.value}
            className={"role-tab" + (role === r.value ? " selected" : "")}
            onClick={() => setRole(r.value)}
          >
            <span>{r.icon}</span>
            <span>{r.label}</span>
          </button>
        ))}
      </div>
      <div className="input-group">
        <span className="input-icon">
          <i className="fa fa-id-card"></i>
        </span>
        <input
          type="text"
          className="login-input"
          placeholder={
            role === "mahasiswa"
              ? "Masukkan NIM Mahasiswa"
              : role === "dosen"
              ? "Masukkan NIDN Dosen"
              : "Masukkan Username Admin"
          }
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
      </div>
      <div className="input-group">
        <span className="input-icon">
          <i className="fa fa-lock"></i>
        </span>
        <input
          className="login-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>
      {/* Register button di bawah password, di atas tombol login */}
      <button
        type="button"
        className="login-btn"
        style={{
          background: "#fff",
          color: "#7b2ff2",
          border: "1.5px solid #7b2ff2",
          marginBottom: 10,
        }}
        onClick={onShowRegister}
      >
        <i className="fa fa-user-plus" style={{ marginRight: 8 }}></i> Register
        Mahasiswa
      </button>
      <button className="login-btn" type="submit">
        <i className="fa fa-sign-in-alt" style={{ marginRight: 8 }}></i> Login
      </button>
      {error && <div className="login-error">{error}</div>}
    </form>
  );
}
