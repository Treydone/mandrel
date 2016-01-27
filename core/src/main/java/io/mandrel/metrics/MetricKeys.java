package io.mandrel.metrics;

public interface MetricKeys {

	String METRIC_DELIM = ".";
	String TYPE_DELIM = "_";

	String GLOBAL = "global";
	String NODE = "node";
	String SPIDER = "spider";

	String NB_PAGES_TOTAL = "nbPagesTotal";
	String TOTAL_SIZE_TOTAL = "totalSizeTotal";
	String STATUSES = "statuses";
	String HOSTS = "hosts";
	String CONTENT_TYPES = "contentTypes";
	String EXTRACTORS = "extractors";

	// GLOBAL
	public static String global() {
		return GLOBAL;
	}

	public static String globalNbPages() {
		return GLOBAL + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String globalTotalSize() {
		return GLOBAL + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String globalPageForStatus(int httpStatus) {
		return GLOBAL + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String globalPageForHost(String host) {
		return GLOBAL + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String globalPageForContentType(String contentType) {
		return GLOBAL + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	// NODE
	public static String node(String node) {
		return NODE + TYPE_DELIM + node;
	}

	public static String nodeNbPages(String node) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String nodeTotalSize(String node) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String nodePageForStatus(String node, int httpStatus) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String nodePageForHost(String node, String host) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String nodePageForContentType(String node, String contentType) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	// SPIDER
	public static String spider(long spiderId) {
		return SPIDER + TYPE_DELIM + spiderId;
	}

	public static String spiderNbPages(long spiderId) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String spiderTotalSize(long spiderId) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String spiderPageForStatus(long spiderId, int httpStatus) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String spiderPageForHost(long spiderId, String host) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String spiderPageForContentType(long spiderId, String contentType) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	public static String spiderPageForExtractor(long spiderId, String extractor) {
		return SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + EXTRACTORS + METRIC_DELIM + extractor;
	}
}
