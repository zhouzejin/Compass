package com.sunny.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class CompassView extends View {
	
	private float bearing;
	
	private Paint markerPaint;
	private Paint textPaint;
	private Paint circlePaint;
	
	private String northString;
	private String eastString;
	private String southString;
	private String westString;
	
	private int textHeight;

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

	protected void initCompassView() {
		setFocusable(true);
		
		Resources r = this.getResources();
		
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(r.getColor(R.color.background_color));
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		northString = r.getString(R.string.cardinal_north);
		eastString = r.getString(R.string.cardinal_east);
		southString = r.getString(R.string.cardinal_south);
		westString = r.getString(R.string.cardinal_west);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.text_color));
		
		textHeight = (int) textPaint.measureText("yY");
		
		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		markerPaint.setColor(r.getColor(R.color.marker_color));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 罗盘是一个填充尽可能多的空间的园，通过设置最短的边界、高度或者宽度来设置测量的尺寸
		int measureWidth = measure(widthMeasureSpec);
		int measureHeight = measure(heightMeasureSpec);
		
		int d = Math.min(measureWidth, measureHeight);
		
		setMeasuredDimension(d, d);
	}

	private int measure(int measureSpec) {
		int result = 0;
		
		// 对测量说明进行编码
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// 如果没有指定边界，则返回默认大小200
			result = 200;
		} else {
			// 因为希望尽量填充可用空间，所以返回整个可用的边界
			result = specSize;
		}
		
		return result;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// 找到控件中心，并将最小边的长度作为罗盘的半径存储起来
		int mMeasuredWidth = getMeasuredWidth();
		int mMeasuredHeight = getMeasuredHeight();
		
		int px = mMeasuredWidth / 2;
		int py = mMeasuredHeight / 2;
		
		int radius = Math.min(px, py);
		
		// 绘制背景
		canvas.drawCircle(px, py, radius, circlePaint);
		
		// 旋转视图，使View上方面对当前的方向
		canvas.save();
		canvas.rotate(-bearing, px, py);
		
		int textWidth = (int) textPaint.measureText("W");
		int cardinalX = px - textWidth / 2;
		int cardinalY = px - radius + textHeight;
		
		// 没15度绘制一个标记，没45度绘制一个文本
		for (int i = 0; i < 24; i++) {
			// 绘制一个标记
			canvas.drawLine(px, py-radius, px, py-radius+10, markerPaint);
			
			canvas.save();
			canvas.translate(0, textHeight);
			
			// 绘制基本方位
			if (i % 6 == 0) {
				String dirString = "";
				switch (i) {
				case 0: {
					dirString = northString;
					int arrowY = 2*textHeight;
					canvas.drawLine(px, arrowY, px-5, 3*textHeight, markerPaint);
					canvas.drawLine(px, arrowY, px+5, 3*textHeight, markerPaint);
					break;
				}
				case 6:
					dirString = eastString;
					break;
				case 12:
					dirString = southString;
					break;
				case 18:
					dirString = westString;
					break;
				}
				canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
			} else if (i % 3 == 0) {
				// 每45度绘制文本
				String angle = String.valueOf(i*15);
				float angleTextWidth = textPaint.measureText(angle);
				
				int angleTextX = (int) (px - angleTextWidth/2);
				int angleTextY = py - radius + textHeight;
				canvas.drawText(angle, angleTextX, angleTextY, textPaint);
			}
			canvas.restore();
			
			canvas.rotate(15, px, py);
		}
		canvas.restore();
	}
	
	public float getBearing() {
		return bearing;
	}
	
	public void setBearing(float bearing) {
		this.bearing = bearing;
		// 添加可访问性支持
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		// 将当前方向用作可访问性事件使用的内容值
		super.dispatchPopulateAccessibilityEvent(event);
		
		if (isShown()) {
			String bearingStr = String.valueOf(bearing);
			if (bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH)
				bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
			
			event.getText().add(bearingStr);
			return true;
		} else 
			return false;
		
	}

}
