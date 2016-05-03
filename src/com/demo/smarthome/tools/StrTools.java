package com.demo.smarthome.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * 
 * @author Administrator
 * 
 */
public class StrTools {
	public static int byteToUint(byte b) {
		return (int) (b & 0xFF);

	}

	public static byte[] byteToSwapByte(byte[] b) {
		int length = b.length;
		byte[] buff = new byte[length];
		for (int i = 0; i < length; i++) {
			buff[i] = b[length - 1 - i];
		}
		return buff;
	}

	public static long stringToInt(String strNum) {
		long index = 0;
		byte[] b = strNum.getBytes();
		byte[] buff = new byte[b.length];
		long num = 0;
		for (int i = 0; i < b.length; i++) {
			if ((b[i] >= 0x30) && (b[i] <= 0x39)) {
				num *= 10;
				num += b[i] - 0x30;
			} else {
				// if(index != 0){
				// break;
				// }
			}
		}

		// try {
		// tmp = new String(buff);
		// //num = Integer.parseInt(strNum.trim());
		// num = Integer.parseInt(new String(buff));
		// } catch (Exception e) {
		// num = 0;
		// }
		return num;
	}

	// public static int strToInt(String str) {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	public static long bcdIntToInt(long num) {
		int[] valPar = new int[] { 1, 10, 100, 1000, 10000, 100000, 1000000,
				10000000 };
		int val = 0;
		int i = 0;
		for (i = 0; i < 8; i++) {

			val += (num % 16) * valPar[i];
			num /= 16;
		}
		return val;
		// int val = 0;
		// try {
		// val = Integer.parseInt(num + "", 10);
		// } catch (Exception e) {
		// val = 0;
		// }
		// return val;
	}

	// public static int strToInt(String str) {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	public static long intToBcdInt(long num) {
		// int val = 0;
		// try {
		// val = Integer.parseInt(num + "", 16);
		// } catch (Exception e) {
		// val = 0;
		// }
		// return val;
		long val = 0;
		int i = 0;
		for (i = 0; i < 8; i++) {
			val += (num % 10) << (i * 4);
			num /= 10;
		}
		return val;
	}

	public static long idTo8ByteId(long num) {
		// int val = 0;
		// try {
		// val = Integer.parseInt(num + "", 16);
		// } catch (Exception e) {
		// val = 0;
		// }
		// return val;
		long val = 0;
		int i = 0;
		for (i = 0; i < 8; i++) {
			val += (num % 10) << (i * 4);
			num /= 10;
		}
		return val;
	}

	/*
	 * Convert byte[] to hex
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		if (hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	//
	public static void printHexString(byte[] b) {
		System.out.print("len:" + b.length + "  ");
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase() + " ");
		}
		System.out.println();
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/** */
	/**
	 * 
	 * @param bArray
	 * @return
	 */
	// public static final String bytesToHexString(byte[] bArray) {
	// StringBuffer sb = new StringBuffer(bArray.length);
	// String sTemp;
	// for (int i = 0; i < bArray.length; i++) {
	// sTemp = Integer.toHexString(0xFF & bArray[i]);
	// if (sTemp.length() < 2)
	// sb.append(0);
	// sb.append(sTemp.toUpperCase());
	// sb.append(" ");
	// }
	// return sb.toString();
	// }

	/** */
	/**
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static final Object bytesToObject(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = new ObjectInputStream(in);
		Object o = oi.readObject();
		oi.close();
		return o;
	}

	/** */
	/**
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public static final byte[] objectToBytes(Serializable s) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream ot = new ObjectOutputStream(out);
		ot.writeObject(s);
		ot.flush();
		ot.close();
		return out.toByteArray();
	}

	public static final String objectToHexString(Serializable s)
			throws IOException {
		return bytesToHexString(objectToBytes(s));
	}

	public static final Object hexStringToObject(String hex)
			throws IOException, ClassNotFoundException {
		return bytesToObject(hexStringToByte(hex));
	}

	/** */
	/**
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
				.toString().substring(1) : temp.toString();
	}

	/**
	 * *
	 *
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;

		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}

		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}

		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;

		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}

			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}

			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	/** */
	/**
	 */
	// public static String BCD2ASC(byte[] bytes) {
	// StringBuffer temp = new StringBuffer(bytes.length * 2);
	//
	// for (int i = 0; i < bytes.length; i++) {
	// int h = ((bytes[i] & 0xf0) >>> 4);
	// int l = (bytes[i] & 0x0f);
	// temp.append(BToA[h]).append( BToA[l]);
	// }
	// return temp.toString() ;
	// }

	/** */
	/**
	 * 
	 * @param origin
	 * @return
	 */
	public static String MD5EncodeToHex(String origin) {
		return bytesToHexString(MD5Encode(origin));
	}

	/** */
	/**
	 * 
	 * @param origin
	 * @return
	 */
	public static byte[] MD5Encode(String origin) {
		return MD5Encode(origin.getBytes());
	}

	/** */
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] MD5Encode(byte[] bytes) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			return md.digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}

	}

	/**
	 * 
	 * @param strNum
	 * @return
	 */
	public static String strNumToBig(String strNum) {
		String str = "";
		// byte[] b = StrTools.hexStringToBytes(strNum);
		long num = Long.parseLong(strNum);
		int len = 0;

		String temp = Long.toHexString(num);
		len = temp.length();
		while (len > 0) {
			if (len < 2) {
				str += "0" + temp.substring(0, len);
				len = 0;
			} else {
				str += temp.substring(len - 2, len);
				len -= 2;
			}
		}
		if (str.length() > 2) {
			if (str.substring(0, 1).equals("0")) {
				str = str.substring(1, str.length());
			}
		}
		return str;
	}

	public static String strNumToHex(String strNum) {
		String str = "";
		// byte[] b = StrTools.hexStringToBytes(strNum);
		long num = Long.parseLong(strNum);
		System.out.println("strNum:" + strNum);
		// System.out.println("");
		System.out.println("num:" + num);
		str = Long.toHexString(num);

		System.out.println("str:" + str);
		return str;
	}

	/**
	 * 
	 * @param strNum
	 * @return
	 */
	public static String strHexNumToStr(String strNum) {
		String str = "";
		// byte[] b = StrTools.hexStringToBytes(strNum);
		long num = Long.parseLong(strNum);
		System.out.println("strNum:" + strNum);
		// System.out.println("");
		System.out.println("num:" + num);
		str = Long.toHexString(num);

		System.out.println("str:" + str);
		return str;
	}

	/**
	 * 0x01 0x02 0x03 ==> "66051"
	 * 
	 * @param
	 * @return
	 */
	public static String byteHexNumToStr(byte[] b) {
		String str = "";
		long val = 0;
		int i;
		for (i = b.length - 1; i >= 0; i--) {
			val += byteToUint(b[i]);
			if (i <= 0) {
				break;
			}
			val *= 256;
		}
		str = val + "";
		System.out.println("b:" + StrTools.bytesToHexString(b));
		System.out.println("str:" + str);
		return str;
	}

	/**
	 * 0x123456789 -->0x0123456789 --> 4886718345
	 * @return
	 */
	public static long StrHexLowToLong(String strVal) {
		long val = 0;
		String str = strVal.replace("0x", " ").trim().toUpperCase();

		while (true) {
			if (str.charAt(0) == '0') {
				str = str.substring(1);
			} else {
				break;
			}
		}
		if (str.length() % 2 == 1) {
			str = "0" + str;
		}
		int len = (str.length() / 2);
		// System.out.println("len: " + len+" str:"+str);
		byte[] result = new byte[len];
		char[] achar = str.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		for (int i = 0; i < result.length; i++) {
			val *= 256;
			val += result[i] & 0xFF;
			// System.out.println("StrHexLowToLong() i:" + i + " ="
			// + (result[i] & 0xFF));

		}
		System.out.println("StrHexHighToLong() strVal:" + strVal + "  to val:"
				+ val);
		return val;
	}

	/**
	 * 0x987654321 -->0x0132547698 --> 5139363480
	 * @return
	 */
	public static long StrHexHighToLong(String strVal) {
		long val = 0;
		int len;
		String tmp;
		String str = strVal.replace("0x", " ").trim().toUpperCase();
//		while (true) {
//			if (str.charAt(0) == '0') {
//				str = str.substring(1);
//			} else {
//				break;
//			}
//		}
		if(str.equals("")){
			return 0;
		}
		len =str.length();
		if (len % 2 == 1) {
			if(len ==1){
				str = "0"+str;
			}else{
				tmp = str.substring(len-1,len);
				str=str.substring(0,len-1);
				str = str+"0"+tmp;
			}
		}
		len = (str.length() / 2);
		System.out.println("len: " + len + " str:" + str);
		byte[] result = new byte[len];
		char[] achar = str.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		for (int i = result.length - 1; i >= 0; i--) {
			val *= 256;
			val += result[i] & 0xFF;
			// System.out.println("StrHexLowToLong() i:" + i + " =" + (result[i]
			// & 0xFF));
		}
		System.out.println("StrHexLowToLong() strVal:" + strVal + "  to val:"
				+ val);
		return val;

	}
	/**
	 * 
	 * @param strNum
	 * @param isBig
	 * @return
	 */
	// public static long strNumToHex(String strNum){
	// String str ="";
	// long num = Long.parseLong(strNum);
	// Long.parseLong(string, radix)
	// int len = 0;
	//
	// String temp=Long.toHexString(num);
	// if(isBig){
	// len = temp.length();
	// while(len>0){
	// if(len<2){
	// str +="0"+temp.substring(0, len);
	// len = 0;
	// }else{
	// str +=temp.substring(len-2, len);
	// len-=2;
	// }
	// }
	// if(str.length()>2){
	// if(str.substring(0, 1).equals("0"));
	// str= str.substring(1, str.length());
	// }
	// }else{
	// str = temp;
	// }
	// num = Long.parseLong(str);
	//
	//
	//
	// return str;
	// }
}
