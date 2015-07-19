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
package io.mandrel.common.robots;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import crawlercommons.fetcher.http.BaseHttpFetcher;
import crawlercommons.fetcher.http.UserAgent;
import crawlercommons.robots.BaseRobotsParser;
import crawlercommons.robots.RobotUtils;

@Slf4j
public abstract class RobotsTxtUtils {

	public static ExtendedRobotRules getRobotRules(String url) {
		ExtendedRobotRules robotRules = null;
		BaseHttpFetcher fetcher = RobotUtils.createFetcher(new UserAgent("Mandrel", null, null), 1);
		BaseRobotsParser parser = new ExtendedRobotRulesParser();
		URL robotsTxtUrl = null;
		try {
			robotsTxtUrl = new URL(url);
		} catch (MalformedURLException e) {
			log.debug("Can not construct robots.txt url", e);
		}
		if (robotsTxtUrl != null) {
			robotRules = (ExtendedRobotRules) RobotUtils.getRobotRules(fetcher, parser, robotsTxtUrl);
		}
		return robotRules;
	}
}
