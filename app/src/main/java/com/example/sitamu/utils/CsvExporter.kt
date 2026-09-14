package com.example.sitamu.utils

import com.example.sitamu.data.model.GuestVisitEntity

object CsvExporter {
    fun generateCsvString(visits: List<GuestVisitEntity>): String {
        val sb = StringBuilder()
        sb.append("No;Nama;No HP;Instansi;Alamat;Keperluan;Bertemu Dengan;Tanggal;Jam\n")
        
        visits.forEachIndexed { index, visit ->
            val instansi = visit.institutionName ?: visit.institutionCategory
            sb.append("${index + 1};")
            sb.append("${escapeCsvField(visit.name)};")
            sb.append("${escapeCsvField(visit.phone)};")
            sb.append("${escapeCsvField(instansi)};")
            sb.append("${escapeCsvField(visit.address)};")
            sb.append("${escapeCsvField(visit.purpose)};")
            sb.append("${escapeCsvField(visit.personToMeet)};")
            sb.append("${visit.visitDate};")
            sb.append("${visit.visitTime}\n")
        }
        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        return if (field.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}
