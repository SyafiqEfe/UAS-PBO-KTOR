import axios from "axios";
const API_URL = "http://localhost:8080";

export const getMahasiswa = async () => {
  const res = await axios.get(`${API_URL}/admin/mahasiswa`);
  return res.data;
};
export const getDosen = async () => {
  const res = await axios.get(`${API_URL}/admin/dosen`);
  return res.data;
};
export const getMatakuliah = async () => {
  const res = await axios.get(`${API_URL}/admin/matakuliah`);
  return res.data;
};
export const addMahasiswa = async (data) => {
  const res = await axios.post(
    `${API_URL}/admin/mahasiswa`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const editMahasiswa = async (nim, data) => {
  const res = await axios.put(
    `${API_URL}/admin/mahasiswa/${nim}`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const deleteMahasiswa = async (nim) => {
  const res = await axios.delete(`${API_URL}/admin/mahasiswa/${nim}`);
  return res.data;
};
export const addDosen = async (data) => {
  const res = await axios.post(
    `${API_URL}/admin/dosen`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const editDosen = async (nidn, data) => {
  const res = await axios.put(
    `${API_URL}/admin/dosen/${nidn}`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const deleteDosen = async (nidn) => {
  const res = await axios.delete(`${API_URL}/admin/dosen/${nidn}`);
  return res.data;
};
export const addMatakuliah = async (data) => {
  const res = await axios.post(
    `${API_URL}/admin/matakuliah`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const editMatakuliah = async (kode, data) => {
  const res = await axios.put(
    `${API_URL}/admin/matakuliah/${kode}`,
    new URLSearchParams(data)
  );
  return res.data;
};
export const deleteMatakuliah = async (kode) => {
  const res = await axios.delete(`${API_URL}/admin/matakuliah/${kode}`);
  return res.data;
};
