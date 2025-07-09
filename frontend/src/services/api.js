import axios from "axios";

const API_URL = "http://localhost:8080";

export const login = async (username, password, role) => {
  const res = await axios.post(
    `${API_URL}/login`,
    new URLSearchParams({ username, password, role }),
    { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
  );
  return res.data;
};

export const registerMahasiswa = async (nama, password) => {
  try {
    // NIM tidak lagi dibuat di sini, hanya nama dan password yang dikirim
    const params = new URLSearchParams({ nama, password });

    // Panggil endpoint /register yang benar
    const res = await axios.post(`${API_URL}/register`, params, {
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
    });
    return res.data;
  } catch (error) {
    console.error("Backend error:", error.response?.data || error.message);
    throw error;
  }
};

export const getMatakuliah = async () => {
  const res = await axios.get(`${API_URL}/matakuliah`);
  // Backend sekarang mengirim array langsung, jadi kita kembalikan saja res.data
  return res.data;
};

export const submitKRS = async (nim, matkulIds) => {
  const params = new URLSearchParams();
  params.append("nim", nim);
  matkulIds.forEach((id) => params.append("matkulIds[]", id));
  const res = await axios.post(`${API_URL}/krs/submit`, params, {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });
  return res.data;
};

export const getKRS = async (nim) => {
  const res = await axios.get(`${API_URL}/krs/${nim}`);
  return res.data;
};

export const inputNilai = async (krsDetailId, nilai, keterangan) => {
  const res = await axios.post(
    `${API_URL}/nilai/input`,
    new URLSearchParams({ krsDetailId, nilai, keterangan }),
    { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
  );
  return res.data;
};

export const getNilai = async (nim) => {
  const res = await axios.get(`${API_URL}/nilai/${nim}`);
  return res.data;
};
