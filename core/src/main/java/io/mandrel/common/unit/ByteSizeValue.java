/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.common.unit;

import io.mandrel.common.MandrelIllegalArgumentException;
import io.mandrel.common.MandrelParseException;
import io.mandrel.common.unit.ByteSizeValue.ByteSizeValueDeserializer;
import io.mandrel.common.unit.ByteSizeValue.ByteSizeValueSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = ByteSizeValueDeserializer.class)
@JsonSerialize(using = ByteSizeValueSerializer.class)
@ThriftStruct
public class ByteSizeValue implements Serializable {

	private static final long serialVersionUID = 2684458824218492593L;

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private long size;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private ByteSizeUnit sizeUnit;

	@Deprecated
	public ByteSizeValue() {
	}

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

	public static ByteSizeValue parseBytesSizeValue(String sValue) throws MandrelParseException {
		long bytes;
		try {
			String lowerSValue = sValue.toLowerCase(Locale.ROOT).trim();
			if (lowerSValue.endsWith("k")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 1)) * ByteSizeUnit.C1);
			} else if (lowerSValue.endsWith("kb")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 2)) * ByteSizeUnit.C1);
			} else if (lowerSValue.endsWith("m")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 1)) * ByteSizeUnit.C2);
			} else if (lowerSValue.endsWith("mb")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 2)) * ByteSizeUnit.C2);
			} else if (lowerSValue.endsWith("g")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 1)) * ByteSizeUnit.C3);
			} else if (lowerSValue.endsWith("gb")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 2)) * ByteSizeUnit.C3);
			} else if (lowerSValue.endsWith("t")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 1)) * ByteSizeUnit.C4);
			} else if (lowerSValue.endsWith("tb")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 2)) * ByteSizeUnit.C4);
			} else if (lowerSValue.endsWith("p")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 1)) * ByteSizeUnit.C5);
			} else if (lowerSValue.endsWith("pb")) {
				bytes = (long) (Double.parseDouble(lowerSValue.substring(0, lowerSValue.length() - 2)) * ByteSizeUnit.C5);
			} else if (lowerSValue.endsWith("b")) {
				bytes = Long.parseLong(lowerSValue.substring(0, lowerSValue.length() - 1).trim());
			} else if (lowerSValue.equals("-1")) {
				// Allow this special value to be unit-less:
				bytes = -1;
			} else if (lowerSValue.equals("0")) {
				// Allow this special value to be unit-less:
				bytes = 0;
			} else {
				// Missing units:
				bytes = Long.parseLong(lowerSValue.trim());
			}
		} catch (NumberFormatException e) {
			throw new MandrelParseException("failed to parse [{}]", e, sValue);
		}
		return new ByteSizeValue(bytes, ByteSizeUnit.BYTES);
	}

	public static class ByteSizeValueDeserializer extends JsonDeserializer<ByteSizeValue> {
		@Override
		public ByteSizeValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			String value = jp.getCodec().readValue(jp, String.class);
			return ByteSizeValue.parseBytesSizeValue(value);
		}
	}

	public static class ByteSizeValueSerializer extends JsonSerializer<ByteSizeValue> {
		@Override
		public void serialize(ByteSizeValue value, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
			jgen.writeString(value.toString());
		}
	}
}