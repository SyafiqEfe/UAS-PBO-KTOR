import React, { useState } from "react";
import { registerMahasiswa } from "../services/api";
import "./RegisterMahasiswa.css";

export default function RegisterMahasiswa({ onRegister }) {
  const [nama, setNama] = useState("");
  const [password, setPassword] = useState("");
  const [nim, setNim] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [showPopup, setShowPopup] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess(false);
    try {
      const res = await registerMahasiswa(nama, password);
      if (res.success) {
        setNim(res.nim);
        setSuccess(true);
        setTimeout(() => setShowPopup(true), 400); // delay animasi popup
        onRegister && onRegister(res.nim);
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError("Terjadi kesalahan. Silakan coba lagi.");
    }
  };

  return (
    <>
      <form
        className="container card"
        onSubmit={handleSubmit}
        style={{ filter: showPopup ? "blur(2px)" : "none" }}
      >
        <h2>Register Mahasiswa</h2>
        <div className="form-group">
          <label>Nama</label>
          <input
            placeholder="Nama"
            value={nama}
            onChange={(e) => setNama(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button className="btn-primary" type="submit">
          Register
        </button>
        {error && <div className="alert-error">{error}</div>}
      </form>
      {showPopup && (
        <div className="modal-overlay">
          <div className="modal-popup">
            <h2 style={{ color: "#7b2ff2", marginBottom: 0 }}>
              Registrasi Berhasil!
            </h2>
            <p style={{ marginTop: 8 }}>
              Selamat, Anda telah terdaftar sebagai mahasiswa.
            </p>
            <div className="nim-popup">
              NIM Anda: <span>{nim}</span>
            </div>
            <div
              style={{
                color: "#c62828",
                fontWeight: 500,
                marginBottom: 12,
              }}
            >
              <i className="fa fa-info-circle" style={{ marginRight: 6 }}></i>
              Simpan NIM ini! NIM digunakan untuk login sebagai mahasiswa.
            </div>
            <button
              className="btn-popup"
              onClick={() => window.location.reload()}
            >
              Kembali ke Login
            </button>
          </div>
        </div>
      )}
    </>
  );
}
