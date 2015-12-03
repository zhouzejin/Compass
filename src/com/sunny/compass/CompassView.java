package com.sunny.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Align;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.Shader.TileMode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class CompassView extends View {
	
	private float bearing;
	private float pitch; // 倾斜值
	private float roll; // 滚动值
	
	private Paint markerPaint;
	private Paint textPaint;
	private Paint circlePaint;
	
//	private String northString;
//	private String eastString;
//	private String southString;
//	private String westString;
	
	private int textHeight;
	
	int[] borderGradientColors;
	float[] borderGradientPositions;
	
	int[] glassGradientColors;
	float[] glassGradientPositions;
	
	int skyHorizonColorFrom;
	int skyHorizonColorTo;
	int groundHorizonColorFrom;
	int groundHorizonColorTo;
	
	private enum CompassDirection {
		N, NNE, NE, ENE, 
		E, ESE, SE, SSE, 
		S, SSW, SW, WSW, 
		W, WNW, NW, NNW
	}

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
		// circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		circlePaint.setStyle(Paint.Style.STROKE);
		
//		northString = r.getString(R.string.cardinal_north);
//		eastString = r.getString(R.string.cardinal_east);
//		southString = r.getString(R.string.cardinal_south);
//		westString = r.getString(R.string.cardinal_west);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.text_color));
		textPaint.setFakeBoldText(true);
		textPaint.setSubpixelText(true);
		textPaint.setTextAlign(Align.LEFT);
		
		textHeight = (int) textPaint.measureText("yY");
		
		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		markerPaint.setColor(r.getColor(R.color.marker_color));
		markerPaint.setAlpha(200);
		markerPaint.setStrokeWidth(1);
		markerPaint.setStyle(Paint.Style.STROKE);
		markerPaint.setShadowLayer(2, 1, 1, r.getColor(R.color.shadow_color));
		
		borderGradientColors = new int[4];
		borderGradientPositions = new float[4];
		
		borderGradientColors[3] = r.getColor(R.color.outer_border);
		borderGradientColors[2] = r.getColor(R.color.inner_border_one);
		borderGradientColors[1] = r.getColor(R.color.inner_border_two);
		borderGradientColors[0] = r.getColor(R.color.inner_border);
		
		borderGradientPositions[3] = 0.0f;
		borderGradientPositions[2] = 1-0.03f;
		borderGradientPositions[1] = 1-0.06f;
		borderGradientPositions[0] = 1.0f;
		
		glassGradientColors = new int[5];
		glassGradientPositions = new float[5];
		
		int glassColor = 245;
		glassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);
		glassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
		glassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
		glassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
		glassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);
		
		glassGradientPositions[4] = 1-0.0f;
		glassGradientPositions[3] = 1-0.06f;
		glassGradientPositions[2] = 1-0.10f;
		glassGradientPositions[1] = 1-0.20f;
		glassGradientPositions[0] = 1-1.0f;
		
		skyHorizonColorFrom = r.getColor(R.color.horizon_sky_from);
		skyHorizonColorTo = r.getColor(R.color.horizon_sky_to);
		
		groundHorizonColorFrom = r.getColor(R.color.horizon_ground_from);
		groundHorizonColorTo = r.getColor(R.color.horizon_ground_to);
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
	
	/*@Override
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
	}*/
	
	public float getBearing() {
		return bearing;
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// 根据绘制方向值的字体大小，计算外圆环的宽度
		float ringWidth = textHeight + 4;
		
		// 计算View的高度和宽度，并用于计算外刻度盘和内刻度盘的半径，同事创建每个盘面的包围框
		int height = getMeasuredHeight();
		int width = getMeasuredWidth();
		
		int px = width / 2;
		int py = height / 2;
		Point center = new Point(px, py);
		
		int radius = Math.min(px, py) - 2;
		
		RectF boundingBox = new RectF(
				center.x - radius, 
				center.y - radius, 
				center.x + radius, 
				center.y + radius);
		
		RectF innerBoundingBox = new RectF(
				center.x - radius + ringWidth, 
				center.y - radius + ringWidth, 
				center.x + radius - ringWidth, 
				center.y + radius - ringWidth);
		
		float innerRadius = innerBoundingBox.height() / 2;
		
		// 绘制盘面：从外围的底层开始，向里面和上方绘制
		RadialGradient boderGradient = new RadialGradient(px, py, radius, 
				borderGradientColors, borderGradientPositions, TileMode.CLAMP);
		
		Paint pgb = new Paint();
		pgb.setShader(boderGradient);
		
		Path outerRingPath = new Path();
		outerRingPath.addOval(boundingBox, Direction.CW);
		
		canvas.drawPath(outerRingPath, pgb);
		
		// 绘制地平仪
		LinearGradient skyShader = new LinearGradient(center.x, 
				innerBoundingBox.top, center.x, innerBoundingBox.bottom,
		        skyHorizonColorFrom, skyHorizonColorTo, TileMode.CLAMP);
		
		Paint skyPaint = new Paint();
		skyPaint.setShader(skyShader);
		
		LinearGradient groundShader = new LinearGradient(center.x,
			      innerBoundingBox.top, center.x, innerBoundingBox.bottom,
			      groundHorizonColorFrom, groundHorizonColorTo, TileMode.CLAMP);
		
		Paint groundPaint = new Paint();
		groundPaint.setShader(groundShader);
		
		// 通过形式化俯仰角和反转角的值来让View处于相应的范围内
		float tiltDegree = pitch;
		while (tiltDegree > 90 || tiltDegree < -90) {
			if (tiltDegree > 90)
				tiltDegree = -90 + (tiltDegree - 90);
			if (tiltDegree < -90)
				tiltDegree = 90 - (tiltDegree + 90);
		}

		float rollDegree = roll;
		while (rollDegree > 180 || rollDegree < -180) {
			if (rollDegree > 180)
				rollDegree = -180 + (rollDegree - 180);
			if (rollDegree < -180)
				rollDegree = 180 - (rollDegree + 180);
		}
		
		// 创建用来填充圆的每个部分的路径
		Path skyPath = new Path();
		skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + (2 * tiltDegree)));
		
		// 将画布围绕圆心按照与当前翻转角相反的方向进行旋转
		canvas.save();
		canvas.rotate(-rollDegree, px, py);
		canvas.drawOval(innerBoundingBox, groundPaint);
		canvas.drawPath(skyPath, skyPaint);
		canvas.drawPath(skyPath, markerPaint);
		
		// 计算水平的水平仪的起始点
		int markWidth = radius / 3;
		int startX = center.x - markWidth;
		int endX = center.x + markWidth;
		
		// 计算天空和地面在水平仪盘面上的接触位置
		double h = innerRadius * Math.cos(Math.toRadians(90 - tiltDegree));
		double justTiltY = center.y - h;
		
		// 找出标示每一个倾斜度的像素的数量
		float pxPerDegree = (innerBoundingBox.height() / 2) / 45f;
		
		// 以当前的倾斜值为中心便利180度，给出一个可能的俯仰角的滑动刻度
		for (int i = 90; i >= -90; i -= 10) {
			double ypos = justTiltY + i * pxPerDegree;

			// Only display the scale within the inner face.
			if ((ypos < (innerBoundingBox.top + textHeight))
					|| (ypos > innerBoundingBox.bottom - textHeight))
				continue;

			// Draw a line and the tilt angle for each scale increment.
			canvas.drawLine(startX, (float) ypos, 
					endX, (float) ypos,
					markerPaint);
			int displayPos = (int) (tiltDegree - i);
			String displayString = String.valueOf(displayPos);
			float stringSizeWidth = textPaint.measureText(displayString);
			canvas.drawText(displayString,
					(int) (center.x - stringSizeWidth / 2), 
					(int) (ypos) + 1,
					textPaint);
		}
		
		// 还原markerPaint对象的笔画粗度
		markerPaint.setStrokeWidth(2);
		canvas.drawLine(center.x - radius / 2,
                (float)justTiltY,
                center.x + radius / 2,
                (float)justTiltY,
                markerPaint); 
		markerPaint.setStrokeWidth(1);
		
		// Draw the arrow
		Path rollArrow = new Path();
		rollArrow.moveTo(center.x - 3, (int) innerBoundingBox.top + 14);
		rollArrow.lineTo(center.x, (int) innerBoundingBox.top + 10);
		rollArrow.moveTo(center.x + 3, innerBoundingBox.top + 14);
		rollArrow.lineTo(center.x, innerBoundingBox.top + 10);
		canvas.drawPath(rollArrow, markerPaint);
		// Draw the string
		String rollText = String.valueOf(rollDegree);
		double rollTextWidth = textPaint.measureText(rollText);
		canvas.drawText(rollText, 
				(float) (center.x - rollTextWidth / 2),
				innerBoundingBox.top + textHeight + 2, 
				textPaint);
		
		// 将画布旋转到正上方，从而去绘制其他盘面标记
		canvas.restore();
		
		// 每次将画布旋转10度，然后画一个标记或一个值；表盘绘制完毕后，把画布恢复为正上方的方向。
		canvas.save();
		canvas.rotate(180, center.x, center.y);
		for (int i = -180; i < 180; i += 10) {
			// Show a numeric value every 30 degrees
			if (i % 30 == 0) {
				String rollString = String.valueOf(i * -1);
				float rollStringWidth = textPaint.measureText(rollString);
				PointF rollStringCenter = new PointF(center.x - rollStringWidth/ 2, 
						innerBoundingBox.top + 1 + textHeight);
				canvas.drawText(rollString, 
						rollStringCenter.x, rollStringCenter.y, 
						textPaint);
			}
			// Otherwise draw a marker line
			else {
				canvas.drawLine(center.x, (int) innerBoundingBox.top, 
						center.x, (int) innerBoundingBox.top + 5, 
						markerPaint);
			}

			canvas.rotate(10, center.x, center.y);
		}
		canvas.restore();
		
		// 在表盘的外边界绘制方向标记
		canvas.save();
		canvas.rotate(-1 * (bearing), px, py);

		// Should this be a double?
		double increment = 22.5;

		for (double i = 0; i < 360; i += increment) {
			CompassDirection cd = CompassDirection.values()
					[(int) (i / 22.5)];
			String headString = cd.toString();

			float headStringWidth = textPaint.measureText(headString);
			PointF headStringCenter = 
					new PointF(center.x - headStringWidth / 2, 
							boundingBox.top + 1 + textHeight);

			if (i % increment == 0)
				canvas.drawText(headString, 
						headStringCenter.x, headStringCenter.y, 
						textPaint);
			else
				canvas.drawLine(center.x, (int) boundingBox.top, 
						center.x, (int) boundingBox.top + 3, 
						markerPaint);

			canvas.rotate((int) increment, center.x, center.y);
		}
		canvas.restore();
		
		// 在内盘面上绘制一个园，使其像是有玻璃覆盖的感觉
		RadialGradient glassShader = 
				new RadialGradient(px, py, (int) innerRadius, 
						glassGradientColors, 
						glassGradientPositions, 
						TileMode.CLAMP);
		Paint glassPaint = new Paint();
		glassPaint.setShader(glassShader);

		canvas.drawOval(innerBoundingBox, glassPaint);

		// Draw the outer ring
		canvas.drawOval(boundingBox, circlePaint);

		// Draw the inner ring
		circlePaint.setStrokeWidth(2);
		canvas.drawOval(innerBoundingBox, circlePaint);
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

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	}

}
