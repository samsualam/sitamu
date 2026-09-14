package com.example.sitamu

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.presentation.report.ReportViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReportExportTest {
    private fun awaitCondition(condition: () -> Boolean) {
        val deadline = System.nanoTime() + 10_000_000_000L
        while (!condition() && System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        assertTrue("Operasi tidak selesai dalam 10 detik", condition())
    }

    @Test
    fun exportWritesFileAndReportsCancellationOrFailureHonestly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repository = GuestRepository(db.guestVisitDao())
        runBlocking { repository.insertGuestVisit(sampleVisit()) }
        val model = ReportViewModel(repository)
        val store = ViewModelStore().apply { put("report", model) }
        val subscription = CoroutineScope(Dispatchers.Unconfined).launch { model.allVisits.collect() }
        try {
            awaitCondition { model.allVisits.value.size == 1 }
            val output = File(context.cacheDir, "kunjungan.csv")
            model.prepareExport()
            assertNull(model.exportMessage)
            model.exportTo(context.contentResolver, Uri.fromFile(output))
            awaitCondition { !model.isExporting }
            assertFalse(model.exportFailed)
            assertTrue(output.readText().contains("1;Siti;08123456789;Pemerintah;Makassar;Konsultasi;Petugas;"))
            assertEquals("Berhasil menyimpan 1 data ke file CSV.", model.exportMessage)

            model.prepareExport()
            model.exportTo(context.contentResolver, null)
            assertFalse(model.isExporting)
            assertNull(model.exportMessage)

            model.prepareExport()
            model.exportTo(context.contentResolver, Uri.fromFile(context.cacheDir))
            awaitCondition { !model.isExporting }
            assertTrue(model.exportFailed)
            assertEquals("File CSV gagal disimpan. Silakan pilih lokasi lain.", model.exportMessage)
        } finally {
            subscription.cancel()
            store.clear()
            shadowOf(Looper.getMainLooper()).idle()
            db.close()
        }
    }
}
