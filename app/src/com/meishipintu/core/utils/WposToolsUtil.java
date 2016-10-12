package com.meishipintu.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;

import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.IPrint.Gravity;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.LatticePrinter.FontFamily;
import cn.weipass.pos.sdk.LatticePrinter.FontSize;
import cn.weipass.pos.sdk.LatticePrinter.FontStyle;
import cn.weipass.pos.sdk.Printer;

public class WposToolsUtil {
	/**
	 * font_size:字体大小枚举值 SMALL:16x16大小; MEDIUM:24x24大小; LARGE:32x32大小;
	 * EXTRALARGE:48x48 一行的宽度为384
	 * (当宽度大小为16时可打印384/16=24个字符;为24时可打印384/24=16个字符;为32时可
	 * 打印384/32=12个字符;为48时可打印384/48=8个字符（一个汉字占1个字符，一个字母 、空格或者数字占半字符）
	 *
	 * 标准打印示例
	 *
	 * @param context
	 * @param printer
	 */
	public static final int rowSize = 384;
	// public static final int smallSize = (int) (384/16d);
	// public static final int mediumSize = (int) (384/24d);
	// public static final int largeSize = (int) (384/32d);
	// public static final int extralargeSize = (int) (384/48d);
	public static final int smallSize = 24 * 2;
	public static final int mediumSize = 16 * 2;
	public static final int largeSize = 12 * 2;
	public static final int extralargeSize = 8 * 2;

	public static String getPrintErrorInfo(int what, String info) {
		String message = "";
		switch (what) {
		case IPrint.EVENT_CONNECT_FAILD:
			message = "连接打印机失败";
			break;
		case IPrint.EVENT_CONNECTED:
			// Log.e("subscribe_msg", "连接打印机成功");
			break;
		case IPrint.EVENT_PAPER_JAM:
			message = "打印机卡纸";
			break;
		case IPrint.EVENT_UNKNOW:
			message = "打印机未知错误";
			break;
		case IPrint.EVENT_OK:
			// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
			// Log.e("subscribe_msg", "打印机正常");
			break;
		case IPrint.EVENT_NO_PAPER:
			message = "打印机缺纸";
			break;
		case IPrint.EVENT_HIGH_TEMP:
			message = "打印机高温";
			break;
		}

		return message;
	}

	/**
	 * 获取md5加密信息
	 *
	 * @param s
	 * @return
	 */
	public static String getStringMD5(String s) {
		// char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
		// '9',
		// 'A', 'B', 'C', 'D', 'E', 'F' };
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		if (str == null || str.trim().equals("")
				|| str.trim().equalsIgnoreCase("null")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
	 *
	 */
	public static int length(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}

	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5
	 *
	 */
	public static double getLength(String s) {
		if (s == null) {
			return 0;
		}
		double valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < s.length(); i++) {
			// 获取一个字符
			String temp = s.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 1;
			} else {
				// 其他字符长度为0.5
				valueLength += 0.5;
			}
		}
		// 进位取整
		return Math.ceil(valueLength);
	}

	public static String getBlankBySize(int size) {
		String resultStr = "";
		for (int i = 0; i < size; i++) {
			resultStr += " ";
		}
		return resultStr;
	}

	// 将Drawable转化为Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	// Bitmap → byte[]
	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
}
