package birger.wifi_pauser;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WifiPauser extends Activity implements DurationPicker.OnDurationChangedListener, TimePicker.OnTimeChangedListener {
    private DurationPicker duration;
    private TimePicker endpoint;
    private Button button;
    private boolean duration_mode;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button = (Button) findViewById(R.id.button);
        button.setTransformationMethod(null);

        duration = (DurationPicker) findViewById(R.id.duration_picker);
        duration.setOnDurationChangedListener(this);

        endpoint = (TimePicker) findViewById(R.id.time_picker);
        endpoint.setIs24HourView(true);
        endpoint.setOnTimeChangedListener(this);
        Calendar now = Calendar.getInstance();
        endpoint.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        endpoint.setCurrentMinute(now.get(Calendar.MINUTE));

        // Trigger first button update:
        duration_mode = true;
        duration.setDuration(8, 0);
        onDurationChanged(null, 8, 0);
    }

    public void buttonClicked(View view) {

        printWifiOffPeriode();

        ((WifiManager) getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);

        Calendar end = Calendar.getInstance();
        if ( duration_mode ) {
            end.add(Calendar.HOUR_OF_DAY, duration.getHours());
            end.add(Calendar.MINUTE, duration.getMinutes());
        } else {
            end.set(Calendar.HOUR_OF_DAY, endpoint.getCurrentHour());
            end.set(Calendar.MINUTE, endpoint.getCurrentMinute());
            end.set(Calendar.SECOND, 0);
            Calendar now = Calendar.getInstance();
            if ( end.before(now) ){
                end.roll(Calendar.DATE, +1);
            }
        }

        Intent intent = new Intent(getBaseContext(), ReEnableWifi.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(), sender);

        finish();
    }

    private String format(int value) {
        return String.format("%02d", value);
    }

    private void printWifiOffPeriode() {
        String text = "Wifi will be turned off";
        if ( duration_mode ) {
            text += " for";
            int hours = duration.getHours();
            if ( hours > 0 ) {
                text += " " + hours + (hours > 1 ? " hours" : " hour");
            }
            int minutes = duration.getMinutes();
            if ( minutes > 0 ) {
                text += " " + minutes + (minutes > 1 ? " minutes" : " minute");
            }
        } else {
            text += " until " + format(endpoint.getCurrentHour()) + ":" + format(endpoint.getCurrentMinute());
        }
        Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
    }

    // For DurationPicker.OnDurationChangedListener interface:
    public void onDurationChanged(DurationPicker which, int num_hours, int num_minutes) {
        duration_mode = true;
        if ( num_hours == 0 && num_minutes == 0 ){
            onTimeChanged(endpoint, endpoint.getCurrentHour(), endpoint.getCurrentMinute());
        } else {
            String text = "Turn off Wifi for";
            if ( num_hours > 0 ){
                text += " " + num_hours + " h";
            }
            if ( num_minutes > 0 ){
                text += " " + format(num_minutes) + " min";
            }
            button.setText(text);
            button.invalidate();
        }
    }

    // For TimePicker.OnTimeChangedListener interface:
    public void onTimeChanged(TimePicker view, int hour, int minute) {
        duration_mode = false;
        button.setText("Turn off Wifi until " + format(hour) + ":" + format(minute));
        button.invalidate();
    }

    // For BroadcastReceiver:
    public static class ReEnableWifi extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
        }
    }
}