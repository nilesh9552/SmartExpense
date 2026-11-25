package com.smartexpense.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.smartexpense.models.Expense;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExportHelper {

    public static File generateCsvFile(Context context, List<Expense> expenses) throws IOException {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String fileName = "SmartExpense_Export_" + System.currentTimeMillis() + ".csv";
        File csvFile = new File(exportDir, fileName);

        FileWriter writer = new FileWriter(csvFile);

        // CSV Header
        writer.append("ID,Expense Name,Amount,Category,Payment Method,Date,Notes,Created At\n");

        for (Expense expense : expenses) {
            writer.append(String.valueOf(expense.getId())).append(",");
            writer.append(escapeSpecialCharacters(expense.getName())).append(",");
            writer.append(String.valueOf(expense.getAmount())).append(",");
            writer.append(escapeSpecialCharacters(expense.getCategory())).append(",");
            writer.append(escapeSpecialCharacters(expense.getPaymentMethod())).append(",");
            writer.append(escapeSpecialCharacters(DateUtils.formatCsvDate(expense.getDate()))).append(",");
            writer.append(escapeSpecialCharacters(expense.getNotes() != null ? expense.getNotes() : "")).append(",");
            writer.append(escapeSpecialCharacters(DateUtils.formatCsvDate(expense.getCreatedAt()))).append("\n");
        }

        writer.flush();
        writer.close();

        return csvFile;
    }

    public static Intent createShareCsvIntent(Context context, File csvFile) {
        Uri contentUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                csvFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart Expense Export");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return Intent.createChooser(shareIntent, "Share or Save CSV");
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
