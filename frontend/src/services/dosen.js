import axios from "axios";
const API_URL = "http://localhost:8080";

// Mengambil semua data dashboard untuk dosen (info matkul + mahasiswa)
export const getDosenDashboardData = async (nidn) => {
  const res = await axios.get(`${API_URL}/dosen/${nidn}/dashboard`);
  return res.data;
};

// Mengupdate jadwal mata kuliah
export const updateJadwalMatkul = async (matkulId, ruangan, jamMulai) => {
  const params = new URLSearchParams({ ruangan, jamMulai });
  const res = await axios.put(`${API_URL}/dosen/matkul/${matkulId}`, params, {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });
  return res.data;
};
