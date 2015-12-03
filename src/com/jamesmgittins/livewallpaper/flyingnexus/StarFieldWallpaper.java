package com.jamesmgittins.livewallpaper.flyingnexus;

import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class StarFieldWallpaper extends WallpaperService {

	private final Handler mHandler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	class WallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private boolean mVisible;
		private Random random;
		private Paint opacPaint;
		private Bitmap starBitmap;
		public Matrix aMatrix;
		
		private static final int BITMAP_WIDTH = 60;
		private static final int BITMAP_HEIGHT = 52;
		
		private Bitmap getStarBitmap() {
			if (starBitmap == null) {
				
				starBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);				
				Canvas c = new Canvas(starBitmap);
				
				int centerX = c.getWidth() / 2;
				int centerY = c.getHeight() / 2;
				
				Paint paint = new Paint();
				paint.setARGB(255, PAINT_BRIGHTNESS, PAINT_BRIGHTNESS, 255);
				paint.setAntiAlias(true);
				paint.setStrokeWidth(8);
				paint.setStrokeCap(Cap.ROUND);
				
				// draw blue line
				c.drawLine(centerX - 5, centerY - 5, centerX - 30, centerY - 30, paint);
				
				// draw red line
				paint.setARGB(255, 255, PAINT_BRIGHTNESS, PAINT_BRIGHTNESS);				
				c.drawLine(centerX + 5, centerY - 5, centerX + 30, centerY - 30, paint);
				
				// draw yellow line
				paint.setARGB(255, 255, 255, PAINT_BRIGHTNESS);
				c.drawLine(centerX - 5, centerY + 5, centerX - 30, centerY + 30, paint);
				
				// draw green line
				paint.setARGB(255, PAINT_BRIGHTNESS, 255, PAINT_BRIGHTNESS);
				c.drawLine(centerX + 5, centerY + 5, centerX + 30, centerY + 30, paint);
			}
			
			return starBitmap;
		}
		
		private final Runnable mDrawCube = new Runnable() {
			@Override
			public void run() {
				drawFrame();
			}
		};
		
		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
		
					c.drawRGB(0, 0, 0);
					drawStars(c);
				}
			} finally {
				try {
					if (c != null) holder.unlockCanvasAndPost(c);
				} catch (Exception e) {
					
				}
			}

			// Reschedule the next redraw
			mHandler.removeCallbacks(mDrawCube);
			if (mVisible) {
				mHandler.postDelayed(mDrawCube, 40);
			}
		}
		
		private static final int NUM_STARS = 60;
		private Star[] stars = new Star[NUM_STARS];
		private int cWidth;
		private int cHeight;
		private int centerX;
		private int centerY;
		
		private float scaling;
				
		private void drawStars(Canvas c) {
			
			cWidth = c.getWidth();
			cHeight = c.getHeight();
			centerX = c.getWidth() / 2;
			centerY = c.getHeight() / 2;
			
			for (int i = 0; i < NUM_STARS; i++) {
				
				if (stars[i] == null) {
					stars[i] = new Star();
					stars[i].xLoc = -1000 + random.nextInt(2000);
					stars[i].yLoc = -1000 + random.nextInt(2000);
					stars[i].zLoc = 100 + random.nextInt(900);
					stars[i].zVelocity = 0.5f + (random.nextFloat() * 4.5f);
				}
				
				// (move the star closer)
				stars[i].zLoc -= stars[i].zVelocity;

				// (calculate screen coordinates)
				stars[i].screenX = stars[i].xLoc / stars[i].zLoc * 100 + centerX;
				stars[i].screenY = stars[i].yLoc / stars[i].zLoc * 100 + centerY;
				
				// if star off screen
				if (	stars[i].screenX + BITMAP_WIDTH < 0 || 
						stars[i].screenX - BITMAP_WIDTH > cWidth || 
						stars[i].screenY + BITMAP_HEIGHT < 0 || 
						stars[i].screenY - BITMAP_HEIGHT > cHeight || 
						stars[i].zLoc < 1) {
					
					stars[i].xLoc = -500 + random.nextInt(1000);
					stars[i].yLoc = -500 + random.nextInt(1000);
					stars[i].zLoc = 1000;
					stars[i].zVelocity = 0.5f + (random.nextFloat() * 4.5f);
				}
				
				
				opacPaint.setAlpha(Math.round(((255f/5f) * stars[i].zVelocity) * (1f - stars[i].zLoc / 1000f)));

				scaling = ((2f/5f) * stars[i].zVelocity) * (1f - stars[i].zLoc / 1000f);
				aMatrix.setScale(scaling, scaling);
				aMatrix.postTranslate(stars[i].screenX - (BITMAP_WIDTH/2f * scaling), stars[i].screenY - (BITMAP_HEIGHT/2f * scaling));
				c.drawBitmap(getStarBitmap(), aMatrix, opacPaint);
			}
		}
		
		private static final int PAINT_BRIGHTNESS = 100;
		
		WallpaperEngine() {			
			random = new Random(System.currentTimeMillis());
			aMatrix = new Matrix();
			aMatrix.preTranslate(0, 0);
			
			opacPaint = new Paint();
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			// By default we don't get touch events, so enable them.
			setTouchEventsEnabled(true);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mDrawCube);
		}
		
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				mHandler.post(mDrawCube);
			} else {
				mHandler.removeCallbacks(mDrawCube);
			}
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			
		}
	}

}
