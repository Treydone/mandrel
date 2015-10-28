import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.parboiled.common.Tuple3;

public class TTTTest {

	@Test
	public void test() {

		List<URI> workers = Arrays.asList(URI.create("uri1"), URI.create("uri2"));
		Stream<Tuple3<Long, Long, URI>> res = workers.stream().flatMap(
				si -> listRunning(si).entrySet().stream().map(entry -> new Tuple3<Long, Long, URI>(entry.getValue(), entry.getKey(), si)));

		Map<Long, Map<Long, List<URI>>> fii = res.collect(Collectors.groupingBy(tuple -> tuple.a,
				Collectors.groupingBy(tuple -> tuple.b, Collectors.mapping(tuple -> tuple.c, Collectors.toList()))));

	}

	private Map<Long, Long> listRunning(URI si) {
		return Collections.singletonMap(1L, 1L);
	}

}
