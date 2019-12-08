package com.lehigh.charlesinwald.mrrelief.ui.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.lehigh.charlesinwald.mrrelief.MainActivity;
import com.lehigh.charlesinwald.mrrelief.R;

import java.util.Calendar;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private static Calendar calendar;
    private static AlarmManager alarmManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        Button testButton = (Button) root.findViewById(R.id.button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SampleNotification();
            }
        });

        startAlarmBroadcastReceiver(this.getContext());

        TextView nextreminder = root.findViewById(R.id.nextreminder);
        int remaining = (int) (calendar.getTimeInMillis() - System.currentTimeMillis()) / 3600000;
        String text = "Next reminder in " + remaining + " hours";
        nextreminder.setText(text);
        return root;
    }

    public void SampleNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getContext(), "Mr.Relief")
                .setContentTitle("Mr.Relief")
                .setContentText("Don't forget to check in!")
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.getContext());
        notificationManager.notify(1, builder.build());

    }

    public static void startAlarmBroadcastReceiver(Context context) {
        Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(System.currentTimeMillis() + 7200000);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

}