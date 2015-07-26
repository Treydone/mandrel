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
package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(callSuper = false, exclude = "compiledPattern")
@Accessors(chain = true)
public class UrlPatternFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	public static UrlPatternFilter STATIC = new UrlPatternFilter().setPattern(
			".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$").setInvert(true);

	@JsonIgnore
	private Pattern compiledPattern;
	private String pattern;
	private boolean invert = false;

	public boolean isValid(Link link) {
		if (link == null || StringUtils.isBlank(link.getUri())) {
			return false;
		}
		boolean match = compiledPattern.matcher(link.getUri()).matches();
		return invert ? !match : match;
	}

	public UrlPatternFilter setPattern(String pattern) {
		this.pattern = pattern;
		compiledPattern = Pattern.compile(pattern);
		return this;
	}

	@Override
	public String getType() {
		return "pattern";
	}
}
