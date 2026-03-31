# FAIt App (Food AI Tracker)

FAIt App adalah aplikasi Android berbasis AI yang digunakan untuk
mendeteksi makanan, menghitung nutrisi (kalori, protein, karbohidrat,
dan lemak), serta memberikan rekomendasi diet berbasis AI.

------------------------------------------------------------------------

## Deskripsi

Aplikasi ini menggabungkan teknologi Computer Vision dan LLM untuk membantu pengguna dalam memonitor asupan makanan harian.
Sistem dapat mendeteksi makanan melalui kamera, mengambil data nutrisi,
serta menampilkan statistik konsumsi harian.

------------------------------------------------------------------------

## Fitur Utama

-   Deteksi makanan menggunakan model TensorFlow Lite (YOLOv26)
-   Input makanan manual
-   Perhitungan kalori dan makronutrien (protein, karbohidrat, lemak)
-   Statistik konsumsi harian
-   Penyimpanan data menggunakan Room Database
-   Rekomendasi diet menggunakan Gemini API
-   Dukungan multi-bahasa (Bahasa Inggris dan Bahasa Indonesia)

------------------------------------------------------------------------

## Teknologi yang Digunakan

-   Kotlin
-   Jetpack Compose
-   MVVM Architecture
-   Clean Architecture
-   TensorFlow Lite (LiteRT)
-   Room Database
-   DataStore
-   Gemini AI API

------------------------------------------------------------------------

## Setup Project

### 1. Install Android Studio

Download dan install Android Studio dari:
https://developer.android.com/studio

Pastikan sudah terinstall: - Android SDK - Emulator atau perangkat fisik

------------------------------------------------------------------------

### 2. Clone Repository

``` bash
git clone https://github.com/USERNAME/FAIt_app.git
cd FAIt_app
```

------------------------------------------------------------------------

### 3. Buka Project

-   Buka Android Studio
-   Pilih "Open"
-   Arahkan ke folder project

------------------------------------------------------------------------

### 4. Sync Gradle

Klik "Sync Project with Gradle Files" dan tunggu hingga selesai.

------------------------------------------------------------------------

## Setup Model AI

### 1. Siapkan File Model, Metadata Model, dan Data Gizinya

- yolo26n_320_int8.tflite
- metadata.yaml
- hasil_gizi_100gram.csv


### 2. Letakkan di Folder Assets

app/src/main/assets/


------------------------------------------------------------------------

## Setup Gemini API

### 1. Dapatkan API Key

https://makersuite.google.com/

### 2. Tambahkan ke local.properties

GEMINI_API_KEY=your_api_key_here

------------------------------------------------------------------------

## Preprocess Pipeline

### 1. Deteksi Makanan (Camera)

-   User membuka fitur kamera
-   Kamera menangkap gambar makanan
-   Model TensorFlow Lite melakukan deteksi objek
-   Sistem menghasilkan nama makanan

### 2. Pemrosesan Data

-   Nama makanan dicocokkan dengan data CSV atau API
-   Sistem mengambil data nutrisi

### 3. Penambahan Makanan

-   User memilih hasil deteksi
-   Input jumlah (gram)
-   Sistem menghitung nutrisi

### 4. Update Statistik

-   Total kalori dan nutrisi diperbarui

### 5. Rekomendasi Diet

-   Data dikirim ke Gemini API
-   Sistem memberikan rekomendasi

------------------------------------------------------------------------

## Menjalankan Aplikasi

-   Jalankan emulator atau device
-   Klik Run di Android Studio

------------------------------------------------------------------------

## Struktur Project

app/ ├── data/ ├── domain/ ├── feature/ ├── di/ ├── utils/

------------------------------------------------------------------------

## Troubleshooting

-   Pastikan model ada di assets
-   Pastikan API key valid
-   Sync Gradle jika build gagal

------------------------------------------------------------------------

## Catatan

Project ini masih dapat dikembangkan lebih lanjut sesuai kebutuhan.
