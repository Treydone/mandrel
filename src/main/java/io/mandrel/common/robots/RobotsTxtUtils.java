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
