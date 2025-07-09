import React, { useState } from "react";
import Login from "./pages/Login";
import RegisterMahasiswa from "./pages/RegisterMahasiswa";
import DashboardMahasiswa from "./pages/DashboardMahasiswa";
import DashboardDosen from "./pages/DashboardDosen";
import DashboardAdmin from "./pages/DashboardAdmin";
import "./Header.css";

function App() {
  const [user, setUser] = useState(null);
  const [showRegister, setShowRegister] = useState(false);

  return (
    <div style={{ minHeight: "100vh", background: "#f8f9fa" }}>
      <div className="krs-header">
        <h1>Sistem KRS</h1>
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        {!user && (
          <div
            className="container card"
            style={{
              marginTop: 0,
              boxShadow: "none",
              background: "none",
              border: "none",
            }}
          >
            {showRegister ? (
              <RegisterMahasiswa onRegister={() => setShowRegister(false)} />
            ) : (
              <Login
                onLogin={setUser}
                onShowRegister={() => setShowRegister(true)}
              />
            )}
          </div>
        )}
        {user && user.role === "mahasiswa" && (
          <DashboardMahasiswa user={user} />
        )}
        {user && user.role === "dosen" && <DashboardDosen user={user} />}
        {user && user.role === "admin" && <DashboardAdmin />}
        {user && !["mahasiswa", "dosen", "admin"].includes(user.role) && (
          <div className="container card">
            Dashboard untuk {user.role} belum tersedia.
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
