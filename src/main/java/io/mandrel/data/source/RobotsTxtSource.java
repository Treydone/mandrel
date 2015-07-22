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
package io.mandrel.data.source;

import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.common.robots.RobotsTxtUtils;
import io.mandrel.http.HCRequester;
import io.mandrel.http.Requester;
import io.mandrel.http.WebPage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonProperty;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;

@Slf4j
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class RobotsTxtSource extends Source {

	private static final long serialVersionUID = 7030874477659153772L;

	@JsonProperty("robots_txt")
	private String robotsTxt;

	@Override
	public void register(EntryListener listener) {

		// TODO to be injected?
		Requester requester = new HCRequester();

		// Robots.txt
		ExtendedRobotRules robotRules = RobotsTxtUtils.getRobotRules(robotsTxt);

		// Sitemaps
		if (robotRules != null && robotRules.getSitemaps() != null) {
			robotRules.getSitemaps().forEach(url -> {
				getSitemapsForUrl(url, listener, requester);
			});
		}
	}

	public List<AbstractSiteMap> getSitemapsForUrl(String sitemapUrl, EntryListener listener, Requester requester) {
		List<AbstractSiteMap> sitemaps = new ArrayList<>();

		SiteMapParser siteMapParser = new SiteMapParser();
		try {
			WebPage page = requester.getBlocking(sitemapUrl);
			List<String> headers = page.getMetadata().getHeaders().get(HttpHeaders.CONTENT_TYPE);
			String contentType = headers != null && headers.size() > 0 ? headers.get(0) : "text/xml";

			AbstractSiteMap sitemap = siteMapParser.parseSiteMap(contentType, page.getBody(), new URL(sitemapUrl));

			if (sitemap.isIndex()) {
				SiteMapIndex index = (SiteMapIndex) sitemap;
				if (index.getSitemaps() != null) {
					// Recursive calls
					// TODO manage the infinite loop...
					index.getSitemaps().forEach(s -> getSitemapsForUrl(s.getUrl().toString(), listener, requester));
				}
			} else {
				listener.onItem(sitemap.getUrl().toString());
			}
		} catch (Exception e) {
			log.debug("Dude?", e);
		}
		return sitemaps;
	}

	@Override
	public boolean check() {
		return true;
	}
}
