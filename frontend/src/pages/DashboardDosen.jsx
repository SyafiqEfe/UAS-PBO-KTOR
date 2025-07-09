import React, { useEffect, useState } from "react";
import { getDosenDashboardData, updateJadwalMatkul } from "../services/dosen";
import { inputNilai } from "../services/api";

// --- Komponen Modal untuk Edit Jadwal ---
const EditJadwalModal = ({ matkul, onClose, onSave }) => {
  const [ruangan, setRuangan] = useState(matkul.ruangan);
  const [jam, setJam] = useState(matkul.jamMulai);

  const handleSave = () => {
    onSave(matkul.id, ruangan, jam);
  };

  return (
    <div style={styles.modalBackdrop}>
      <div style={styles.modalContent}>
        <h3>Edit Jadwal: {matkul.nama}</h3>
        <div style={styles.inputGroup}>
          <label>Ruangan</label>
          <input
            type="text"
            value={ruangan}
            onChange={(e) => setRuangan(e.target.value)}
            style={styles.input}
          />
        </div>
        <div style={styles.inputGroup}>
          <label>Jam Mulai</label>
          <input
            type="text"
            value={jam}
            onChange={(e) => setJam(e.target.value)}
            style={styles.input}
          />
        </div>
        <div style={styles.modalActions}>
          <button onClick={onClose} style={styles.buttonSecondary}>
            Batal
          </button>
          <button onClick={handleSave} style={styles.buttonPrimary}>
            Simpan
          </button>
        </div>
      </div>
    </div>
  );
};

// --- Komponen untuk satu baris mahasiswa ---
const MahasiswaRow = ({ mhs, onNilaiChange, onSimpanNilai }) => (
  <tr style={styles.tableRow}>
    <td style={styles.tableCell}>{mhs.nim}</td>
    <td style={styles.tableCell}>{mhs.nama}</td>
    <td style={styles.tableCell}>
      <input
        type="text"
        placeholder="Contoh: A"
        defaultValue={mhs.nilai || ""}
        onChange={(e) =>
          onNilaiChange(mhs.krsDetailId, "nilai", e.target.value)
        }
        style={{ ...styles.input, width: "80px" }}
      />
    </td>
    <td style={styles.tableCell}>
      <input
        type="text"
        placeholder="Sakit:0, Izin:1, Alpha:0"
        defaultValue={mhs.keterangan || ""}
        onChange={(e) =>
          onNilaiChange(mhs.krsDetailId, "keterangan", e.target.value)
        }
        style={{ ...styles.input, width: "150px" }}
      />
    </td>
    <td style={styles.tableCell}>
      <button
        onClick={() => onSimpanNilai(mhs.krsDetailId)}
        style={styles.buttonSmall}
      >
        Simpan
      </button>
    </td>
  </tr>
);

// --- Komponen utama Dashboard Dosen ---
export default function DashboardDosen({ user }) {
  const [dashboardData, setDashboardData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [nilaiInput, setNilaiInput] = useState({});
  const [editingMatkul, setEditingMatkul] = useState(null); // Untuk modal edit

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const data = await getDosenDashboardData(user.username);
      setDashboardData(data || []);
    } catch (error) {
      console.error("Gagal memuat data dashboard:", error);
      setMessage("Gagal memuat data. Periksa koneksi atau log backend.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (user && user.username) {
      fetchData();
    }
  }, [user]);

  const handleNilaiChange = (krsDetailId, field, value) => {
    setNilaiInput((prev) => ({
      ...prev,
      [krsDetailId]: {
        ...prev[krsDetailId],
        [field]: value,
      },
    }));
  };

  const handleSimpanNilai = async (krsDetailId) => {
    const data = nilaiInput[krsDetailId];
    if (!data || !data.nilai || !data.keterangan) {
      setMessage("Nilai dan Keterangan harus diisi.");
      return;
    }
    try {
      setMessage("Menyimpan nilai...");
      const res = await inputNilai(krsDetailId, data.nilai, data.keterangan);
      setMessage(res.message || "Nilai berhasil disimpan.");
      setTimeout(() => setMessage(""), 3000); // Hapus pesan setelah 3 detik
    } catch (error) {
      setMessage("Gagal menyimpan nilai.");
      console.error(error);
    }
  };

  const handleSaveJadwal = async (matkulId, ruangan, jamMulai) => {
    try {
      setMessage("Memperbarui jadwal...");
      const res = await updateJadwalMatkul(matkulId, ruangan, jamMulai);
      setMessage(res.message || "Jadwal berhasil diperbarui.");
      setEditingMatkul(null); // Tutup modal
      fetchData(); // Muat ulang data
      setTimeout(() => setMessage(""), 3000);
    } catch (error) {
      setMessage("Gagal memperbarui jadwal.");
      console.error(error);
    }
  };

  if (isLoading) {
    return <div style={styles.container}>Memuat data dosen...</div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2>Dashboard Dosen</h2>
        <div>
          <p style={{ margin: 0 }}>
            Selamat datang, <strong>{user.nama}</strong>
          </p>
          <p style={{ margin: 0, color: "#6c757d" }}>NIDN: {user.username}</p>
        </div>
      </div>
      {message && <div style={styles.messageBox}>{message}</div>}

      <h3>Mata Kuliah yang Diampu</h3>
      {dashboardData.length === 0 ? (
        <p>Anda tidak mengampu mata kuliah apapun.</p>
      ) : (
        dashboardData.map((matkul) => (
          <div key={matkul.id} style={styles.matkulCard}>
            <div style={styles.matkulHeader}>
              <div>
                <h4 style={{ margin: 0 }}>
                  {matkul.nama} ({matkul.kode})
                </h4>
                <p style={{ margin: "4px 0 0", color: "#495057" }}>
                  Jadwal: {matkul.ruangan}, {matkul.jamMulai}
                </p>
              </div>
              <button
                onClick={() => setEditingMatkul(matkul)}
                style={styles.buttonSecondary}
              >
                Edit Jadwal
              </button>
            </div>

            <h5 style={styles.subHeader}>Daftar Mahasiswa</h5>
            {matkul.mahasiswa.length === 0 ? (
              <p style={{ textAlign: "center", color: "#6c757d" }}>
                Belum ada mahasiswa yang mengambil mata kuliah ini.
              </p>
            ) : (
              <div style={{ overflowX: "auto" }}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={styles.tableHeader}>NIM</th>
                      <th style={styles.tableHeader}>Nama Mahasiswa</th>
                      <th style={styles.tableHeader}>Nilai Huruf</th>
                      <th style={styles.tableHeader}>Keterangan Absensi</th>
                      <th style={styles.tableHeader}>Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    {matkul.mahasiswa.map((mhs) => (
                      <MahasiswaRow
                        key={mhs.krsDetailId}
                        mhs={mhs}
                        onNilaiChange={handleNilaiChange}
                        onSimpanNilai={handleSimpanNilai}
                      />
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        ))
      )}
      {editingMatkul && (
        <EditJadwalModal
          matkul={editingMatkul}
          onClose={() => setEditingMatkul(null)}
          onSave={handleSaveJadwal}
        />
      )}
    </div>
  );
}

// --- Objek untuk styling, agar kode JSX lebih bersih ---
const styles = {
  container: {
    maxWidth: 1200,
    margin: "32px auto",
    padding: "16px 24px",
    fontFamily: "sans-serif",
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    borderBottom: "2px solid #dee2e6",
    paddingBottom: 16,
    marginBottom: 16,
  },
  messageBox: {
    textAlign: "center",
    padding: 12,
    margin: "16px 0",
    borderRadius: 8,
    background: "#e9ecef",
    fontWeight: "bold",
  },
  matkulCard: {
    background: "#fff",
    border: "1px solid #dee2e6",
    borderRadius: 8,
    marginBottom: 24,
    boxShadow: "0 4px 6px rgba(0,0,0,0.05)",
  },
  matkulHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: 16,
    background: "#f8f9fa",
    borderBottom: "1px solid #dee2e6",
  },
  subHeader: {
    padding: "12px 16px",
    margin: 0,
    background: "#f1f3f5",
    borderBottom: "1px solid #dee2e6",
  },
  table: { width: "100%", borderCollapse: "collapse" },
  tableHeader: {
    padding: 12,
    textAlign: "left",
    background: "#e9ecef",
    borderBottom: "2px solid #dee2e6",
  },
  tableRow: { borderBottom: "1px solid #f1f3f5" },
  tableCell: { padding: 12, verticalAlign: "middle" },
  input: {
    padding: "8px 12px",
    borderRadius: 4,
    border: "1px solid #ced4da",
    width: "100%",
  },
  buttonPrimary: {
    padding: "10px 16px",
    border: "none",
    borderRadius: 4,
    background: "#0d6efd",
    color: "white",
    cursor: "pointer",
  },
  buttonSecondary: {
    padding: "8px 12px",
    border: "1px solid #6c757d",
    borderRadius: 4,
    background: "white",
    color: "#6c757d",
    cursor: "pointer",
  },
  buttonSmall: {
    padding: "6px 10px",
    border: "none",
    borderRadius: 4,
    background: "#198754",
    color: "white",
    cursor: "pointer",
  },
  modalBackdrop: {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    background: "rgba(0,0,0,0.5)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  modalContent: {
    background: "white",
    padding: 24,
    borderRadius: 8,
    width: "90%",
    maxWidth: 500,
  },
  modalActions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: 12,
    marginTop: 24,
  },
  inputGroup: { marginBottom: 16 },
};
