const API_BASE = "http://localhost:8080"
let currentUser = null
let myMatkul = []

// Check authentication
function checkAuth() {
  const token = localStorage.getItem("token")
  const userType = localStorage.getItem("userType")
  const userData = localStorage.getItem("userData")

  if (!token || userType !== "dosen" || !userData) {
    window.location.href = "index.html"
    return false
  }

  currentUser = JSON.parse(userData)
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
      case "matkul":
        loadMyMatkul()
        break
      case "mahasiswa":
        loadMatkulForFilter()
        break
      case "presensi":
        loadMatkulForPresensi()
        break
      case "nilai":
        loadMatkulForNilai()
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

// Load my mata kuliah
async function loadMyMatkul() {
  showLoading()

  try {
    const response = await apiCall(`/dosen/${currentUser.nidn}/matkul`)
    const data = await response.json()

    if (response.ok) {
      myMatkul = data
      renderMyMatkul()
    }
  } catch (error) {
    showAlert("Gagal memuat mata kuliah", "error")
  } finally {
    hideLoading()
  }
}

// Render my mata kuliah
function renderMyMatkul() {
  const tbody = document.querySelector("#matkul-table tbody")
  tbody.innerHTML = ""

  myMatkul.forEach((matkul) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${matkul.nama}</td>
            <td>${matkul.sks}</td>
            <td>${matkul.jamMulai}</td>
            <td>${matkul.ruangan}</td>
            <td>${matkul.jumlahMahasiswa || 0}</td>
            <td>
                <button class="btn btn-info btn-sm" onclick="viewMahasiswa('${matkul.id}')">
                    <i class="fas fa-users"></i> Lihat Mahasiswa
                </button>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Load mata kuliah for filter
async function loadMatkulForFilter() {
  const select = document.getElementById("filter-matkul")
  select.innerHTML = '<option value="">-- Pilih Mata Kuliah --</option>'

  myMatkul.forEach((matkul) => {
    const option = document.createElement("option")
    option.value = matkul.id
    option.textContent = matkul.nama
    select.appendChild(option)
  })
}

// Load mahasiswa by mata kuliah
async function loadMahasiswaByMatkul() {
  const matkulId = document.getElementById("filter-matkul").value

  if (!matkulId) {
    document.getElementById("mahasiswa-list").classList.add("hidden")
    return
  }

  showLoading()

  try {
    const response = await apiCall(`/dosen/${currentUser.nidn}/matkul/${matkulId}/mahasiswa`)
    const data = await response.json()

    if (response.ok) {
      renderMahasiswaList(data, matkulId)
      document.getElementById("mahasiswa-list").classList.remove("hidden")
    }
  } catch (error) {
    showAlert("Gagal memuat data mahasiswa", "error")
  } finally {
    hideLoading()
  }
}

// Render mahasiswa list
function renderMahasiswaList(mahasiswa, matkulId) {
  const tbody = document.querySelector("#mahasiswa-table tbody")
  tbody.innerHTML = ""

  mahasiswa.forEach((mhs) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${mhs.nim}</td>
            <td>${mhs.nama}</td>
            <td>${mhs.email}</td>
            <td>
                <span class="badge badge-${getPresensiColor(mhs.presensi)}">
                    ${mhs.presensi || "Belum ada"}
                </span>
            </td>
            <td>
                <span class="badge badge-${getNilaiColor(mhs.nilai)}">
                    ${mhs.nilai || "Belum ada"}
                </span>
            </td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="inputPresensi('${mhs.id}', '${matkulId}')">
                    <i class="fas fa-check"></i> Presensi
                </button>
                <button class="btn btn-success btn-sm" onclick="inputNilai('${mhs.id}', '${matkulId}')">
                    <i class="fas fa-star"></i> Nilai
                </button>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Load mata kuliah for presensi
async function loadMatkulForPresensi() {
  const select = document.getElementById("presensi-matkul")
  select.innerHTML = '<option value="">-- Pilih Mata Kuliah --</option>'

  myMatkul.forEach((matkul) => {
    const option = document.createElement("option")
    option.value = matkul.id
    option.textContent = matkul.nama
    select.appendChild(option)
  })

  // Load mahasiswa when mata kuliah changes
  select.addEventListener("change", async () => {
    const matkulId = select.value
    const mahasiswaSelect = document.getElementById("presensi-mahasiswa")
    mahasiswaSelect.innerHTML = '<option value="">-- Pilih Mahasiswa --</option>'

    if (matkulId) {
      try {
        const response = await apiCall(`/dosen/${currentUser.nidn}/matkul/${matkulId}/mahasiswa`)
        const data = await response.json()

        if (response.ok) {
          data.forEach((mhs) => {
            const option = document.createElement("option")
            option.value = mhs.id
            option.textContent = `${mhs.nim} - ${mhs.nama}`
            mahasiswaSelect.appendChild(option)
          })
        }
      } catch (error) {
        showAlert("Gagal memuat data mahasiswa", "error")
      }
    }
  })
}

// Load mata kuliah for nilai
async function loadMatkulForNilai() {
  const select = document.getElementById("nilai-matkul")
  select.innerHTML = '<option value="">-- Pilih Mata Kuliah --</option>'

  myMatkul.forEach((matkul) => {
    const option = document.createElement("option")
    option.value = matkul.id
    option.textContent = matkul.nama
    select.appendChild(option)
  })

  // Load mahasiswa when mata kuliah changes
  select.addEventListener("change", async () => {
    const matkulId = select.value
    const mahasiswaSelect = document.getElementById("nilai-mahasiswa")
    mahasiswaSelect.innerHTML = '<option value="">-- Pilih Mahasiswa --</option>'

    if (matkulId) {
      try {
        const response = await apiCall(`/dosen/${currentUser.nidn}/matkul/${matkulId}/mahasiswa`)
        const data = await response.json()

        if (response.ok) {
          data.forEach((mhs) => {
            const option = document.createElement("option")
            option.value = mhs.id
            option.textContent = `${mhs.nim} - ${mhs.nama}`
            mahasiswaSelect.appendChild(option)
          })
        }
      } catch (error) {
        showAlert("Gagal memuat data mahasiswa", "error")
      }
    }
  })
}

// Submit presensi
document.getElementById("presensi-form").addEventListener("submit", async (e) => {
  e.preventDefault()

  const matkulId = document.getElementById("presensi-matkul").value
  const mahasiswaId = document.getElementById("presensi-mahasiswa").value
  const status = document.getElementById("presensi-status").value

  showLoading()

  try {
    const response = await apiCall(`/dosen/${currentUser.nidn}/matkul/${matkulId}/presensi`, {
      method: "POST",
      body: JSON.stringify({
        mahasiswaId: mahasiswaId,
        presensi: status,
      }),
    })

    if (response.ok) {
      showAlert("Presensi berhasil disimpan", "success")
      document.getElementById("presensi-form").reset()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal menyimpan presensi", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
})

// Submit nilai
document.getElementById("nilai-form").addEventListener("submit", async (e) => {
  e.preventDefault()

  const matkulId = document.getElementById("nilai-matkul").value
  const mahasiswaId = document.getElementById("nilai-mahasiswa").value
  const nilai = document.getElementById("nilai-grade").value

  showLoading()

  try {
    const response = await apiCall(`/dosen/${currentUser.nidn}/matkul/${matkulId}/nilai`, {
      method: "POST",
      body: JSON.stringify({
        mahasiswaId: mahasiswaId,
        nilai: nilai,
      }),
    })

    if (response.ok) {
      showAlert("Nilai berhasil disimpan", "success")
      document.getElementById("nilai-form").reset()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal menyimpan nilai", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
})

// Helper functions
function getPresensiColor(presensi) {
  switch (presensi) {
    case "HADIR":
      return "success"
    case "SAKIT":
      return "warning"
    case "IZIN":
      return "info"
    case "ALPHA":
      return "danger"
    default:
      return "secondary"
  }
}

function getNilaiColor(nilai) {
  switch (nilai) {
    case "A":
      return "success"
    case "B":
      return "info"
    case "C":
      return "warning"
    case "D":
      return "danger"
    case "E":
      return "danger"
    default:
      return "secondary"
  }
}

// Initialize page
document.addEventListener("DOMContentLoaded", () => {
  if (checkAuth()) {
    document.getElementById("dosen-name").textContent = currentUser.nama
    document.getElementById("dosen-nidn").textContent = `NIDN: ${currentUser.nidn}`
    loadMyMatkul()
  }
})

// Add CSS for badges and buttons
const style = document.createElement("style")
style.textContent = `
    .badge {
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 0.8rem;
        font-weight: 600;
    }
    .badge-success { background: #28a745; color: white; }
    .badge-danger { background: #dc3545; color: white; }
    .badge-warning { background: #ffc107; color: #333; }
    .badge-info { background: #17a2b8; color: white; }
    .badge-secondary { background: #6c757d; color: white; }
    .btn-sm { padding: 5px 10px; font-size: 0.8rem; margin: 2px; }
    .section.hidden { display: none; }
    select {
        width: 100%;
        padding: 10px;
        border: 2px solid #e9ecef;
        border-radius: 8px;
        font-size: 1rem;
        background: #f8f9fa;
    }
    select:focus {
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
