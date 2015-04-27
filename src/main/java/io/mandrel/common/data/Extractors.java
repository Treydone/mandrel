package io.mandrel.common.data;

import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.WebPageExtractor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Extractors implements Serializable {

	private static final long serialVersionUID = -8343604385890261256L;

	@JsonProperty("pages")
	private List<WebPageExtractor> pages;

	@JsonProperty("outlinks")
	private List<OutlinkExtractor> outlinks = Arrays.asList(new OutlinkExtractor(Constants._DEFAULT_OUTLINKS_EXTRATOR));
}