package io.mandrel.data.spider;

import io.mandrel.gateway.Document;
import io.mandrel.http.Metadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class Analysis {

	private Map<String, List<Document>> documents;
	private Map<String, Set<Link>> outlinks;
	private Map<String, Set<String>> filteredOutlinks;
	private Metadata metadata;
}
