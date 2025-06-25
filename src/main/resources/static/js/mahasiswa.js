const API_BASE = "http://localhost:8080"
let currentUser = null
let availableMatkul = []
let myMatkul = []

// Check authentication
function checkAuth() {
  const token = localStorage.getItem("token")
  const userType = localStorage.getItem("userType")
  const userData = localStorage.getItem("userData")

  if (!token || userType !== "mahasiswa" || !userData) {
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
  // Hide all sections
  document.querySelectorAll(".section").forEach((section) => {
    section.classList.add("hidden")
  })

  if (sectionName) {
    document.getElementById(sectionName + "-section").classList.remove("hidden")

    // Load data based on section
    switch (sectionName) {
      case "matkul":
        loadMatakuliah()
        break
      case "jadwal":
        loadJadwal()
        break
      case "nilai":
        loadNilai()
        break
      case "profil":
        loadProfil()
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

// Load mata kuliah
async function loadMatakuliah() {
  showLoading()

  try {
    // Load available mata kuliah
    const response = await apiCall("/matakuliah")
    const data = await response.json()

    if (response.ok) {
      availableMatkul = data

      // Load my mata kuliah
      const myResponse = await apiCall(`/mahasiswa/${currentUser.nim}/jadwal`)
      const myData = await myResponse.json()

      if (myResponse.ok) {
        myMatkul = myData
        renderMatakuliah()
        updateSKSInfo()
      }
    }
  } catch (error) {
    showAlert("Gagal memuat data mata kuliah", "error")
  } finally {
    hideLoading()
  }
}

// Render mata kuliah table
function renderMatakuliah() {
  const tbody = document.querySelector("#matkul-table tbody")
  tbody.innerHTML = ""

  availableMatkul.forEach((matkul) => {
    const isEnrolled = myMatkul.some((my) => my.id === matkul.id)
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>MK${matkul.id.substring(0, 6)}</td>
            <td>${matkul.nama}</td>
            <td>${matkul.sks}</td>
            <td>${matkul.dosenNama}</td>
            <td>${matkul.jamMulai}</td>
            <td>${matkul.ruangan}</td>
            <td>
                <span class="badge ${isEnrolled ? "badge-success" : "badge-secondary"}">
                    ${isEnrolled ? "Diambil" : "Tersedia"}
                </span>
            </td>
            <td>
                ${
                  isEnrolled
                    ? `<button class="btn btn-danger btn-sm" onclick="dropMatkul('${matkul.id}')">
                        <i class="fas fa-times"></i> Drop
                    </button>`
                    : `<button class="btn btn-success btn-sm" onclick="takeMatkul('${matkul.id}')">
                        <i class="fas fa-plus"></i> Ambil
                    </button>`
                }
            </td>
        `

    tbody.appendChild(row)
  })
}

// Update SKS info
function updateSKSInfo() {
  const totalSKS = myMatkul.reduce((sum, matkul) => sum + matkul.sks, 0)
  document.getElementById("total-sks").textContent = `Total SKS: ${totalSKS}/24`
}

// Take mata kuliah
async function takeMatkul(matkulId) {
  const currentSKS = myMatkul.reduce((sum, matkul) => sum + matkul.sks, 0)
  const selectedMatkul = availableMatkul.find((m) => m.id === matkulId)

  if (currentSKS + selectedMatkul.sks > 24) {
    showAlert("Total SKS tidak boleh melebihi 24", "error")
    return
  }

  if (currentSKS + selectedMatkul.sks < 19 && myMatkul.length > 0) {
    if (!confirm("Total SKS kurang dari 19. Apakah Anda yakin?")) {
      return
    }
  }

  showLoading()

  try {
    const response = await apiCall(`/mahasiswa/${currentUser.nim}/ambil-matkul`, {
      method: "POST",
      body: JSON.stringify({ matkulId: matkulId }),
    })

    const data = await response.json()

    if (response.ok) {
      showAlert("Mata kuliah berhasil diambil", "success")
      loadMatakuliah()
    } else {
      showAlert(data.message || "Gagal mengambil mata kuliah", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
}

// Drop mata kuliah
async function dropMatkul(matkulId) {
  if (!confirm("Apakah Anda yakin ingin drop mata kuliah ini?")) {
    return
  }

  showLoading()

  try {
    const response = await apiCall(`/mahasiswa/${currentUser.nim}/drop-matkul`, {
      method: "DELETE",
      body: JSON.stringify({ matkulId: matkulId }),
    })

    if (response.ok) {
      showAlert("Mata kuliah berhasil di-drop", "success")
      loadMatakuliah()
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal drop mata kuliah", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
}

// Load jadwal
async function loadJadwal() {
  showLoading()

  try {
    const response = await apiCall(`/mahasiswa/${currentUser.nim}/jadwal`)
    const data = await response.json()

    if (response.ok) {
      renderJadwal(data)
    }
  } catch (error) {
    showAlert("Gagal memuat jadwal", "error")
  } finally {
    hideLoading()
  }
}

// Render jadwal table
function renderJadwal(jadwal) {
  const tbody = document.querySelector("#jadwal-table tbody")
  tbody.innerHTML = ""

  // Group by day (simplified - assuming all classes are on different days)
  const days = ["Senin", "Selasa", "Rabu", "Kamis", "Jumat"]

  jadwal.forEach((matkul, index) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${days[index % 5]}</td>
            <td>${matkul.jamMulai}</td>
            <td>${matkul.nama}</td>
            <td>${matkul.dosenNama}</td>
            <td>${matkul.ruangan}</td>
            <td>${matkul.sks}</td>
        `

    tbody.appendChild(row)
  })
}

// Load nilai
async function loadNilai() {
  showLoading()

  try {
    const response = await apiCall(`/mahasiswa/${currentUser.nim}/nilai`)
    const data = await response.json()

    if (response.ok) {
      renderNilai(data)
    }
  } catch (error) {
    showAlert("Gagal memuat nilai", "error")
  } finally {
    hideLoading()
  }
}

// Render nilai table
function renderNilai(nilai) {
  const tbody = document.querySelector("#nilai-table tbody")
  tbody.innerHTML = ""

  nilai.forEach((item) => {
    const row = document.createElement("tr")

    row.innerHTML = `
            <td>${item.matkulNama}</td>
            <td>${item.dosenNama}</td>
            <td>${item.sks}</td>
            <td>
                <span class="badge badge-${getPresensiColor(item.presensi)}">
                    ${item.presensi || "Belum ada"}
                </span>
            </td>
            <td>
                <span class="badge badge-${getNilaiColor(item.nilai)}">
                    ${item.nilai || "Belum ada"}
                </span>
            </td>
            <td>
                <span class="badge ${item.nilai ? "badge-success" : "badge-warning"}">
                    ${item.nilai ? "Selesai" : "Berlangsung"}
                </span>
            </td>
        `

    tbody.appendChild(row)
  })
}

// Get presensi color
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

// Get nilai color
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

// Load profil
function loadProfil() {
  document.getElementById("profil-nama").value = currentUser.nama
  document.getElementById("profil-email").value = currentUser.email
}

// Update profil
document.getElementById("profil-form").addEventListener("submit", async (e) => {
  e.preventDefault()

  const nama = document.getElementById("profil-nama").value
  const email = document.getElementById("profil-email").value
  const password = document.getElementById("profil-password").value

  showLoading()

  try {
    const updateData = { nama, email }
    if (password) {
      updateData.password = password
    }

    const response = await apiCall(`/mahasiswa/${currentUser.nim}`, {
      method: "PUT",
      body: JSON.stringify(updateData),
    })

    if (response.ok) {
      const updatedUser = await response.json()
      currentUser = updatedUser
      localStorage.setItem("userData", JSON.stringify(updatedUser))

      document.getElementById("student-name").textContent = updatedUser.nama
      showAlert("Profil berhasil diupdate", "success")
      showSection("")
    } else {
      const data = await response.json()
      showAlert(data.message || "Gagal update profil", "error")
    }
  } catch (error) {
    showAlert("Terjadi kesalahan", "error")
  } finally {
    hideLoading()
  }
})

// Initialize page
document.addEventListener("DOMContentLoaded", () => {
  if (checkAuth()) {
    document.getElementById("student-name").textContent = currentUser.nama
    document.getElementById("student-nim").textContent = `NIM: ${currentUser.nim}`
  }
})

// Add CSS for badges
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
    .btn-sm { padding: 5px 10px; font-size: 0.8rem; }
    .section.hidden { display: none; }
    select, input, label {
        width: 100%;
        padding: 10px;
        border: 2px solid #e9ecef;
        border-radius: 8px;
        font-size: 1rem;
        background: #f8f9fa;
        margin-bottom: 10px;
    }
    select:focus, input:focus {
        outline: none;
        border-color: #667eea;
        background: white;
    }
    label {
        display: block;
        font-weight: 600;
        color: #333;
        background: transparent;
        border: none;
        padding: 0;
        margin-bottom: 5px;
    }
`
document.head.appendChild(style)
