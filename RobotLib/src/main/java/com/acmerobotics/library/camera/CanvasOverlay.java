package com.acmerobotics.library.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class CanvasOverlay {

	public enum ImageRegion {
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
	}

	private int bgColor = 0;

	private Canvas canvas;
	private double width, height;

	private int padding;
	private int[] offsets = {0, 0, 0, 0, 0, 0};

	public CanvasOverlay(Canvas canvas, int padding) {
		this.canvas = canvas;
		this.padding = padding;
		this.width = canvas.getWidth();
		this.height = canvas.getHeight();
	}

	public CanvasOverlay(Canvas canvas) {
		this(canvas, 10);
	}
	
	public void drawText(String text, ImageRegion region, double height, Paint paint) {
		float textSize = (float) (height * this.height - 2 * padding);
		paint.setTextSize(textSize);
		float textWidth = paint.measureText(text);
		
		int offset = offsets[region.ordinal()];
		
		int x = 0;
		switch(region) {
		case TOP_LEFT:
		case BOTTOM_LEFT:
			x = 0;
			break;
		case TOP_RIGHT:
		case BOTTOM_RIGHT:
			x = (int) (width - (padding + textWidth));
			break;
		case TOP_CENTER:
		case BOTTOM_CENTER:
			x = (int) (width - textWidth) / 2;
			break;
		}
		
		Rect rect = null;		
		switch(region) {
		case TOP_LEFT:
		case TOP_CENTER:
		case TOP_RIGHT:
			rect = new Rect(x, offset, (int) (x + textWidth + 2 * padding), (int) (offset + textSize + 2 * padding));
			break;
		case BOTTOM_LEFT:
		case BOTTOM_CENTER:
		case BOTTOM_RIGHT:
			rect = new Rect(x, (int) (this.height - offset - 2 * padding - textSize), (int) (x + textWidth + 2 * padding), (int) (this.height - offset));
			break;
		}

		int oldColor = paint.getColor();
		if (bgColor != 0) {
			paint.setColor(Color.BLUE);
			canvas.drawRect(rect, paint);
		}
		paint.setColor(oldColor);

		canvas.drawText(text, rect.left + padding, rect.top + textSize + padding, paint);
		
		offsets[region.ordinal()] = (int) (offset + 2 * padding + textSize);
	}
	
	public void setBackgroundColor(int color) {
		bgColor = color;
	}
	
	public void clearBackgroundColor() {
		bgColor = 0;
	}

}
