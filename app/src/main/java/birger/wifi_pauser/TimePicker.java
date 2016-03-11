package birger.wifi_pauser;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.OnWheelChangedListener;

public class TimePicker extends FrameLayout implements OnWheelChangedListener {
	private WheelView hours_wheel;
	private WheelView minutes_wheel;

	private boolean wheel_scrolling = false;

	public interface OnTimeChangedListener {
		public void onTimeChanged(TimePicker which, int hours, int minutes);
	}
	
	private OnTimeChangedListener registered_listener;
	
	private static final OnTimeChangedListener DO_NOTHING = new OnTimeChangedListener() {
		public void onTimeChanged(TimePicker which, int hours, int minutes) {
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

	public TimePicker(Context context) {
		this(context, null);
	}
	
	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setOnTimeChangedListener(DO_NOTHING);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.time_picker, this, true);
		
		// Get current time
		Calendar now = Calendar.getInstance();
		
        hours_wheel = (WheelView) findViewById(R.id.time_hours);
        hours_wheel.setViewAdapter(new NumericWheelAdapter(context, 0, 23, "%02d"));
        hours_wheel.setCyclic(true);
        hours_wheel.setVisibleItems(3);
        hours_wheel.setCurrentItem(now.get(Calendar.HOUR_OF_DAY));
        hours_wheel.addChangingListener(this);
        hours_wheel.addScrollingListener(scrollListener);
        
        minutes_wheel = (WheelView) findViewById(R.id.time_minutes);
        minutes_wheel.setViewAdapter(new NumericWheelAdapter(context, 0, 59, "%02d"));
        minutes_wheel.setCyclic(true);
        minutes_wheel.setVisibleItems(3);
        minutes_wheel.setCurrentItem(now.get(Calendar.MINUTE));
        minutes_wheel.addChangingListener(this);
        minutes_wheel.addScrollingListener(scrollListener);
	}
	
	public void setOnTimeChangedListener(OnTimeChangedListener listener) {
		if ( listener != null ) {
			registered_listener = listener;
		} else {
			registered_listener = DO_NOTHING;
		}
	}

	public boolean is24HourView() {
		return true;
	}
	
	public void setIs24HourView(boolean ignored) {
		// do nothing
	}
	
	public int getCurrentHour() {
		return hours_wheel.getCurrentItem();
	}
	
	public void setCurrentHour(int hour) {
		hours_wheel.setCurrentItem(hour);
	}
	
	public int getCurrentMinute() {
		return minutes_wheel.getCurrentItem();
	}
	
	public void setCurrentMinute(int minute) {
		minutes_wheel.setCurrentItem(minute);
	}
	
	// For OnWheelChangedListener interface:
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (!wheel_scrolling) {
			registered_listener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());			
		}
	}
}
