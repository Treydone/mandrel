package io.mandrel.common.data;

import io.mandrel.common.content.OutlinkExtractor;
import io.mandrel.common.content.WebPageExtractor;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Extractors {

	@JsonProperty("pages")
	private List<WebPageExtractor> pages;

	@JsonProperty("outlinks")
	private List<OutlinkExtractor> outlinks = Arrays.asList(new OutlinkExtractor("_default"));
}