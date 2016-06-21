package com.meishipintu.assistant.posd;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * 打印动作封装类
 * 
 * @author Mocks
 * 
 */
public class PrintAction {
	public static enum PRINT_TYPE {
		TEXT, IMAGE, QRCODE, BARCODE
	}

	private PRINT_TYPE mPrintType;

	// PRINT_TYPE_STRING
	private Vector<Byte> mCommand = new Vector<Byte>();

	// PRINT_TYPE_IMAGE
	private byte[] imageData = null;
	private int imageWidth;
	private int imageHeight;

	// PRINT_TYPE_CODE
	private String codeStr = null;
	private int qr_size;
	private int qr_level;
	private int bar_type;
	private int bar_width;
	private int bar_height;

	public PrintAction(PRINT_TYPE printType) {
		mPrintType = printType;
	}

	public PRINT_TYPE getPrintType() {
		return mPrintType;
	}

	public byte[] getPrintBytes() {
		byte[] sendData = new byte[mCommand.size()];
		for (int i = 0; i < mCommand.size(); i++) {
			sendData[i] = ((Byte) mCommand.get(i)).byteValue();
		}
		return sendData;
	}

	public byte[] getImageBytes() {
		return imageData;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setLineMargin(byte x) {
		byte[] Cmd = new byte[3];
		Cmd[0] = 0x1B;
		Cmd[1] = 0x42;
		Cmd[2] = x;
		writeCmd(Cmd);
	}

	public void setAlign(int n) {
		byte[] Cmd = new byte[3];

		Cmd[0] = 0x1B;
		Cmd[1] = 0x61;
		Cmd[2] = (byte) n;
		writeCmd(Cmd);
	}

	public void setPrintMode(boolean small, boolean bold, boolean doubleHeight, boolean doubleWidth, boolean underLine) {
		byte cmd = 0x00;
		if (small) {
			cmd |= 0x01;
		}
		if (bold) {
			cmd |= 0x08;
		}
		if (doubleHeight) {
			cmd |= 0x10;
		}
		if (doubleWidth) {
			cmd |= 0x20;
		}
		if (underLine) {
			cmd |= 0x80;
		}

		writeCmd(new byte[] { 0x1B, 0x21, cmd });
	}

	public void buildString(String str) {

		byte[] bs = null;
		try {
			bs = str.getBytes("GB2312");
		} catch (UnsupportedEncodingException e) {
			bs = null;
		}
		writeCmd(bs);
	}

	public void buildImage(byte[][] pixels) {
		if (pixels == null) {
			return;
		}
		int width = pixels[0].length * 8;
		int height = pixels.length;
		byte[] imageByte = new byte[pixels[0].length * pixels.length];
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width / 8; i++) {
				imageByte[j * width / 8 + i] = pixels[j][i];
			}
		}
		imageData = imageByte;
		imageWidth = width;
		imageHeight = height;
	}

	/**
	 * 
	 * @param b
	 *            最大200
	 */
	public void buildImage(Bitmap b) {
		if (b == null) {
			return;
		}
		byte[][] pixels = getBitmapPrintPixels(b, 200);
		buildImage(pixels);
	}

	/**
	 * 
	 * @param str
	 * @param qr_size
	 *            最大248
	 * @param qr_level
	 *            0~4,等级越高，二维码越复杂，越容易识别
	 */
	public void buildQRCode(String str, int qr_size, int qr_level) {
		this.codeStr = str;
		this.qr_size = qr_size;
		this.qr_level = qr_level;
	}

	/**
	 * 
	 * @param str
	 * @param type
	 *            0--Code129,1--Code39
	 * @param width
	 *            最大384
	 * @param height
	 *            最大100
	 */
	public void buildBarCode(String str, int type, int width, int height) {
		this.codeStr = str;
		this.bar_type = type;
		this.bar_width = width;
		this.bar_height = height;
	}

	public String getCodeStr() {
		return codeStr;
	}

	public int getQRSize() {
		return qr_size;
	}

	public int getQRLevel() {
		return qr_level;
	}

	public int getBarType() {
		return bar_type;
	}

	public int getBarWidth() {
		return bar_width;
	}

	public int getBarHeight() {
		return bar_height;
	}

	private void writeCmd(byte[] cmd) {

		if (cmd == null) {
			return;
		}

		for (int i = 0; i < cmd.length; i++) {
			mCommand.add(Byte.valueOf(cmd[i]));
		}
	}

	private byte[][] getBitmapPrintPixels(Bitmap bm, final int maxHeight) {
		if (bm == null) {
			return null;
		} else {

			int width = bm.getWidth();
			int height = bm.getHeight();
			int newHeight = height > maxHeight ? maxHeight : height;

			float scaleHeight = ((float) newHeight) / height;

			int newWidth = (int) (width * scaleHeight);
			newWidth = (newWidth / 8) * 8;
			newWidth = newWidth > 312 ? 312 : newWidth;

			float scaleWidth = ((float) newWidth) / width;

			newHeight = (int) (height * scaleWidth);

			scaleHeight = ((float) newHeight) / height;

			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
			newWidth = newbm.getWidth();
			newHeight = newbm.getHeight();

			byte[][] adPicturePixels = new byte[newHeight][newWidth / 8];

			for (int j = 0; j < newHeight; j++) {
				for (int i = 0; i < newWidth; i++) {
					int pixel = newbm.getPixel(i, j) & 0xffffffff;
					int alpha = (pixel >> 24) & 0xff;
					int red = (pixel >> 16) & 0xff;
					int green = (pixel >> 8) & 0xff;
					int blue = pixel & 0xff;
					if (alpha == 0) {
						red = 255;
						green = 255;
						blue = 255;
					}
					int gray = (blue * 29 + green * 150 + red * 77) >> 8;
					if (gray > 200) {
						adPicturePixels[j][i / 8] &= ~(1 << (7 - i % 8));
					} else {
						adPicturePixels[j][i / 8] |= (1 << (7 - i % 8));
					}
				}
			}
			newbm.recycle();

			return adPicturePixels;
		}
	}
}