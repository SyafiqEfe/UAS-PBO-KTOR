# Sistem Akademik KTOR

Sistem akademik berbasis web menggunakan KTOR (Kotlin), PostgreSQL, dan Exposed ORM dengan frontend HTML/CSS/JavaScript.

## Fitur

### Backend (KTOR)
- ✅ Authentication & Authorization
- ✅ REST API endpoints
- ✅ Database integration (PostgreSQL + Exposed ORM)
- ✅ CRUD operations untuk semua entitas
- ✅ Validasi data dan error handling
- ✅ Seeding data awal

### Frontend (HTML/CSS/JS)
- ✅ Multi-role login (Mahasiswa, Dosen, Admin)
- ✅ Dashboard responsif untuk setiap role
- ✅ CRUD interface untuk admin
- ✅ KRS (Kartu Rencana Studi) untuk mahasiswa
- ✅ Input nilai dan presensi untuk dosen
- ✅ Laporan dan statistik

## Teknologi

- **Backend**: Kotlin + KTOR Framework
- **Database**: PostgreSQL + Exposed ORM
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Authentication**: Bearer Token
- **Build Tool**: Gradle

## Setup & Installation

### Prerequisites
- JDK 17+
- PostgreSQL 12+
- Gradle 7+

### Database Setup
1. Install PostgreSQL
2. Create database: `academic_db`
3. Update connection string di `Application.kt` jika perlu

### Running the Application

#### Development
\`\`\`bash
# Clone repository
git clone <repository-url>
cd academic-system-ktor

# Run with Gradle
./gradlew run
\`\`\`

#### Production (Docker)
\`\`\`bash
# Build and run with Docker Compose
docker-compose up --build
\`\`\`

### Default Credentials

#### Admin
- Username: `admin`
- Password: `admin123`

#### Dosen (contoh)
- NIDN: `100001`
- Password: `fikri123`

#### Mahasiswa
- Registrasi melalui form registrasi di halaman login

## API Endpoints

### Authentication
- `POST /login` - Login untuk semua role
- `POST /register` - Registrasi mahasiswa baru

### Mahasiswa
- `GET /matakuliah` - Daftar mata kuliah
- `POST /mahasiswa/{nim}/ambil-matkul` - Ambil mata kuliah
- `DELETE /mahasiswa/{nim}/drop-matkul` - Drop mata kuliah
- `GET /mahasiswa/{nim}/jadwal` - Jadwal kuliah
- `GET /mahasiswa/{nim}/nilai` - Nilai dan presensi
- `PUT /mahasiswa/{nim}` - Update profil

### Dosen
- `GET /dosen/{nidn}/matkul` - Mata kuliah yang diampu
- `GET /dosen/{nidn}/matkul/{id}/mahasiswa` - Mahasiswa per mata kuliah
- `POST /dosen/{nidn}/matkul/{id}/presensi` - Input presensi
- `POST /dosen/{nidn}/matkul/{id}/nilai` - Input nilai

### Admin
- `GET /admin/mahasiswa` - CRUD mahasiswa
- `GET /admin/dosen` - CRUD dosen
- `GET /admin/matakuliah` - CRUD mata kuliah

## Database Schema

### Tables
- `persons` - Data dasar person (inheritance)
- `mahasiswa` - Data mahasiswa
- `dosen` - Data dosen
- `admin` - Data admin
- `matakuliah` - Data mata kuliah
- `mahasiswa_matakuliah` - Junction table (KRS)
- `presensi` - Data presensi
- `nilai` - Data nilai

## Project Structure

\`\`\`
src/
├── main/
│   ├── kotlin/com/academic/
│   │   ├── database/         # Database tables & models
│   │   ├── models/          # Data classes
│   │   ├── services/        # Business logic
│   │   ├── routes/          # API routes
│   │   ├── plugins/         # KTOR plugins
│   │   └── Application.kt   # Main application
│   └── resources/
│       ├── static/          # Frontend files
│       │   ├── css/
│       │   ├── js/
│       │   └── *.html
│       └── logback.xml
└── test/                    # Test files
\`\`\`

## Features Checklist

- [x] Login multi-role (Mahasiswa, Dosen, Admin)
- [x] Registrasi mahasiswa baru
- [x] Dashboard untuk setiap role
- [x] CRUD Mahasiswa (Admin)
- [x] CRUD Dosen (Admin)
- [x] CRUD Mata Kuliah (Admin)
- [x] KRS - Ambil/Drop mata kuliah (Mahasiswa)
- [x] Lihat jadwal kuliah (Mahasiswa)
- [x] Input presensi (Dosen)
- [x] Input nilai (Dosen)
- [x] Lihat nilai dan presensi (Mahasiswa)
- [x] Laporan dan statistik (Admin)
- [x] Validasi SKS (min 19, max 24)
- [x] Interface responsif
- [x] Error handling
- [x] Data seeding

## License

MIT License
