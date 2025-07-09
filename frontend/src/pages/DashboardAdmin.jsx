import React, { useEffect, useState } from "react";
import {
  getMahasiswa,
  getDosen,
  getMatakuliah,
  addMahasiswa,
  deleteMahasiswa,
  addDosen,
  deleteDosen,
  addMatakuliah,
  deleteMatakuliah,
  // editMahasiswa, editDosen, editMatakuliah (akan diimplementasikan jika diperlukan)
} from "../services/admin";

export default function DashboardAdmin() {
  const [mahasiswa, setMahasiswa] = useState([]);
  const [dosen, setDosen] = useState([]);
  const [matkul, setMatkul] = useState([]);
  const [mahasiswaForm, setMahasiswaForm] = useState({}); // State terpisah untuk form mahasiswa
  const [dosenForm, setDosenForm] = useState({}); // State terpisah untuk form dosen
  const [matkulForm, setMatkulForm] = useState({}); // State terpisah untuk form matakuliah
  const [msg, setMsg] = useState("");
  const [activeTab, setActiveTab] = useState("mahasiswa"); // State untuk tab aktif

  // Fungsi untuk refresh semua data
  const refresh = async () => {
    try {
      const resMahasiswa = await getMahasiswa();
      setMahasiswa(resMahasiswa.mahasiswa || []);
      const resDosen = await getDosen();
      setDosen(resDosen.dosen || []);
      const resMatkul = await getMatakuliah();
      setMatkul(resMatkul.matakuliah || []);
    } catch (error) {
      setMsg(
        "Gagal memuat data: " + (error.response?.data?.message || error.message)
      );
      console.error("Error refreshing data:", error);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const handleAddMahasiswa = async (e) => {
    e.preventDefault();
    const res = await addMahasiswa(mahasiswaForm);
    setMsg(res.success ? "Berhasil tambah mahasiswa" : res.message);
    if (res.success) {
      setMahasiswaForm({}); // Reset form
      refresh();
    }
  };

  const handleDeleteMahasiswa = async (nim) => {
    if (window.confirm(`Yakin ingin menghapus mahasiswa dengan NIM ${nim}?`)) {
      const res = await deleteMahasiswa(nim);
      setMsg(res.success ? "Mahasiswa berhasil dihapus" : res.message);
      if (res.success) refresh();
    }
  };

  const handleAddDosen = async (e) => {
    e.preventDefault();
    const res = await addDosen(dosenForm);
    setMsg(res.success ? "Berhasil tambah dosen" : res.message);
    if (res.success) {
      setDosenForm({}); // Reset form
      refresh();
    }
  };

  const handleDeleteDosen = async (nidn) => {
    if (window.confirm(`Yakin ingin menghapus dosen dengan NIDN ${nidn}?`)) {
      const res = await deleteDosen(nidn);
      setMsg(res.success ? "Dosen berhasil dihapus" : res.message);
      if (res.success) refresh();
    }
  };

  const handleAddMatakuliah = async (e) => {
    e.preventDefault();
    const res = await addMatakuliah(matkulForm);
    setMsg(res.success ? "Berhasil tambah mata kuliah" : res.message);
    if (res.success) {
      setMatkulForm({}); // Reset form
      refresh();
    }
  };

  const handleDeleteMatakuliah = async (kode) => {
    if (
      window.confirm(`Yakin ingin menghapus mata kuliah dengan kode ${kode}?`)
    ) {
      const res = await deleteMatakuliah(kode);
      setMsg(res.success ? "Mata Kuliah berhasil dihapus" : res.message);
      if (res.success) refresh();
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.headerTitle}>Dashboard Admin</h2>
      {msg && <div style={styles.messageBox}>{msg}</div>}

      <div style={styles.tabContainer}>
        <button
          onClick={() => setActiveTab("mahasiswa")}
          style={{
            ...styles.tabButton,
            ...(activeTab === "mahasiswa" && styles.tabButtonActive),
          }}
        >
          Mahasiswa
        </button>
        <button
          onClick={() => setActiveTab("dosen")}
          style={{
            ...styles.tabButton,
            ...(activeTab === "dosen" && styles.tabButtonActive),
          }}
        >
          Dosen
        </button>
        <button
          onClick={() => setActiveTab("matakuliah")}
          style={{
            ...styles.tabButton,
            ...(activeTab === "matakuliah" && styles.tabButtonActive),
          }}
        >
          Mata Kuliah
        </button>
      </div>

      {activeTab === "mahasiswa" && (
        <div style={styles.section}>
          <h3>Tambah Mahasiswa</h3>
          <form onSubmit={handleAddMahasiswa} style={styles.form}>
            <input
              placeholder="NIM"
              value={mahasiswaForm.nim || ""}
              onChange={(e) =>
                setMahasiswaForm((f) => ({ ...f, nim: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="Nama"
              value={mahasiswaForm.nama || ""}
              onChange={(e) =>
                setMahasiswaForm((f) => ({ ...f, nama: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={mahasiswaForm.password || ""}
              onChange={(e) =>
                setMahasiswaForm((f) => ({ ...f, password: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="DPA ID (opsional)"
              value={mahasiswaForm.dpaId || ""}
              onChange={(e) =>
                setMahasiswaForm((f) => ({ ...f, dpaId: e.target.value }))
              }
              style={styles.input}
            />
            <button type="submit" style={styles.buttonPrimary}>
              Tambah
            </button>
          </form>

          <h3>Data Mahasiswa</h3>
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>NIM</th>
                  <th style={styles.tableHeader}>Nama</th>
                  <th style={styles.tableHeader}>DPA ID</th>
                  <th style={styles.tableHeader}>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {mahasiswa.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={styles.tableCellCenter}>
                      Tidak ada data mahasiswa.
                    </td>
                  </tr>
                ) : (
                  mahasiswa.map((m) => (
                    <tr key={m.nim} style={styles.tableRow}>
                      <td style={styles.tableCell}>{m.nim}</td>
                      <td style={styles.tableCell}>{m.nama}</td>
                      <td style={styles.tableCell}>{m.dpaId || "-"}</td>
                      <td style={styles.tableCell}>
                        {/* <button style={styles.buttonEdit}>Edit</button> */}
                        <button
                          style={styles.buttonDelete}
                          onClick={() => handleDeleteMahasiswa(m.nim)}
                        >
                          Hapus
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === "dosen" && (
        <div style={styles.section}>
          <h3>Tambah Dosen</h3>
          <form onSubmit={handleAddDosen} style={styles.form}>
            <input
              placeholder="NIDN"
              value={dosenForm.nidn || ""}
              onChange={(e) =>
                setDosenForm((f) => ({ ...f, nidn: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="Nama"
              value={dosenForm.nama || ""}
              onChange={(e) =>
                setDosenForm((f) => ({ ...f, nama: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={dosenForm.password || ""}
              onChange={(e) =>
                setDosenForm((f) => ({ ...f, password: e.target.value }))
              }
              style={styles.input}
              required
            />
            <button type="submit" style={styles.buttonPrimary}>
              Tambah
            </button>
          </form>

          <h3>Data Dosen</h3>
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>NIDN</th>
                  <th style={styles.tableHeader}>Nama</th>
                  <th style={styles.tableHeader}>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {dosen.length === 0 ? (
                  <tr>
                    <td colSpan="3" style={styles.tableCellCenter}>
                      Tidak ada data dosen.
                    </td>
                  </tr>
                ) : (
                  dosen.map((d) => (
                    <tr key={d.nidn} style={styles.tableRow}>
                      <td style={styles.tableCell}>{d.nidn}</td>
                      <td style={styles.tableCell}>{d.nama}</td>
                      <td style={styles.tableCell}>
                        {/* <button style={styles.buttonEdit}>Edit</button> */}
                        <button
                          style={styles.buttonDelete}
                          onClick={() => handleDeleteDosen(d.nidn)}
                        >
                          Hapus
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === "matakuliah" && (
        <div style={styles.section}>
          <h3>Tambah Mata Kuliah</h3>
          <form onSubmit={handleAddMatakuliah} style={styles.form}>
            <input
              placeholder="Kode (e.g., IF001)"
              value={matkulForm.kode || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, kode: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="Nama Mata Kuliah"
              value={matkulForm.nama || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, nama: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              type="number"
              placeholder="SKS"
              value={matkulForm.sks || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, sks: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              type="number"
              placeholder="ID Dosen Pengampu"
              value={matkulForm.dosenId || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, dosenId: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="Ruangan"
              value={matkulForm.ruangan || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, ruangan: e.target.value }))
              }
              style={styles.input}
              required
            />
            <input
              placeholder="Jam Mulai (e.g., 08:00)"
              value={matkulForm.jamMulai || ""}
              onChange={(e) =>
                setMatkulForm((f) => ({ ...f, jamMulai: e.target.value }))
              }
              style={styles.input}
              required
            />
            <button type="submit" style={styles.buttonPrimary}>
              Tambah
            </button>
          </form>

          <h3>Data Mata Kuliah</h3>
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>Kode</th>
                  <th style={styles.tableHeader}>Nama</th>
                  <th style={styles.tableHeader}>SKS</th>
                  <th style={styles.tableHeader}>Dosen</th>
                  <th style={styles.tableHeader}>Ruangan</th>
                  <th style={styles.tableHeader}>Jam</th>
                  <th style={styles.tableHeader}>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {matkul.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={styles.tableCellCenter}>
                      Tidak ada data mata kuliah.
                    </td>
                  </tr>
                ) : (
                  matkul.map((mk) => (
                    <tr key={mk.kode} style={styles.tableRow}>
                      <td style={styles.tableCell}>{mk.kode}</td>
                      <td style={styles.tableCell}>{mk.nama}</td>
                      <td style={styles.tableCellCenter}>{mk.sks}</td>
                      <td style={styles.tableCell}>{mk.dosen}</td>
                      <td style={styles.tableCell}>{mk.ruangan}</td>
                      <td style={styles.tableCell}>{mk.jamMulai}</td>
                      <td style={styles.tableCell}>
                        {/* <button style={styles.buttonEdit}>Edit</button> */}
                        <button
                          style={styles.buttonDelete}
                          onClick={() => handleDeleteMatakuliah(mk.kode)}
                        >
                          Hapus
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

// Basic Styles for DashboardAdmin - Anda bisa memindahkannya ke CSS file
const styles = {
  container: {
    maxWidth: "1200px",
    margin: "32px auto",
    padding: "16px 24px",
    fontFamily: "sans-serif",
    backgroundColor: "#f8f9fa",
    borderRadius: "8px",
    boxShadow: "0 0 10px rgba(0,0,0,0.1)",
  },
  headerTitle: {
    textAlign: "center",
    color: "#343a40",
    marginBottom: "20px",
  },
  messageBox: {
    padding: "10px",
    marginBottom: "20px",
    borderRadius: "5px",
    backgroundColor: "#d4edda", // Green for success
    color: "#155724",
    border: "1px solid #c3e6cb",
    textAlign: "center",
  },
  tabContainer: {
    display: "flex",
    justifyContent: "center",
    marginBottom: "20px",
    borderBottom: "1px solid #dee2e6",
  },
  tabButton: {
    padding: "10px 20px",
    border: "none",
    backgroundColor: "transparent",
    cursor: "pointer",
    fontSize: "1em",
    fontWeight: "bold",
    color: "#6c757d",
    borderBottom: "3px solid transparent",
    transition: "all 0.3s ease",
  },
  tabButtonActive: {
    color: "#007bff",
    borderBottom: "3px solid #007bff",
  },
  section: {
    backgroundColor: "#ffffff",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
    marginBottom: "20px",
  },
  form: {
    display: "flex",
    flexWrap: "wrap",
    gap: "10px",
    marginBottom: "20px",
  },
  input: {
    flex: "1 1 calc(33% - 20px)", // For 3 columns layout
    padding: "10px",
    borderRadius: "5px",
    border: "1px solid #ced4da",
  },
  buttonPrimary: {
    flex: "1 1 auto",
    padding: "10px 15px",
    borderRadius: "5px",
    border: "none",
    backgroundColor: "#007bff",
    color: "white",
    cursor: "pointer",
    fontWeight: "bold",
  },
  tableWrapper: {
    overflowX: "auto",
    marginBottom: "20px",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginBottom: "10px",
  },
  tableHeader: {
    padding: "12px 8px",
    textAlign: "left",
    backgroundColor: "#e9ecef",
    borderBottom: "2px solid #dee2e6",
  },
  tableRow: {
    borderBottom: "1px solid #dee2e6",
  },
  tableCell: {
    padding: "10px 8px",
    verticalAlign: "middle",
  },
  tableCellCenter: {
    padding: "10px 8px",
    verticalAlign: "middle",
    textAlign: "center",
  },
  buttonEdit: {
    padding: "5px 10px",
    borderRadius: "4px",
    border: "none",
    backgroundColor: "#ffc107",
    color: "white",
    cursor: "pointer",
    marginRight: "5px",
  },
  buttonDelete: {
    padding: "5px 10px",
    borderRadius: "4px",
    border: "none",
    backgroundColor: "#dc3545",
    color: "white",
    cursor: "pointer",
  },
};
