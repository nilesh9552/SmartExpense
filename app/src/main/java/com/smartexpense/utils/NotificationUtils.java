package com.smartexpense.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.smartexpense.R;
import com.smartexpense.activities.MainActivity;

public class NotificationUtils {

    public static final String CHANNEL_ID_BUDGET = "channel_smart_expense_budget";
    private static final int NOTIFICATION_ID_BUDGET = 1001;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.budget_warning_channel);
            String description = context.getString(R.string.budget_warning_channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_BUDGET, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void checkAndNotifyBudget(Context context, double currentSpent, double monthlyBudget) {
        PreferenceManager prefManager = new PreferenceManager(context);
        if (!prefManager.areNotificationsEnabled() || monthlyBudget <= 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String title;
        String content;

        if (currentSpent >= monthlyBudget) {
            title = context.getString(R.string.budget_exceeded_warning);
            content = "You have spent " + CurrencyHelper.formatAmount(context, currentSpent) +
                    ", which exceeds your monthly budget of " + CurrencyHelper.formatAmount(context, monthlyBudget) + "!";
            sendNotification(context, title, content);
        } else if (currentSpent >= (monthlyBudget * 0.8)) {
            int pct = (int) Math.round((currentSpent / monthlyBudget) * 100);
            title = context.getString(R.string.budget_approaching_warning);
            content = "Warning: You have used " + pct + "% of your monthly budget (" +
                    CurrencyHelper.formatAmount(context, currentSpent) + " of " + CurrencyHelper.formatAmount(context, monthlyBudget) + ").";
            sendNotification(context, title, content);
        }
    }

    private static void sendNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_BUDGET, builder.build());
        } catch (SecurityException ignored) {
            // Handled when permission revoked
        }
    }
}
