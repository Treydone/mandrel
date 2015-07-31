package io.mandrel.common.unit;

import io.mandrel.common.MandrelIllegalArgumentException;

import java.io.Serializable;

public class ByteSizeValue implements Serializable {

	private static final long serialVersionUID = 2684458824218492593L;

	private long size;

	private ByteSizeUnit sizeUnit;

	public ByteSizeValue(long bytes) {
		this(bytes, ByteSizeUnit.BYTES);
	}

	public ByteSizeValue(long size, ByteSizeUnit sizeUnit) {
		this.size = size;
		this.sizeUnit = sizeUnit;
	}

	public int bytesAsInt() throws MandrelIllegalArgumentException {
		long bytes = bytes();
		if (bytes > Integer.MAX_VALUE) {
			throw new MandrelIllegalArgumentException("size [" + toString() + "] is bigger than max int");
		}
		return (int) bytes;
	}

	public long bytes() {
		return sizeUnit.toBytes(size);
	}

	public long getBytes() {
		return bytes();
	}

	public long kb() {
		return sizeUnit.toKB(size);
	}

	public long getKb() {
		return kb();
	}

	public long mb() {
		return sizeUnit.toMB(size);
	}

	public long getMb() {
		return mb();
	}

	public long gb() {
		return sizeUnit.toGB(size);
	}

	public long getGb() {
		return gb();
	}

	public long tb() {
		return sizeUnit.toTB(size);
	}

	public long getTb() {
		return tb();
	}

	public long pb() {
		return sizeUnit.toPB(size);
	}

	public long getPb() {
		return pb();
	}

	public double kbFrac() {
		return ((double) bytes()) / ByteSizeUnit.C1;
	}

	public double getKbFrac() {
		return kbFrac();
	}

	public double mbFrac() {
		return ((double) bytes()) / ByteSizeUnit.C2;
	}

	public double getMbFrac() {
		return mbFrac();
	}

	public double gbFrac() {
		return ((double) bytes()) / ByteSizeUnit.C3;
	}

	public double getGbFrac() {
		return gbFrac();
	}

	public double tbFrac() {
		return ((double) bytes()) / ByteSizeUnit.C4;
	}

	public double getTbFrac() {
		return tbFrac();
	}

	public double pbFrac() {
		return ((double) bytes()) / ByteSizeUnit.C5;
	}

	public double getPbFrac() {
		return pbFrac();
	}

	@Override
	public String toString() {
		return getPrintableValue();
	}

	public String getPrintableValue() {
		long bytes = bytes();
		double value = bytes;
		String suffix = "b";
		if (bytes >= ByteSizeUnit.C5) {
			value = pbFrac();
			suffix = "pb";
		} else if (bytes >= ByteSizeUnit.C4) {
			value = tbFrac();
			suffix = "tb";
		} else if (bytes >= ByteSizeUnit.C3) {
			value = gbFrac();
			suffix = "gb";
		} else if (bytes >= ByteSizeUnit.C2) {
			value = mbFrac();
			suffix = "mb";
		} else if (bytes >= ByteSizeUnit.C1) {
			value = kbFrac();
			suffix = "kb";
		}
		return format1Decimals(value, suffix);
	}

	/**
	 * Format the double value with a single decimal points, trimming trailing
	 * '.0'.
	 */
	public static String format1Decimals(double value, String suffix) {
		String p = String.valueOf(value);
		int ix = p.indexOf('.') + 1;
		int ex = p.indexOf('E');
		char fraction = p.charAt(ix);
		if (fraction == '0') {
			if (ex != -1) {
				return p.substring(0, ix - 1) + p.substring(ex) + suffix;
			} else {
				return p.substring(0, ix - 1) + suffix;
			}
		} else {
			if (ex != -1) {
				return p.substring(0, ix) + fraction + p.substring(ex) + suffix;
			} else {
				return p.substring(0, ix) + fraction + suffix;
			}
		}
	}

	public static String print(long bytes) {
		return new ByteSizeValue(bytes).getPrintableValue();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ByteSizeValue sizeValue = (ByteSizeValue) o;

		if (size != sizeValue.size)
			return false;
		if (sizeUnit != sizeValue.sizeUnit)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (size ^ (size >>> 32));
		result = 31 * result + (sizeUnit != null ? sizeUnit.hashCode() : 0);
		return result;
	}
}