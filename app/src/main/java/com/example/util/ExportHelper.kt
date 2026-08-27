package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Assessment
import com.example.data.model.RemedialItem
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentScore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    // === WHATSAPP BROADCAST GENERATOR & CLIPBOARD ===
    fun generateWhatsAppRemedialMessage(
        schoolClass: SchoolClass,
        assessmentTitle: String,
        remedialList: List<RemedialItem>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("📢 *PEMBERITAHUAN REMEDIAL SISWA*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🏫 *Kelas:* ${schoolClass.name}\n")
        sb.append("📚 *Mata Pelajaran:* ${schoolClass.subject}\n")
        sb.append("📝 *Materi/Penilaian:* $assessmentTitle\n")
        sb.append("📅 *Tanggal:* $dateStr\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        if (remedialList.isEmpty()) {
            sb.append("Alhamdulillah, seluruh siswa telah mencapai Kriteria Ketuntasan Minimal (KKM). 🎉\n")
        } else {
            sb.append("Berikut daftar siswa yang memperoleh nilai di bawah KKM dan perlu mengikuti program remedial/perbaikan nilai:\n\n")
            remedialList.forEachIndexed { index, item ->
                val diff = item.kkm - item.score
                sb.append("${index + 1}. *${item.studentName}* (${if (item.nis.isNotBlank()) "NIS: ${item.nis}" else "Siswa"})\n")
                sb.append("   • Nilai: *${item.score}* (KKM: *${item.kkm}*) — Selisih: -${"%.1f".format(diff)}\n")
                sb.append("   • Topik Materi: ${item.topic}\n\n")
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📌 *Catatan Guru:*\n")
            sb.append("Mohon siswa yang bersangkutan segera menghubungi guru pengampu untuk jadwal dan materi tugas perbaikan. Terima kasih.\n")
            sb.append("\n_Dibuat otomatis via Aplikasi LishNilai_")
        }

        return sb.toString()
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Teks berhasil disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareToWhatsApp(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general share chooser if WhatsApp is not installed
            val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "Bagikan Daftar Remedial"
            )
            context.startActivity(shareIntent)
        }
    }

    // === EXCEL / CSV EXPORT GENERATOR ===
    fun exportToExcelCsv(
        context: Context,
        schoolClass: SchoolClass,
        students: List<Student>,
        assessments: List<Assessment>,
        scoresMap: Map<Pair<Long, Long>, StudentScore> // (assessmentId, studentId) -> score
    ): File? {
        try {
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Rekap_Nilai_${schoolClass.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            val file = File(reportsDir, fileName)

            val sb = StringBuilder()
            // Header Info
            sb.append("REKAPITULASI NILAI SISWA SEMESTER\n")
            sb.append("Kelas:;${schoolClass.name}\n")
            sb.append("Mata Pelajaran:;${schoolClass.subject}\n")
            sb.append("Tahun Ajaran:;${schoolClass.academicYear} - Semester ${schoolClass.semester}\n")
            sb.append("KKM Standar:;${schoolClass.defaultKkm}\n\n")

            // Table Header Columns
            sb.append("No;NIS;Nama Siswa;L/P;")
            assessments.forEach { a ->
                sb.append("\"${a.title} (KKM ${a.kkm})\";")
            }
            sb.append("Rata-Rata;Status KKM;Keterangan\n")

            // Student Rows
            students.forEachIndexed { index, student ->
                sb.append("${index + 1};${student.nis};\"${student.name}\";${student.gender};")
                var totalScore = 0.0
                var scoreCount = 0
                var hasBelowKkm = false

                assessments.forEach { assessment ->
                    val score = scoresMap[Pair(assessment.id, student.id)]?.finalScore ?: 0.0
                    totalScore += score
                    scoreCount++
                    if (score < assessment.kkm) {
                        hasBelowKkm = true
                    }
                    sb.append("$score;")
                }

                val avg = if (scoreCount > 0) totalScore / scoreCount else 0.0
                val status = if (avg >= schoolClass.defaultKkm && !hasBelowKkm) "TUNTAS" else "BELUM TUNTAS"
                val notes = if (hasBelowKkm) "Perlu Remedial" else "Kompeten"

                sb.append("${"%.1f".format(avg)};$status;$notes\n")
            }

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM for Excel to open Indonesian / UTF-8 accents correctly
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // === PDF REPORT GENERATION ===
    fun exportToPdfReport(
        context: Context,
        schoolClass: SchoolClass,
        students: List<Student>,
        assessments: List<Assessment>,
        scoresMap: Map<Pair<Long, Long>, StudentScore>
    ): File? {
        try {
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Laporan_Nilai_${schoolClass.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(reportsDir, fileName)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Portrait (595x842 pt)
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#004D40")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            val headerPaint = Paint().apply {
                color = Color.parseColor("#00695C")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val textPaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val redTextPaint = Paint().apply {
                color = Color.parseColor("#DC2626")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val greenTextPaint = Paint().apply {
                color = Color.parseColor("#16A34A")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 0.8f
            }

            val bgHeaderPaint = Paint().apply {
                color = Color.parseColor("#E0F2F1")
                style = Paint.Style.FILL
            }

            val bgRowAltPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }

            val width = 595f
            var y = 40f

            // Document Header
            canvas.drawText("LAPORAN REKAPITULASI NILAI SISWA DIGITAL", width / 2, y, titlePaint)
            y += 16f
            canvas.drawText("APLIKASI LISHNILAI - ADMINISTRASI AKADEMIK", width / 2, y, subtitlePaint)
            y += 18f

            // Header line divider
            canvas.drawLine(36f, y, width - 36f, y, Paint().apply {
                color = Color.parseColor("#00695C")
                strokeWidth = 2f
            })
            y += 16f

            // Info Box
            val leftMargin = 36f
            canvas.drawText("Kelas", leftMargin, y, headerPaint)
            canvas.drawText(": ${schoolClass.name}", leftMargin + 80f, y, textPaint)
            canvas.drawText("Tahun Ajaran", width / 2 + 20f, y, headerPaint)
            canvas.drawText(": ${schoolClass.academicYear} (${schoolClass.semester})", width / 2 + 90f, y, textPaint)
            y += 14f

            canvas.drawText("Mata Pelajaran", leftMargin, y, headerPaint)
            canvas.drawText(": ${schoolClass.subject}", leftMargin + 80f, y, textPaint)
            canvas.drawText("KKM Standar", width / 2 + 20f, y, headerPaint)
            canvas.drawText(": ${schoolClass.defaultKkm}", width / 2 + 90f, y, textPaint)
            y += 20f

            // Table Drawing
            val tableLeft = 36f
            val tableRight = width - 36f
            val maxAssessmentsShown = assessments.take(4) // Show up to 4 assessments comfortably on A4

            // Columns widths
            val colNoW = 24f
            val colNisW = 50f
            val colNameW = 140f
            val colScoreW = 45f
            val colAvgW = 45f
            val colStatusW = 60f

            // Table Header Rect
            val headerH = 22f
            canvas.drawRect(tableLeft, y, tableRight, y + headerH, bgHeaderPaint)
            canvas.drawRect(tableLeft, y, tableRight, y + headerH, linePaint.apply { style = Paint.Style.STROKE })

            var curX = tableLeft + 4f
            val headerTextY = y + 14f
            canvas.drawText("No", curX, headerTextY, headerPaint)
            curX += colNoW
            canvas.drawText("NIS", curX, headerTextY, headerPaint)
            curX += colNisW
            canvas.drawText("Nama Siswa", curX, headerTextY, headerPaint)
            curX += colNameW

            maxAssessmentsShown.forEachIndexed { i, a ->
                val shortTitle = if (a.title.length > 8) a.title.take(7) + ".." else a.title
                canvas.drawText(shortTitle, curX, headerTextY, headerPaint)
                curX += colScoreW
            }

            canvas.drawText("Rerata", curX, headerTextY, headerPaint)
            curX += colAvgW
            canvas.drawText("Status", curX, headerTextY, headerPaint)

            y += headerH

            // Student Rows
            val rowH = 18f
            students.take(28).forEachIndexed { index, student ->
                if (index % 2 == 1) {
                    canvas.drawRect(tableLeft, y, tableRight, y + rowH, bgRowAltPaint)
                }
                canvas.drawLine(tableLeft, y + rowH, tableRight, y + rowH, linePaint)

                var rowX = tableLeft + 4f
                val rowTextY = y + 12f

                canvas.drawText("${index + 1}", rowX, rowTextY, textPaint)
                rowX += colNoW

                canvas.drawText(student.nis.ifBlank { "-" }, rowX, rowTextY, textPaint)
                rowX += colNisW

                val displayName = if (student.name.length > 20) student.name.take(19) + ".." else student.name
                canvas.drawText(displayName, rowX, rowTextY, textPaint)
                rowX += colNameW

                var totalScore = 0.0
                var count = 0
                var hasUnderKkm = false

                maxAssessmentsShown.forEach { assessment ->
                    val sc = scoresMap[Pair(assessment.id, student.id)]?.finalScore ?: 0.0
                    totalScore += sc
                    count++
                    val isUnder = sc < assessment.kkm
                    if (isUnder) hasUnderKkm = true

                    val p = if (isUnder) redTextPaint else textPaint
                    canvas.drawText("${sc.toInt()}", rowX + 6f, rowTextY, p)
                    rowX += colScoreW
                }

                val avg = if (count > 0) totalScore / count else 0.0
                val avgUnder = avg < schoolClass.defaultKkm || hasUnderKkm
                canvas.drawText(
                    "%.1f".format(avg),
                    rowX + 4f,
                    rowTextY,
                    if (avgUnder) redTextPaint else textPaint
                )
                rowX += colAvgW

                val statusText = if (avgUnder) "Remedial" else "Tuntas"
                canvas.drawText(
                    statusText,
                    rowX + 2f,
                    rowTextY,
                    if (avgUnder) redTextPaint else greenTextPaint
                )

                y += rowH
            }

            // Outer border of table
            canvas.drawRect(tableLeft, 40f + 16f + 18f + 16f + 20f + 14f + 20f, tableRight, y, linePaint.apply { style = Paint.Style.STROKE })

            // Footer Signature section
            y = 720f
            val dateFmt = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val todayStr = dateFmt.format(Date())

            canvas.drawText("Mengetahui,", tableLeft + 20f, y, subtitlePaint)
            canvas.drawText("Kota Pengampu, $todayStr", width - 180f, y, subtitlePaint)
            y += 14f
            canvas.drawText("Kepala Sekolah", tableLeft + 20f, y, subtitlePaint)
            canvas.drawText("Guru Mata Pelajaran", width - 180f, y, subtitlePaint)

            y += 50f
            canvas.drawText("(__________________________)", tableLeft + 10f, y, headerPaint)
            canvas.drawText("(__________________________)", width - 190f, y, headerPaint)
            y += 12f
            canvas.drawText("NIP. ....................................", tableLeft + 10f, y, subtitlePaint)
            canvas.drawText("NIP. ....................................", width - 190f, y, subtitlePaint)

            document.finishPage(page)
            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Share File Helper
    fun shareGeneratedFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.lishnilai.app.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
