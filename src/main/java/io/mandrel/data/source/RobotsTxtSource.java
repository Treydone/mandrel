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

import io.mandrel.blob.Blob;
import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.common.robots.RobotsTxtUtils;
import io.mandrel.requests.Requester;
import io.mandrel.requests.http.ApacheHttpRequester;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.servo.util.Throwables;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;

@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class RobotsTxtSource extends Source {

	private static final long serialVersionUID = 7030874477659153772L;

	@JsonProperty("robots_txt")
	private String robotsTxt;

	@JsonProperty("max_depth")
	private int maxDepth = 2;

	@Override
	public void register(EntryListener listener) {

		// TODO to be injected?
		Requester requester = new ApacheHttpRequester();

		// Robots.txt
		ExtendedRobotRules robotRules;

		try {
			robotRules = RobotsTxtUtils.getRobotRules(robotsTxt);
		} catch (Exception e) {
			log.warn("Can not get robots.txt rules {}", new Object[] { robotsTxt }, e);
			return;
		}

		// Sitemaps
		if (robotRules != null && robotRules.getSitemaps() != null) {
			robotRules.getSitemaps().forEach(url -> {
				getSitemapsForUrl(url, listener, requester, 0);
			});
		}
	}

	public List<AbstractSiteMap> getSitemapsForUrl(String sitemapUrl, EntryListener listener, Requester requester, int depth) {
		List<AbstractSiteMap> sitemaps = new ArrayList<>();

		SiteMapParser siteMapParser = new SiteMapParser();

		Blob blob;
		try {
			blob = requester.getBlocking(new URI(sitemapUrl));
		} catch (Exception e) {
			log.warn("Can not get the sitemap {}", new Object[] { sitemapUrl }, e);
			throw Throwables.propagate(e);
		}

		try {
			String contentType = blob.metadata().contentMetadata().contentType() != null ? blob.metadata().contentMetadata().contentType() : "text/xml";

			AbstractSiteMap sitemap = siteMapParser.parseSiteMap(contentType, IOUtils.toByteArray(blob.payload().openStream()), new URL(sitemapUrl));

			if (sitemap.isIndex()) {
				SiteMapIndex index = (SiteMapIndex) sitemap;
				if (index.getSitemaps() != null && depth < maxDepth) {
					int newDepth = depth++;
					index.getSitemaps().forEach(s -> getSitemapsForUrl(s.getUrl().toString(), listener, requester, newDepth));
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

	public String name() {
		return "robots.txt";
	}
}
