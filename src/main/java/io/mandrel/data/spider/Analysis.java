package io.mandrel.data.spider;

import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.gateway.Document;
import io.mandrel.http.Metadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import crawlercommons.sitemaps.AbstractSiteMap;

@Data
public class Analysis {

	private Map<String, List<Document>> documents;
	private Map<String, Set<Link>> outlinks;
	private Map<String, Set<String>> filteredOutlinks;
	private Metadata metadata;
	private Map<String, List<AbstractSiteMap>> sitemaps;
	private ExtendedRobotRules robotRules;
}
