package io.mandrel.common.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

@AllArgsConstructor
public class KryoSerializer<T> implements StreamSerializer<T> {

	private final CompressionType type;

	private final Class<T> clazz;

	private final KryoPool pool;

	@Getter
	private final int typeId;

	public void write(ObjectDataOutput objectDataOutput, T data) throws IOException {
		Kryo kryo = pool.borrow();
		try {
			switch (type) {
			case DEFLATE: {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16384);
				DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
				Output output = new Output(deflaterOutputStream);
				kryo.writeObject(output, data);
				output.close();

				byte[] bytes = byteArrayOutputStream.toByteArray();
				objectDataOutput.write(bytes);
			}
				break;
			case SNAPPY: {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16384);
				SnappyOutputStream snappyOutputStream = new SnappyOutputStream(byteArrayOutputStream);
				Output output = new Output(snappyOutputStream);
				kryo.writeObject(output, data);
				output.close();

				byte[] bytes = byteArrayOutputStream.toByteArray();
				objectDataOutput.write(bytes);
			}
				break;
			case NONE: {
				Output output = new Output((OutputStream) objectDataOutput);
				kryo.writeObject(output, data);
				output.flush();
			}
				break;
			}
		} finally {
			pool.release(kryo);
		}
	}

	public T read(ObjectDataInput objectDataInput) throws IOException {
		Kryo kryo = pool.borrow();
		try {
			InputStream in = (InputStream) objectDataInput;
			switch (type) {
			case DEFLATE:
				in = new InflaterInputStream(in);
				break;
			case SNAPPY:
				in = new SnappyInputStream(in);
				break;
			case NONE:
				break;
			}

			Input input = new Input(in);
			return kryo.readObject(input, clazz);
		} finally {
			pool.release(kryo);
		}
	}

	public void destroy() {
	}
}
