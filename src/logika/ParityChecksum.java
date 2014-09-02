package logika;

import java.util.zip.Checksum;

public class ParityChecksum implements Checksum {

	private static ParityChecksum parityChecksummer;

	private ParityChecksum(){		
	}
	
	public static ParityChecksum getSingletonObject() {
		if (parityChecksummer == null)
			parityChecksummer = new ParityChecksum();
		return parityChecksummer;
	}

	long checksum = 0;

	public void update(int b) {
		int numOneBits = 0;
		for (int i = 1; i < 256; i *= 2) {
			if ((b & i) != 0)
				numOneBits++;
		}
		checksum = (checksum + numOneBits) % 2;
	}

	public void update(byte data[], int offset, int length) {
		checksum = 0;
		for (int i = offset; i < offset + length; i++) {
			this.update(data[i]);
		}
	}

	public long getValue() {
		return checksum;
	}

	public void reset() {
		checksum = 0;
	}
}