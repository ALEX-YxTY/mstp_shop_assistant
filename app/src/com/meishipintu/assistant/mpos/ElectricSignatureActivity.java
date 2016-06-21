package com.meishipintu.assistant.mpos;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.epos.bitmap.monochrome.BitmapConvertor;
import com.epos.jbiglib.JBIGLib;
import com.epos.utilstools.MPosConfig;
import com.meishipintu.assistant.R;

public class ElectricSignatureActivity extends Activity {
	
	private String mStrExtraData=null;
	LinearLayout mContent;
	signature mSignature;
	private Bitmap mBitmap;
	Button mClear, mGetSign, mCancel;
	View mView;
	File mypath;
	public static String tempDir;
	private String uniqueId;
	public String current = null;
	public static Bitmap createBlackAndWhite(Bitmap src) {
	    int width = src.getWidth();
	    int height = src.getHeight();
	    // create output bitmap
	    Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
	    // color information
	    int A, R, G, B;
	    int pixel;

	    // scan through all pixels
	    for (int x = 0; x < width; ++x) {
	        for (int y = 0; y < height; ++y) {
	            // get pixel color
	            pixel = src.getPixel(x, y);
	            A = Color.alpha(pixel);
	            R = Color.red(pixel);
	            G = Color.green(pixel);
	            B = Color.blue(pixel);
	            int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

	            // use 128 as threshold, above -> white, below -> black
	            if (gray > 128) 
	                gray = 255;
	            else
	                gray = 0;
	            // set new pixel color to output bitmap
	            bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
	        }
	    }
	    return bmOut;
	}
	
	public Bitmap ConvertToBlackAndWhite(Bitmap sampleBitmap){
		ColorMatrix bwMatrix =new ColorMatrix();
		bwMatrix.setSaturation(0);
		final ColorMatrixColorFilter colorFilter= new ColorMatrixColorFilter(bwMatrix);
		Bitmap rBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Paint paint=new Paint();
		paint.setColorFilter(colorFilter);
		Canvas myCanvas =new Canvas(rBitmap);
		myCanvas.drawBitmap(rBitmap, 0, 0, paint);
		return rBitmap;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_electric_signature);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.window_title_withbutton);
        //((MainApp)getApplicationContext()).isCountTime=false;
       if( getIntent().getExtras()!=null&&!getIntent().getExtras().isEmpty()&&getIntent().getExtras().containsKey("EXTRADATA")){
        
        	try {
				mStrExtraData=new String(getIntent().getExtras().getByteArray("EXTRADATA"),"GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mStrExtraData=null;
			}
		
       }
        
        tempDir = Environment.getExternalStorageDirectory() + "/"
				+ getResources().getString(R.string.external_dir) + "/";
		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		File directory = cw.getDir(getResources().getString(R.string.external_dir),
				Context.MODE_PRIVATE);
		/*
		Point size = new Point();
	    getWindowManager().getDefaultDisplay().getSize(size);
	    Log.v("diiiiii", String.format("%d--%d", size.x,size.y));
	    */
		prepareDirectory();
		uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_"
				+ Math.random();
		current = uniqueId + ".jpeg";
		mypath = new File(tempDir, current);
        
        mContent = (LinearLayout) findViewById(R.id.linearLayout);
		mSignature = new signature(this, null);
		mSignature.setBackgroundColor(Color.WHITE);
		mContent.addView(mSignature, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		
		mClear = (Button) findViewById(R.id.button_Clear);		
		mGetSign = (Button) findViewById(R.id.button_OK);
		mCancel = (Button) findViewById(R.id.button_Cancle);
		mView = mContent;		
		mClear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v("log_tag", "Panel Cleared");
				mSignature.clear();
				mGetSign.setEnabled(false);
			}
		});
		
		mGetSign.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v("log_tag", "Panel Saved");
				boolean error = false;//captureSignature();
				if (!error) {					
					mView.setDrawingCacheEnabled(true);
					mSignature.save(mView);
					Bundle b = new Bundle();
					b.putString("status", "done");
					Intent intent = new Intent();
					intent.putExtras(b);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*
				Log.v("log_tag", "Panel Canceled");
				Bundle b = new Bundle();
				b.putString("status", "cancel");
				Intent intent = new Intent();
				intent.putExtras(b);
				setResult(RESULT_OK, intent);
				*/
				finish();
			}
		});
		//mCancel.setVisibility(View.GONE);
		
    }
    private boolean captureSignature() {

		return true;
	}
    
    private String getTodaysDate() {

		final Calendar c = Calendar.getInstance();
		int todaysDate = (c.get(Calendar.YEAR) * 10000)
				+ ((c.get(Calendar.MONTH) + 1) * 100)
				+ (c.get(Calendar.DAY_OF_MONTH));
		Log.w("DATE:", String.valueOf(todaysDate));
		return (String.valueOf(todaysDate));

	}

	private String getCurrentTime() {

		final Calendar c = Calendar.getInstance();
		int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000)
				+ (c.get(Calendar.MINUTE) * 100) + (c.get(Calendar.SECOND));
		Log.w("TIME:", String.valueOf(currentTime));
		return (String.valueOf(currentTime));

	}
    private boolean prepareDirectory() {
		try {
			if (makedirs()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(
					this,
					"Could not initiate File System.. Is Sdcard mounted properly?",
					1000).show();
			return false;
		}
	}
    private boolean makedirs() {
		File tempdir = new File(tempDir);
		if (!tempdir.exists())
			tempdir.mkdirs();
		/*
		if (tempdir.isDirectory()) {
			File[] files = tempdir.listFiles();
			for (File file : files) {
				if (!file.delete()) {
					System.out.println("Failed to delete " + file);
				}
			}
		}
		*/
		return (tempdir.isDirectory());
	}
    public class signature extends View {
		private static final float STROKE_WIDTH = 10f;
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
		private Paint paint = new Paint();
		private Path path = new Path();

		private float lastTouchX;
		private float lastTouchY;
		private final RectF dirtyRect = new RectF();

		public signature(Context context, AttributeSet attrs) {
			super(context, attrs);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH);
			paint.setTextSize(60);
			ColorMatrix ma = new ColorMatrix();
			ma.setSaturation(0);//生成一个灰度图像
			paint.setColorFilter(new ColorMatrixColorFilter(ma));
			
		}

		public void save(View v) {
			Log.v("log_tag", "Width: " + v.getWidth());
			Log.v("log_tag", "Height: " + v.getHeight());
			if (mBitmap == null) {
				mBitmap = Bitmap.createBitmap(mContent.getWidth(),mContent.getHeight(), Bitmap.Config.ARGB_8888);
			}
			Canvas canvas = new Canvas(mBitmap);
			try {
				//FileOutputStream mFileOutStream = new FileOutputStream(mypath);				
				v.draw(canvas);
				int nWidth=380;
				int nHeight=(int)(((float)nWidth/(float)mContent.getWidth())*mContent.getHeight());
				Bitmap resizeBitmap=Bitmap.createScaledBitmap(mBitmap,nWidth, nHeight,true);				
				//mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
				BitmapConvertor convertor = new BitmapConvertor();
	            convertor.convertBitmap(ConvertToBlackAndWhite(resizeBitmap), tempDir+"orignalsignature.bmp");//����ԭʼbmp
				/*
				int bytes = bitmap.getByteCount();
				ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
				bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
				*/
				JBIGLib jbigLib=new JBIGLib();
				int nSuccess=jbigLib.BmpToJBig(convertor.mRawBitmapData,nWidth,nHeight);
				if(nSuccess>=0){					
					File file = new File("//mnt//sdcard//signature.jbg");
					FileInputStream fileStream = new FileInputStream(file);
					byte byteFile[] = new byte[(int) file.length()];
					fileStream.read(byteFile);
					fileStream.close();
					
					showByteInfo(byteFile);
					Log.v("log_tag", "save ok");					
					MPosConfig.mResult.set(false);
				}
				else{
					Log.v("log_tag", "read failure");
				}
				
				//mFileOutStream.flush();
				//mFileOutStream.close();
				//String url = Images.Media.insertImage(getContentResolver(),mBitmap, "title", null);
				//Log.v("log_tag", "url: " + url);
				// In case you want to delete the file
				// boolean deleted = mypath.delete();
				// Log.v("log_tag","deleted: " + mypath.toString() + deleted);
				// If you want to convert the image to string use base64
				// converter

			} catch (Exception e) {
				Log.v("log_tag", e.toString());
			}
			
		}
		public void showByteInfo(byte [] byteInfo){
			String strDisp="";
			for(int i=0;i<byteInfo.length;i++){
				strDisp=strDisp+String.format("%2X", byteInfo[i]).replace(" ","0")+" ";
			}
			System.out.println(strDisp);
		}

		public void clear() {
			path.reset();
			invalidate();
		}
		private void drawCenter(Canvas canvas){
			if(mStrExtraData!=null){
				float stokeWidth=paint.getStrokeWidth();
				paint.setStrokeWidth(0);
				Rect rect = new Rect();
				paint.getTextBounds(mStrExtraData, 0, mStrExtraData.length(), rect);
				float nStartX=0,nStartY=0;
				nStartX=(rect.width()<mContent.getWidth())? (mContent.getWidth()-rect.width())/2:0;
				nStartY=mContent.getHeight()-(mContent.getHeight()-rect.height())/2;
				canvas.drawText(mStrExtraData,nStartX,nStartY, paint);
				paint.setStrokeWidth(stokeWidth);
			}
		}
		@Override
		protected void onDraw(Canvas canvas) {
			drawCenter(canvas);
			canvas.drawPath(path, paint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float eventX = event.getX();
			float eventY = event.getY();
			mGetSign.setEnabled(true);
			int nAction=event.getAction();
			switch (nAction) {
			case MotionEvent.ACTION_DOWN:
				path.moveTo(eventX, eventY);
				lastTouchX = eventX;
				lastTouchY = eventY;
				return true;

			case MotionEvent.ACTION_MOVE:

			case MotionEvent.ACTION_UP:

				resetDirtyRect(eventX, eventY);
				int historySize = event.getHistorySize();
				for (int i = 0; i < historySize; i++) {
					float historicalX = event.getHistoricalX(i);
					float historicalY = event.getHistoricalY(i);
					expandDirtyRect(historicalX, historicalY);
					path.lineTo(historicalX, historicalY);
				}			
				path.lineTo(eventX, eventY);

				break;
			

			default:
				debug("Ignored touch event: " + event.toString());
				return false;
			}

			invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
					(int) (dirtyRect.top - HALF_STROKE_WIDTH),
					(int) (dirtyRect.right + HALF_STROKE_WIDTH),
					(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

			lastTouchX = eventX;
			lastTouchY = eventY;

			return true;
		}

		private void debug(String string) {
		}

		private void expandDirtyRect(float historicalX, float historicalY) {
			if (historicalX < dirtyRect.left) {
				dirtyRect.left = historicalX;
			} else if (historicalX > dirtyRect.right) {
				dirtyRect.right = historicalX;
			}

			if (historicalY < dirtyRect.top) {
				dirtyRect.top = historicalY;
			} else if (historicalY > dirtyRect.bottom) {
				dirtyRect.bottom = historicalY;
			}
		}

		private void resetDirtyRect(float eventX, float eventY) {
			dirtyRect.left = Math.min(lastTouchX, eventX);
			dirtyRect.right = Math.max(lastTouchX, eventX);
			dirtyRect.top = Math.min(lastTouchY, eventY);
			dirtyRect.bottom = Math.max(lastTouchY, eventY);
		}
	}

}
