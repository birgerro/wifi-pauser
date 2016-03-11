package birger.wifi_pauser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;

public class DurationPicker extends FrameLayout implements OnWheelChangedListener {
    private WheelView hours_wheel;
    private WheelView minutes_wheel;

    private boolean wheel_scrolling = false;

    private int minutes_interval = 5;

    public interface OnDurationChangedListener {
        public void onDurationChanged(DurationPicker which, int num_hours, int num_minutes);
    }

    private OnDurationChangedListener registered_listener;

    private static final OnDurationChangedListener DO_NOTHING = new OnDurationChangedListener() {
        public void onDurationChanged(DurationPicker which, int num_hours, int num_minutes) {
            // Do nothing
        }
    };

    private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        //@Override
        public void onScrollingStarted(WheelView wheel) {
            wheel_scrolling = true;
        }
        //@Override
        public void onScrollingFinished(WheelView wheel) {
            wheel_scrolling = false;
            onChanged(null, 0, 0);
        }
    };

    public DurationPicker(Context context) {
        this(context, null);
    }

    public DurationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnDurationChangedListener(DO_NOTHING);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.duration_picker, this, true);

        hours_wheel = (WheelView) findViewById(R.id.duration_hours);
        hours_wheel.setViewAdapter(new NumericWheelAdapter(context, 0, 10, "%02d"));
        hours_wheel.setVisibleItems(3);
        hours_wheel.addChangingListener(this);
        hours_wheel.addScrollingListener(scrollListener);
        
        minutes_wheel = (WheelView) findViewById(R.id.duration_minutes);
        minutes_wheel.setViewAdapter(new NumericWheelAdapter(context, 0, 59, "%02d"));
        minutes_wheel.setVisibleItems(3);
        minutes_wheel.addChangingListener(this);
        minutes_wheel.addScrollingListener(scrollListener);
        
        setMinutesInterval(5);
    }

    public void setOnDurationChangedListener(OnDurationChangedListener listener) {
        if ( listener != null ) {
            registered_listener = listener;
        } else {
            registered_listener = DO_NOTHING;
        }
    }

    public void setDuration(int hours, int minutes) {
        setHours(hours);
        setMinutes(minutes);
    }

    public int getHours() {
        return hours_wheel.getCurrentItem();
    }

    public void setHours(int hours) {
        hours_wheel.setCurrentItem(hours);
    }

    public int getMinutes() {
        return minutes_wheel.getCurrentItem() * minutes_interval;
    }

    public void setMinutes(int minutes) {
        minutes_wheel.setCurrentItem(Math.round(((float) minutes)/minutes_interval));
    }

    public void setMinutesInterval(int interval) {
        double current_minutes = getMinutes();
        minutes_wheel.setCurrentItem(0);
        minutes_interval = interval;

        minutes_wheel.setViewAdapter(new MinutesAdapter(getContext(), interval));

        minutes_wheel.setCurrentItem((int) Math.round(current_minutes/minutes_interval));
    }

    private class MinutesAdapter extends NumericWheelAdapter {
        // Items step value
        private int minutes_interval;

        public MinutesAdapter(Context context, int interval) {
            super(context, 0, (60/interval)-1);
            minutes_interval = interval;
        }

        @Override
        public CharSequence getItemText(int index) {
            if (index >= 0 && index < getItemsCount()) {
                int value = index * minutes_interval;
                //return Integer.toString(value);
                return String.format("%02d", value);
            }
            return null;
        }
    }

    // For OnWheelChangedListener interface:
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (!wheel_scrolling) {
            registered_listener.onDurationChanged(this, getHours(), getMinutes());
        }
    }
}
