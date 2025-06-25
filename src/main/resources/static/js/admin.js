const API_BASE = "http://localhost:8080"

// Check authentication
function checkAuth() {
  const token = localStorage.getItem("token")
  const userType = localStorage.getItem("userType")

  if (!token || userType !== "admin") {
    window.location.href = "index.html"
    return false
  }

  return true
}

// Logout
function logout() {
  localStorage.clear()
  window.location.href = "index.html"
}

// Show/Hide sections
function showSection(sectionName) {
  document.querySelectorAll(".section").forEach((section) => {
    section.classList.add("hidden")
  })

  if (sectionName) {
    document.getElementById(sectionName + "-section").classList.remove("hidden")

    switch (sectionName) {
      case "mahasiswa":
        loadMahasiswa()
        break
      case "dosen":
        loadDosen()
        break
      case "matakuliah":
        loadMatakuliah()
        break
      case "laporan":
        loadLaporan()
        break
    }
  }
}

// Show/Hide loading
function showLoading() {
  document.getElementById("loading").classList.remove("hidden")
}

function hideLoading() {
  document.getElementById("loading").classList.add("hidden")
}

// Show/Hide modal
function showModal() {
  document.getElementById("form-modal").style.display = "block"
}

function hideModal() {
  document.getElementById("form-modal").style.display = "none"
}

// Show alert
function showAlert(message, type = "success") {
  const alertDiv = document.createElement("div")
  alertDiv.className = `alert alert-${type}`
  alertDiv.innerHTML = `
        <i class="fas fa-${type === "success" ? "check-circle" : "exclamation-triangle"}"></i>
        ${message}
    `

  document.querySelector(".dashboard").insertBefore(alertDiv, document.querySelector(".dashboard").firstChild)

  setTimeout(() => {
    alertDiv.remove()
  }, 5000)
}

// API call helper
async function apiCall(endpoint, options = {}) {
  const token = localStorage.getItem("token")
  const defaultOptions = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...defaultOptions,
    ...options,
    headers: { ...defaultOptions.headers, ...options.headers },
  })

  if (response.status === 401) {
    logout()
    return
  }

  return response
}

// Load mahasiswa
async function loadMahasiswa() {
  showLoading()

  try {
    const response = await apiCall("/admin/mahasiswa")
    const data = await response.json()

    if (response.ok) {
      renderMahasiswa(data)
    }
  } catch (error) {
    showAlert("Gagal memuat data mahasiswa", "error")
  } finally {
    hideLoading()
  }
}

// Render mahasiswa table
function renderMahasiswa(mahasiswa) {
  const tbody = document.querySelector("#mahasiswa-table tbody")
  tbody.innerHTML = ""

  mahasiswa.forEach((mhs) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${mhs.nim}</td>
            <td>${mhs.nama}</td>
            <td>${mhs.email}</td>
            <td>${mhs.totalSKS || 0}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="editMahasiswa('${mhs.id}')">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteMahasiswa('${mhs.id}')">
                    <i class="fas fa-trash"></i> Hapus
                </button>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Show add mahasiswa form
function showAddMahasiswa() {
  document.getElementById("modal-title").textContent = "Tambah Mahasiswa"
  document.getElementById("modal-body").innerHTML = `
        <form id="mahasiswa-form">
            <div class="form-row">
                <div class="form-group">
                    <label>Nama Lengkap</label>
                    <input type="text" id="mhs-nama" required>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="mhs-email" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="mhs-password" required>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-danger" onclick="hideModal()">
                    <i class="fas fa-times"></i> Batal
                </button>
                <button type="submit" class="btn btn-success">
                    <i class="fas fa-save"></i> Simpan
                </button>
            </div>
        </form>
    `

  showModal()

  document.getElementById("mahasiswa-form").addEventListener("submit", async (e) => {
    e.preventDefault()

    const nama = document.getElementById("mhs-nama").value
    const email = document.getElementById("mhs-email").value
    const password = document.getElementById("mhs-password").value

    showLoading()

    try {
      const response = await apiCall("/admin/mahasiswa", {
        method: "POST",
        body: JSON.stringify({ nama, email, password }),
      })

      if (response.ok) {
        showAlert("Mahasiswa berhasil ditambahkan", "success")
        hideModal()
        loadMahasiswa()
      } else {
        const data = await response.json()
        showAlert(data.message || "Gagal menambahkan mahasiswa", "error")
      }
    } catch (error) {
      showAlert("Terjadi kesalahan", "error")
    } finally {
      hideLoading()
    }
  })
}

// Load dosen
async function loadDosen() {
  showLoading()

  try {
    const response = await apiCall("/admin/dosen")
    const data = await response.json()

    if (response.ok) {
      renderDosen(data)
    }
  } catch (error) {
    showAlert("Gagal memuat data dosen", "error")
  } finally {
    hideLoading()
  }
}

// Render dosen table
function renderDosen(dosen) {
  const tbody = document.querySelector("#dosen-table tbody")
  tbody.innerHTML = ""

  dosen.forEach((dsn) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${dsn.nidn}</td>
            <td>${dsn.nama}</td>
            <td>${dsn.matakuliah ? dsn.matakuliah.join(", ") : "Belum ada"}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="editDosen('${dsn.id}')">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteDosen('${dsn.id}')">
                    <i class="fas fa-trash"></i> Hapus
                </button>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Show add dosen form
function showAddDosen() {
  document.getElementById("modal-title").textContent = "Tambah Dosen"
  document.getElementById("modal-body").innerHTML = `
        <form id="dosen-form">
            <div class="form-row">
                <div class="form-group">
                    <label>NIDN</label>
                    <input type="text" id="dsn-nidn" required>
                </div>
                <div class="form-group">
                    <label>Nama Lengkap</label>
                    <input type="text" id="dsn-nama" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="dsn-password" required>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-danger" onclick="hideModal()">
                    <i class="fas fa-times"></i> Batal
                </button>
                <button type="submit" class="btn btn-success">
                    <i class="fas fa-save"></i> Simpan
                </button>
            </div>
        </form>
    `

  showModal()

  document.getElementById("dosen-form").addEventListener("submit", async (e) => {
    e.preventDefault()

    const nidn = document.getElementById("dsn-nidn").value
    const nama = document.getElementById("dsn-nama").value
    const password = document.getElementById("dsn-password").value

    showLoading()

    try {
      const response = await apiCall("/admin/dosen", {
        method: "POST",
        body: JSON.stringify({ nidn, nama, password }),
      })

      if (response.ok) {
        showAlert("Dosen berhasil ditambahkan", "success")
        hideModal()
        loadDosen()
      } else {
        const data = await response.json()
        showAlert(data.message || "Gagal menambahkan dosen", "error")
      }
    } catch (error) {
      showAlert("Terjadi kesalahan", "error")
    } finally {
      hideLoading()
    }
  })
}

// Load mata kuliah
async function loadMatakuliah() {
  showLoading()

  try {
    const response = await apiCall("/admin/matakuliah")
    const data = await response.json()

    if (response.ok) {
      renderMatakuliah(data)
    }
  } catch (error) {
    showAlert("Gagal memuat data mata kuliah", "error")
  } finally {
    hideLoading()
  }
}

// Render mata kuliah table
function renderMatakuliah(matakuliah) {
  const tbody = document.querySelector("#matakuliah-table tbody")
  tbody.innerHTML = ""

  matakuliah.forEach((mk) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${mk.nama}</td>
            <td>${mk.sks}</td>
            <td>${mk.dosenNama || "Belum ditentukan"}</td>
            <td>${mk.jamMulai}</td>
            <td>${mk.ruangan}</td>
            <td>${mk.jumlahMahasiswa || 0}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="editMatakuliah('${mk.id}')">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteMatakuliah('${mk.id}')">
                    <i class="fas fa-trash"></i> Hapus
                </button>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Show add mata kuliah form
async function showAddMatakuliah() {
  // Load dosen list first
  const dosenResponse = await apiCall("/admin/dosen")
  const dosenData = await dosenResponse.json()

  document.getElementById("modal-title").textContent = "Tambah Mata Kuliah"
  document.getElementById("modal-body").innerHTML = `
        <form id="matakuliah-form">
            <div class="form-row">
                <div class="form-group">
                    <label>Nama Mata Kuliah</label>
                    <input type="text" id="mk-nama" required>
                </div>
                <div class="form-group">
                    <label>SKS</label>
                    <input type="number" id="mk-sks" min="1" max="6" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Dosen</label>
                    <select id="mk-dosen" required>
                        <option value="">-- Pilih Dosen --</option>
                        ${dosenData.map((dosen) => `<option value="${dosen.id}">${dosen.nama}</option>`).join("")}
                    </select>
                </div>
                <div class="form-group">
                    <label>Jam Mulai</label>
                    <input type="time" id="mk-jam" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Ruangan</label>
                    <input type="text" id="mk-ruangan" required>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-danger" onclick="hideModal()">
                    <i class="fas fa-times"></i> Batal
                </button>
                <button type="submit" class="btn btn-success">
                    <i class="fas fa-save"></i> Simpan
                </button>
            </div>
        </form>
    `

  showModal()

  document.getElementById("matakuliah-form").addEventListener("submit", async (e) => {
    e.preventDefault()

    const nama = document.getElementById("mk-nama").value
    const sks = Number.parseInt(document.getElementById("mk-sks").value)
    const dosenId = document.getElementById("mk-dosen").value
    const jamMulai = document.getElementById("mk-jam").value
    const ruangan = document.getElementById("mk-ruangan").value

    showLoading()

    try {
      const response = await apiCall("/admin/matakuliah", {
        method: "POST",
        body: JSON.stringify({ nama, sks, dosenId, jamMulai, ruangan }),
      })

      if (response.ok) {
        showAlert("Mata kuliah berhasil ditambahkan", "success")
        hideModal()
        loadMatakuliah()
      } else {
        const data = await response.json()
        showAlert(data.message || "Gagal menambahkan mata kuliah", "error")
      }
    } catch (error) {
      showAlert("Terjadi kesalahan", "error")
    } finally {
      hideLoading()
    }
  })
}

// Load laporan
async function loadLaporan() {
  showLoading()

  try {
    const [mahasiswaRes, dosenRes, matkulRes] = await Promise.all([
      apiCall("/admin/mahasiswa"),
      apiCall("/admin/dosen"),
      apiCall("/admin/matakuliah"),
    ])

    const mahasiswaData = await mahasiswaRes.json()
    const dosenData = await dosenRes.json()
    const matkulData = await matkulRes.json()

    if (mahasiswaRes.ok && dosenRes.ok && matkulRes.ok) {
      document.getElementById("total-mahasiswa").textContent = mahasiswaData.length
      document.getElementById("total-dosen").textContent = dosenData.length
      document.getElementById("total-matakuliah").textContent = matkulData.length

      const totalSKS = mahasiswaData.reduce((sum, mhs) => sum + (mhs.totalSKS || 0), 0)
      const rataSKS = mahasiswaData.length > 0 ? (totalSKS / mahasiswaData.length).toFixed(1) : 0
      document.getElementById("rata-sks").textContent = rataSKS
    }
  } catch (error) {
    showAlert("Gagal memuat laporan", "error")
  } finally {
    hideLoading()
  }
}

// Delete functions
async function deleteMahasiswa(id) {
  if (!confirm("Apakah Anda yakin ingin menghapus mahasiswa ini?")) {
    return
  }

  showLoading()

  try {
    const response = await apiCall(`/admin/mahasiswa/${id}`, {
      method: "DELETE",
    })

    if (response.ok) {
      showAlert("Mahasiswa berhasil dihapus", "success")
      loadMahasiswa()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal menghapus mahasiswa", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
}

async function deleteDosen(id) {
  if (!confirm("Apakah Anda yakin ingin menghapus dosen ini?")) {
    return
  }

  showLoading()

  try {
    const response = await apiCall(`/admin/dosen/${id}`, {
      method: "DELETE",
    })

    if (response.ok) {
      showAlert("Dosen berhasil dihapus", "success")
      loadDosen()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal menghapus dosen", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
}

async function deleteMatakuliah(id) {
  if (!confirm("Apakah Anda yakin ingin menghapus mata kuliah ini?")) {
    return
  }

  showLoading()

  try {
    const response = await apiCall(`/admin/matakuliah/${id}`, {
      method: "DELETE",
    })

    if (response.ok) {
      showAlert("Mata kuliah berhasil dihapus", "success")
      loadMatakuliah()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal menghapus mata kuliah", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
}

// Initialize page
document.addEventListener("DOMContentLoaded", () => {
  if (checkAuth()) {
    loadLaporan()
  }
})

// Close modal when clicking outside
window.onclick = (event) => {
  const modal = document.getElementById("form-modal")
  if (event.target == modal) {
    hideModal()
  }
}

// Add CSS for admin specific styles
const style = document.createElement("style")
style.textContent = `
    .section.hidden { display: none; }
    .btn-sm { padding: 5px 10px; font-size: 0.8rem; margin: 2px; }
    input, select {
        width: 100%;
        padding: 10px;
        border: 2px solid #e9ecef;
        border-radius: 8px;
        font-size: 1rem;
        background: #f8f9fa;
    }
    input:focus, select:focus {
        outline: none;
        border-color: #667eea;
        background: white;
    }
    label {
        display: block;
        margin-bottom: 5px;
        font-weight: 600;
        color: #333;
    }
`
document.head.appendChild(style)
