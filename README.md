# SITAMU — Buku Tamu Digital

Aplikasi Android lokal untuk login petugas, pencatatan kunjungan, pencarian,
rekap harian, foto tamu, dan ekspor CSV. Data disimpan di perangkat menggunakan
Room; sesi login menggunakan DataStore.

## Menjalankan

1. Buka project di Android Studio dan lakukan Gradle Sync.
2. Pilih perangkat Android dengan API 24 atau lebih baru, lalu tekan Run.
3. Untuk database baru, masuk dengan username **admin** dan password **admin123**.
   Akun dan password yang sudah ada pada database lama tetap dipertahankan.

Gunakan menu **Tambah** untuk mencatat kunjungan. Buka tamu dari dashboard atau
daftar untuk mengedit atau menghapusnya. Foto dipilih melalui pemilih dokumen
Android. Pada menu **Laporan**, tombol ekspor membuka pemilih lokasi penyimpanan;
pesan berhasil baru muncul setelah file CSV selesai ditulis.

## Build dan pengujian

Terminal memerlukan `JAVA_HOME` yang menunjuk ke JDK yang valid. Konfigurasi
daemon project menggunakan JDK 25; Android Studio menyediakan pengaturan Gradle
JDK melalui Settings > Build, Execution, Deployment > Build Tools > Gradle.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

Pengujian lokal menggunakan Robolectric API 34 dan mencakup inisialisasi database,
pelestarian data lama, login/logout, CRUD dan pencarian tamu, lifecycle form,
serta penulisan dan kegagalan ekspor CSV. Laporan tersedia di
`app/build/reports/tests/testDebugUnitTest/index.html`.

Untuk menjalankan pengujian instrumentasi pada perangkat yang terhubung:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```
