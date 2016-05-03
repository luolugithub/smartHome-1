package com.demo.smarthome.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.demo.smarthome.R;
import com.demo.smarthome.service.Cfg;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**********************************************************
 *废弃
 *:2015-11-07
 **********************************************************/
public class HistoryDataLineView extends View {

	private static String TAG = "historyDataLineView";
	private static final int CIRCLE_SIZE = 10;
	private static enum Linestyle {
		Line, Curve
	}

	private Context mContext;
	private Paint mPaint;
	private Resources res;
	private DisplayMetrics dm;
	private boolean isInitDatafinish = false;
	/**
	 * data
	 */
	private Linestyle mStyle = Linestyle.Curve;

	private int canvasHeight;
	private int canvasWidth;
	private int bheight = 0;
	//
	private int blwidh;
	private boolean isMeasure = true;
	//
	boolean pm2_5Flag;
	/**
	 */
	private float maxValue;
	/**
	 */
	private float averageValue;


	//
	private static final int xTextChange =5;
	//
	private static final int yTextChange =5;
	//
	private static final int yTextUnit = 10;
	private static final int xSpaceCount = 13;

	private int marginTop = 50;
	private int marginBottom = 100;

	private static int noData = -1;
	//
	private String yUnit;

	//
	private static final float mPaintWidth = 2.0f;
	//
	private static final boolean enablePaintPoint = false;
	/**
	 *
	 */
	private Point[] mPoints;
	/**
	 *
	 */
	private ArrayList<Double> yRawData;
	/**
	 *
	 */

	private int spacingHeight;

	public HistoryDataLineView(Context context)
	{
		this(context, null);
	}

	public HistoryDataLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initView();
	}

	private void initView() {
		this.res = mContext.getResources();
		//
		this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//
		dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		if (isMeasure) {
			this.canvasHeight = getHeight();
			this.canvasWidth = getWidth();
			if (bheight == 0)
				bheight = (int) (canvasHeight - marginBottom);
			//
			blwidh = dip2px(30);
			isMeasure = false;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(!isInitDatafinish){
			return;
		}


		drawAllXLine(canvas);
		drawAllYLine(canvas);

		mPoints = getPoints();
		//
		mPaint.setColor(ContextCompat.getColor(mContext,R.color.result_points));
		mPaint.setStrokeWidth(dip2px(mPaintWidth));
		mPaint.setStyle(Style.STROKE);

		if (mStyle == Linestyle.Curve) {

			drawScrollLine(canvas);
		}
		else {
			drawLine(canvas);
		}

		//
		if(enablePaintPoint){
			mPaint.setStyle(Style.FILL);
			for (int i = 0; i < mPoints.length; i++) {

				canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
			}
		}
	}

	/**
	 */
	private void drawAllXLine(Canvas canvas) {

		Paint XlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		for (int i = 0; i < spacingHeight + 1; i++) {
			//
			if( i == 0){
				XlinePaint.setColor(ContextCompat.getColor(mContext,R.color.encode_view));
				canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, canvasWidth - blwidh,
						bheight - (bheight / spacingHeight) * i + marginTop, XlinePaint);
			}else{
				linePaint.setColor(ContextCompat.getColor(mContext,R.color.encode_view));
				canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, canvasWidth - blwidh,
						bheight - (bheight / spacingHeight) * i + marginTop, linePaint);
			}

			//
			if( i!= 0) {
				if (pm2_5Flag) {
					int intAverage = (int) averageValue;
					drawText(String.valueOf(intAverage * i), blwidh / 2 - dip2px(yTextChange + 2)
							, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
				} else {
					drawText(String.valueOf(averageValue * i), blwidh / 2 - dip2px(yTextChange)
							, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
				}
			}
		}
		//
		drawText(String.valueOf("(" + yUnit + ")"), blwidh / 2 - dip2px(yTextChange) - dip2px(5)
				,  dip2px(yTextUnit), canvas);
	}

	/**
	 */
	private void drawAllYLine(Canvas canvas) {
		//
		int offset = 0;
		if(Cfg.phoneWidth == 480){
			offset = dip2px(10);
		}
		Paint YlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		for (int i = 0; i < xSpaceCount; i++) {

			//
			if( i == 0){
				YlinePaint.setColor(ContextCompat.getColor(mContext,R.color.sbc_snippet_text));
				canvas.drawLine(blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, marginTop, blwidh
						+ (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, bheight + marginTop, YlinePaint);
			}else{
				linePaint.setColor(ContextCompat.getColor(mContext,R.color.encode_view));
				canvas.drawLine(blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, marginTop, blwidh
						+ (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, bheight + marginTop, linePaint);
			}


			//
			//
			if(i == xSpaceCount - 1){
				drawText(String.valueOf(i*2) + "(h)", blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(35) + offset, canvas);
			}else if(i == 0 || i > 9){
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(35)+ offset, canvas);
			}
			else {
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						, bheight + dip2px(35)+ offset, canvas);
			}
		}
	}

	private void drawScrollLine(Canvas canvas) {

		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < mPoints.length - 1; i++) {

			//
			if(mPoints[i].y == noData ){
				if(mPoints[i + 1].y == noData) {
					continue;
				}else {
					//0
					mPoints[i].y = bheight + marginTop;
				}
			}
			startp.x = mPoints[i].x;
			startp.y = mPoints[i].y;
			//
			if(mPoints[i + 1].y == noData){
				endp.x = mPoints[i + 1].x;
				endp.y = bheight + marginTop;
			}
			else{
				endp.x = mPoints[i + 1].x;
				endp.y = mPoints[i + 1].y;
			}
			int wt = (startp.x + endp.x) / 2;
			Point p3 = new Point();
			Point p4 = new Point();
			p3.y = startp.y;
			p3.x = wt;
			p4.y = endp.y;
			p4.x = wt;

			Path path = new Path();
			path.moveTo(startp.x, startp.y);
			path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
			canvas.drawPath(path, mPaint);
		}
	}

	private void drawLine(Canvas canvas) {

		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < mPoints.length - 1; i++) {

			//
			if(mPoints[i].y == noData ){
				if(mPoints[i + 1].y == noData) {
					continue;
				}else {
					//0
					mPoints[i].y = bheight + marginTop;
				}
			}
			startp.x = mPoints[i].x;
			startp.y = mPoints[i].y;
			//
			if(mPoints[i + 1].y == noData){
				endp.x = mPoints[i + 1].x;
				endp.y = bheight + marginTop;
			}
			else{
				endp.x = mPoints[i + 1].x;
				endp.y = mPoints[i + 1].y;
			}

			canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
		}
	}

	private void drawText(String text, int x, int y, Canvas canvas) {

		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setTextSize(dip2px(12));
		p.setColor(ContextCompat.getColor(mContext,R.color.encode_view));
		p.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(text, x, y, p);
	}

	private Point[] getPoints() {

		int Xvalue;
		double xPointSpace;
		Point[] points = new Point[yRawData.size()];
		for (int i = 0; i < yRawData.size(); i++) {


				int ph = bheight - (int) (bheight * (yRawData.get(i) / (int) maxValue));
				//
				xPointSpace = (double) (canvasWidth - 2 * blwidh) / (yRawData.size() - 1);
				Xvalue = (int) (blwidh + xPointSpace * i);
			//
			if(yRawData.get(i) >= 0) {
				points[i] = new Point(Xvalue, ph + marginTop);
			}else {
				points[i] = new Point(Xvalue,noData);
			}
		}
		return points;
	}

	public void setData(ArrayList<Double> yRawData, float maxValue, float averageValue,boolean pmFlag) {

		this.maxValue = maxValue;
		this.averageValue = averageValue;
		this.mPoints = new Point[yRawData.size()];
		this.yRawData = yRawData;
		this.spacingHeight = (int)(maxValue / averageValue);
		isInitDatafinish = true;
		this.pm2_5Flag = pmFlag;
		//
		postInvalidate();
	}

	public void setTotalvalue(float maxValue)
	{
		this.maxValue = maxValue;
	}

	public void setPjvalue(int averageValue)
	{
		this.averageValue = averageValue;
	}

	public void setMargint(int marginTop)
	{
		this.marginTop = marginTop;
	}

	public void setMarginb(int marginBottom)
	{
		this.marginBottom = marginBottom;
	}

	public void setMstyle(Linestyle mStyle)
	{
		this.mStyle = mStyle;
	}

	public void setBheight(int bheight)
	{
		this.bheight = bheight;
	}

	public void setyUnit(String yUnit) {
		this.yUnit = yUnit;
	}
	/**
	 *
	 */
	private int dip2px(float dpValue)
	{
		return (int) (dpValue * dm.density + 0.5f);
	}

}
