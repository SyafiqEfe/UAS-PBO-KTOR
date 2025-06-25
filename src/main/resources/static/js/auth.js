const API_BASE = "http://localhost:8080";

// Show/Hide tabs
function showTab(tabName) {
  document.querySelectorAll(".tab-content").forEach((tab) => {
    tab.classList.remove("active");
  });

  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.remove("active");
  });

  document.getElementById(tabName + "-tab").classList.add("active");
  event.target.classList.add("active");
}

function showRegister() {
  document.getElementById("register-modal").style.display = "block";
}

function hideRegister() {
  document.getElementById("register-modal").style.display = "none";
}

function showLoading() {
  document.getElementById("loading").classList.remove("hidden");
}

function hideLoading() {
  document.getElementById("loading").classList.add("hidden");
}

function showAlert(message, type = "success") {
  const alertDiv = document.createElement("div");
  alertDiv.className = `alert alert-${type}`;
  alertDiv.innerHTML = `
        <i class="fas fa-${
          type === "success" ? "check-circle" : "exclamation-triangle"
        }"></i>
        ${message}
    `;

  document.body.insertBefore(alertDiv, document.body.firstChild);

  setTimeout(() => {
    alertDiv.remove();
  }, 5000);
}

document
  .getElementById("mahasiswa-login-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("mhs-email").value;
    const password = document.getElementById("mhs-password").value;

    showLoading();

    try {
      const response = await fetch(`${API_BASE}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identifier: email,
          password: password,
          role: "MAHASISWA",
        }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userType", "mahasiswa");
        localStorage.setItem("userData", JSON.stringify(data.user));
        window.location.href = "mahasiswa.html";
      } else {
        showAlert(data.message || "Login gagal", "error");
      }
    } catch (error) {
      showAlert("Terjadi kesalahan saat login", "error");
    } finally {
      hideLoading();
    }
  });

document
  .getElementById("dosen-login-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const nidn = document.getElementById("dosen-nidn").value;
    const password = document.getElementById("dosen-password").value;

    showLoading();

    try {
      const response = await fetch(`${API_BASE}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identifier: nidn,
          password: password,
          role: "DOSEN",
        }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userType", "dosen");
        localStorage.setItem("userData", JSON.stringify(data.user));
        window.location.href = "dosen.html";
      } else {
        showAlert(data.message || "Login gagal", "error");
      }
    } catch (error) {
      showAlert("Terjadi kesalahan saat login", "error");
    } finally {
      hideLoading();
    }
  });

document
  .getElementById("admin-login-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("admin-username").value;
    const password = document.getElementById("admin-password").value;

    showLoading();

    try {
      const response = await fetch(`${API_BASE}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identifier: username,
          password: password,
          role: "ADMIN",
        }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userType", "admin");
        localStorage.setItem("userData", JSON.stringify(data.user));
        window.location.href = "admin.html";
      } else {
        showAlert(data.message || "Login gagal", "error");
      }
    } catch (error) {
      showAlert("Terjadi kesalahan saat login", "error");
    } finally {
      hideLoading();
    }
  });

document
  .getElementById("register-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const nama = document.getElementById("reg-nama").value;
    const email = document.getElementById("reg-email").value;
    const password = document.getElementById("reg-password").value;

    showLoading();

    try {
      const response = await fetch(`${API_BASE}/register/mahasiswa`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          nama: nama,
          email: email,
          password: password,
        }),
      });

      const data = await response.json();

      if (response.ok) {
        showAlert(`Registrasi berhasil! NIM Anda: ${data.data.nim}`, "success");
        hideRegister();
        document.getElementById("register-form").reset();
      } else {
        showAlert(data.message || "Registrasi gagal", "error");
      }
    } catch (error) {
      showAlert("Terjadi kesalahan saat registrasi", "error");
    } finally {
      hideLoading();
    }
  });

window.onclick = (event) => {
  const modal = document.getElementById("register-modal");
  if (event.target == modal) {
    hideRegister();
  }
};
