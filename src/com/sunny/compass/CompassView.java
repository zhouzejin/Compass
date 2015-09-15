package com.sunny.compass;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

	public CompassView(Context context) {
		super(context);
		initCompassView();
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCompassView();
	}

	public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initCompassView();
	}

	private void initCompassView() {
		setFocusable(true);
	}

}
