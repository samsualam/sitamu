package com.example.sitamu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label))

    @Test
    fun loginCreateEditSearchReportDeleteAndLogout() {
        awaitText("Masuk Admin")
        field("Username").performTextInput("admin")
        field("Password").performTextInput("salah")
        compose.onNodeWithText("Masuk", useUnmergedTree = true).performClick()
        awaitText("Username atau password tidak sesuai.")
        field("Password").performTextReplacement("admin123")
        compose.onNodeWithText("Masuk", useUnmergedTree = true).performClick()
        awaitText("Dashboard Beranda")

        compose.onNodeWithContentDescription("Tambah").performClick()
        awaitText("Tambah Tamu Baru")
        compose.onNodeWithText("Simpan Data Tamu").performScrollTo().performClick()
        awaitText("Nama lengkap wajib diisi.")
        field("Nama Lengkap *").performScrollTo().performTextInput("Tamu Uji")
        field("Nomor HP *").performScrollTo().performTextInput("08123456789")
        compose.onNodeWithText("Asal Instansi *").performScrollTo().performClick()
        compose.onNodeWithText("Pemerintah").performClick()
        field("Alamat *").performScrollTo().performTextInput("Makassar")
        field("Keperluan *").performScrollTo().performTextInput("Konsultasi")
        field("Bertemu Dengan *").performScrollTo().performTextInput("Petugas")

        // Recreating the Activity must not clear the form's ViewModel state.
        compose.activityRule.scenario.recreate()
        field("Nama Lengkap *").assertTextContains("Tamu Uji")
        compose.onNodeWithText("Simpan Data Tamu").performScrollTo().performClick()
        awaitText("Dashboard Beranda")
        awaitText("Tamu Uji")
        compose.onNodeWithText("Tamu Uji").performScrollTo().performClick()
        awaitText("Detail Kunjungan")
        compose.onNodeWithText("Edit").performScrollTo().performClick()
        awaitText("Edit Data Tamu")
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasSetTextAction() and hasText("Tamu Uji")).fetchSemanticsNodes().isNotEmpty()
        }
        field("Nama Lengkap *").performScrollTo().performTextReplacement("Tamu Diubah")
        compose.onNodeWithText("Simpan Data Tamu").performScrollTo().performClick()
        awaitText("Detail Kunjungan")
        awaitText("Tamu Diubah")
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithContentDescription("Tamu").performClick()
        awaitText("Daftar Kunjungan Tamu")
        field("Cari tamu (Nama, Instansi, HP, Keperluan)...").performTextInput("tidak-ada")
        awaitText("Belum ada data kunjungan.")
        field("Cari tamu (Nama, Instansi, HP, Keperluan)...").performTextReplacement("Diubah")
        awaitText("Tamu Diubah")
        compose.onNodeWithContentDescription("Laporan").performClick()
        awaitText("Laporan Rekap Kunjungan")
        awaitText("1 Tamu")
        compose.onNodeWithText("Export Semua Data ke CSV").assertIsEnabled()
        compose.onNodeWithContentDescription("Tamu").performClick()
        awaitText("Tamu Diubah")
        compose.onNodeWithText("Tamu Diubah").performClick()
        compose.onNodeWithText("Hapus").performScrollTo().performClick()
        awaitText("Hapus Data?")
        compose.onAllNodesWithText("Hapus").onLast().performClick()
        awaitText("Daftar Kunjungan Tamu")
        awaitText("Belum ada data kunjungan.")
        compose.onNodeWithContentDescription("Pengaturan").performClick()
        compose.onNodeWithText("Keluar dari Akun (Logout)").performClick()
        awaitText("Masuk Admin")
    }
}
