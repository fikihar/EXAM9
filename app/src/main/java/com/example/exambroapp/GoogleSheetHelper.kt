package com.example.exambroapp

import android.util.Log
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleSheetHelper {
    private val client = OkHttpClient()

    fun getMapelByKodeExam(spreadsheetId: String, kodeExam: String): List<Mapel> {
        val url = "https://docs.google.com/spreadsheets/d/$spreadsheetId/gviz/tq?tqx=out:json"
        val mapelList = mutableListOf<Mapel>()

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonResponse = response.body?.string()

            if (jsonResponse.isNullOrEmpty()) return mapelList

            val cleanedJson = jsonResponse
                .substringAfter("google.visualization.Query.setResponse(", "")
                .substringBeforeLast(")")

            val jsonObject = JsonParser.parseString(cleanedJson).asJsonObject
            val rows = jsonObject["table"]?.asJsonObject?.get("rows")?.asJsonArray

            rows?.forEach { row ->
                val cells = row.asJsonObject["c"]?.asJsonArray
                if (cells != null && cells.size() >= 9) {
                    // Ambil data dari kolom spreadsheet
                    val mapel = cells[0]?.asJsonObject?.get("v")?.asString.orEmpty() // Kolom MAPEL
                    val kelas = cells[1]?.asJsonObject?.get("v")?.asString.orEmpty() // Kolom KELAS
                    val kode = cells[3]?.asJsonObject?.get("v")?.asString.orEmpty()  // Kolom KODE EXAM
                    val link = cells[6]?.asJsonObject?.get("v")?.asString.orEmpty() // Kolom LINK

                    // Cocokkan KODE EXAM dengan input
                    if (kode == kodeExam) {
                        mapelList.add(Mapel(name = mapel, kelas = kelas, link = link))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetHelper", "Error: ${e.message}")
        }

        return mapelList
    }
}
