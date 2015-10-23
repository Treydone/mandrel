import io.mandrel.common.data.Spider;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeMemoryOutput;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Pouet {

	@Test
	public void test() throws JsonProcessingException {

		StopWatch watch = new StopWatch();
		watch.start();
		ObjectMapper mapper = new ObjectMapper();
		for (int i = 0; i < 100000; i++) {
			mapper.writeValueAsBytes(new Spider());
		}
		watch.stop();
		System.err.println(watch.getTime());
	}

	@Test
	public void test2() throws JsonProcessingException {

		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				kryo.register(Spider.class, 0);
				return kryo;
			}
		};
		KryoPool pool = new KryoPool.Builder(factory).softReferences().build();
		Kryo kryo = pool.borrow();

		StopWatch watch = new StopWatch();
		watch.start();

		for (int i = 0; i < 100000; i++) {
			UnsafeMemoryOutput output = new UnsafeMemoryOutput(16384);
			kryo.writeObject(output, new Spider());
			output.close();
		}
		watch.stop();

		pool.release(kryo);
		System.err.println(watch.getTime());
	}

}
