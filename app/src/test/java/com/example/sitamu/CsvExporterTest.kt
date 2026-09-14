package com.example.sitamu

import com.example.sitamu.utils.CsvExporter
import org.junit.Assert.*
import org.junit.Test

class CsvExporterTest {
    @Test
    fun preservesSeparatorsQuotesAndNewlinesInGuestData() {
        val csv = CsvExporter.generateCsvString(listOf(sampleVisit().copy(
            name = "Siti; Aminah", address = "Jalan \"Mawar\"\nMakassar"
        )))
        assertTrue(csv.contains("1;\"Siti; Aminah\";08123456789;Pemerintah;\"Jalan \"\"Mawar\"\"\nMakassar\";"))
        assertTrue(csv.endsWith(";14-09-2026;10:00\n"))
    }

    @Test
    fun emptyExportContainsOnlyHeader() {
        assertEquals("No;Nama;No HP;Instansi;Alamat;Keperluan;Bertemu Dengan;Tanggal;Jam\n",
            CsvExporter.generateCsvString(emptyList()))
    }
}
