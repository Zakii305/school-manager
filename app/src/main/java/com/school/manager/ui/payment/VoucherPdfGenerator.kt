package com.school.manager.ui.payment

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VoucherData(
    val voucherNo: String,
    val studentName: String,
    val studentEmail: String,
    val className: String,
    val rollNo: String,
    val description: String,
    val amount: Double,
    val dueDate: Long,
    val bankName: String = "School Bank",
    val accountNo: String = "PK00-0000-0000-0000"
)

object VoucherPdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    fun generate(context: Context, data: VoucherData): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        val canvas = page.canvas

        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(),
            Paint().apply { color = Color.rgb(245, 245, 245) })

        // Header
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 110f,
            Paint().apply { color = Color.rgb(74, 20, 140) })
        canvas.drawText("School Manager", 40f, 50f,
            Paint().apply { color = Color.WHITE; textSize = 26f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("FEE VOUCHER", 40f, 80f,
            Paint().apply { color = Color.rgb(255, 193, 7); textSize = 15f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Voucher #${data.voucherNo}", PAGE_W - 190f, 50f,
            Paint().apply { color = Color.WHITE; textSize = 11f; isAntiAlias = true })

        // Student block
        val rect = Rect(30, 140, PAGE_W - 30, 260)
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })
        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f
            isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("STUDENT", 50f, 168f, lbl)
        canvas.drawText(data.studentName, 50f, 190f, val_)
        canvas.drawText("CLASS", 50f, 220f, lbl)
        canvas.drawText(data.className, 50f, 242f, val_)
        canvas.drawText("ROLL NO", 330f, 168f, lbl)
        canvas.drawText(data.rollNo, 330f, 190f, val_)
        canvas.drawText("EMAIL", 330f, 220f, lbl)
        canvas.drawText(data.studentEmail.take(30), 330f, 242f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 11f; isAntiAlias = true })

        // Amount block
        canvas.drawRect(Rect(30, 280, PAGE_W - 30, 420),
            Paint().apply { color = Color.WHITE })
        canvas.drawRect(Rect(30, 280, PAGE_W - 30, 420),
            Paint().apply { color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        canvas.drawText("DESCRIPTION", 50f, 305f, lbl)
        canvas.drawText(data.description, 50f, 330f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f; isAntiAlias = true })
        canvas.drawText("DUE DATE", 50f, 365f, lbl)
        canvas.drawText(
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(data.dueDate)),
            50f, 388f, val_)
        canvas.drawText("AMOUNT DUE", 330f, 305f, lbl)
        canvas.drawText("Rs ${data.amount.toInt()}", 330f, 340f,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 28f
                isFakeBoldText = true; isAntiAlias = true })

        // Bank instructions
        canvas.drawText("PAYMENT INSTRUCTIONS", 40f, 460f,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 12f
                isFakeBoldText = true; isAntiAlias = true })
        val inst = Paint().apply { color = Color.rgb(60, 60, 60); textSize = 10f; isAntiAlias = true }
        listOf(
            "1. Pay at any branch of ${data.bankName}.",
            "2. Account Number: ${data.accountNo}",
            "3. Use Voucher #${data.voucherNo} as payment reference.",
            "4. Bring this voucher when making the payment.",
            "5. Keep the stamped copy for your records."
        ).forEachIndexed { i, line ->
            canvas.drawText(line, 40f, 485f + i * 18f, inst)
        }

        // Footer
        val fy = PAGE_H - 60f
        canvas.drawLine(40f, fy - 15f, PAGE_W - 40f, fy - 15f,
            Paint().apply { color = Color.rgb(200, 200, 200); strokeWidth = 1f })
        canvas.drawText(
            "For official enquiries, contact the accounts department.",
            40f, fy,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 9f; isAntiAlias = true })

        pdf.finishPage(page)
        val dir = File(context.getExternalFilesDir(null), "vouchers").apply { mkdirs() }
        val safeName = data.studentName.replace(" ", "_").ifBlank { "student" }
        val file = File(dir, "Voucher_${data.voucherNo}_${safeName}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Voucher"))
    }
}
