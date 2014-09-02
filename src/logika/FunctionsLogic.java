package logika;

import java.nio.ByteBuffer;
import java.util.*;

public class FunctionsLogic {

	static final String RNDSTRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static Random rnd = new Random();
	public final static int MAXDATA = 1471;

	public static String randomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(RNDSTRING.charAt(rnd.nextInt(RNDSTRING.length())));
		return sb.toString();
	}

	public static List<String> fragment(String s) {
		List<String> list = new ArrayList<String>();
		int position = 0;

		while (position < s.length()) {
			list.add(s.substring(
					position,
					(position + MAXDATA) < s.length() ? position + MAXDATA : s
							.length() - 1));
			position += MAXDATA;
		}

		return list;
	}

	public static byte[] createHeader(int ckc, int repeated, int last, int ack) {
		StringBuilder bitString = new StringBuilder();
		bitString.append(Integer.toString(ckc))
				.append(Integer.toString(repeated))
				.append(Integer.toString(last)).append(Integer.toString(ack))
				.append("0000");
		return ByteBuffer.allocate(1)
				.put((byte) Integer.parseInt(bitString.toString(), 2)).array();
	}

	public static String byteToHex(byte b) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[1 * 2];
		int v;
		for (int j = 0; j < 1; j++) {
			v = b & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		
		return new String(hexChars);
	}
}