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

import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import crawlercommons.robots.BaseRobotRules;

/**
 * Result from parsing a single robots.txt file - which means we
 * get a set of rules, and a crawl-delay.
 */

@SuppressWarnings("serial")
public class ExtendedRobotRules extends BaseRobotRules {

    public enum RobotRulesMode {
        ALLOW_ALL,
        ALLOW_NONE,
        ALLOW_SOME
    }

    /**
     * Single rule that maps from a path prefix to an allow flag.
     */
    @AllArgsConstructor
    @Data
    protected class RobotRule implements Comparable<RobotRule>, Serializable {
        private final String prefix;
        private final boolean allow;

        // Sort from longest to shortest rules.
        @Override
        public int compareTo(RobotRule o) {
            if (prefix.length() < o.prefix.length()) {
                return 1;
            } else if (prefix.length() > o.prefix.length()) {
                return -1;
            } else if (allow == o.allow) {
                return 0;
            } else if (allow) {
                // Allow comes before disallow
                return -1;
            } else {
                return 1;
            }
        }

    }


    private List<RobotRule> rules;
    private RobotRulesMode mode;
    
    public ExtendedRobotRules() {
        this(RobotRulesMode.ALLOW_SOME);
    }
    
    public ExtendedRobotRules(RobotRulesMode mode) {
        super();
        
        this.mode = mode;
        this.rules = new ArrayList<RobotRule>();
    }
    
    public List<RobotRule> getRules() {
		return rules;
	}

	public RobotRulesMode getMode() {
		return mode;
	}

	public void clearRules() {
        rules.clear();
    }

    public void addRule(String prefix, boolean allow) {
        // Convert old-style case of disallow: <nothing>
        // into new allow: <nothing>.
        if (!allow && (prefix.length() == 0)) {
            allow = true;
        }

        rules.add(new RobotRule(prefix, allow));
    }

    public boolean isAllowed(String url) {
        if (mode == RobotRulesMode.ALLOW_NONE) {
            return false;
        } else if (mode == RobotRulesMode.ALLOW_ALL) {
            return true;
        } else {
            String pathWithQuery = getPath(url, true);

            // Always allow robots.txt
            if (pathWithQuery.equals("/robots.txt")) {
                return true;
            }

            for (RobotRule rule : rules) {
                if (ruleMatches(pathWithQuery, rule.prefix)){
                    return rule.allow;
                }
            }

            return true;
        }
    }

    private String getPath(String url, boolean getWithQuery) {

        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            String query = urlObj.getQuery();
            if (getWithQuery && query != null){
                path += "?" + query;
            }
            
            if ((path == null) || (path.equals(""))) {
                return "/";
            } else {
                // We used to lower-case the path, but Google says we need to do case-sensitive matching.
                return URLDecoder.decode(path, "UTF-8");
            }
        } catch (Exception e) {
            // If the URL is invalid, we don't really care since the fetch
            // will fail, so return the root.
            return "/";
        }
    }

    private boolean ruleMatches(String text, String pattern) {
        int patternPos = 0;
        int textPos = 0;
        
        int patternEnd = pattern.length();
        int textEnd = text.length();
        
        boolean containsEndChar = pattern.endsWith("$");
        if (containsEndChar) {
            patternEnd -= 1;
        }
        
        while ((patternPos < patternEnd) && (textPos < textEnd)) {
            // Find next wildcard in the pattern.
            int wildcardPos = pattern.indexOf('*', patternPos);
            if (wildcardPos == -1) {
                wildcardPos = patternEnd;
            }
            
            // If we're at a wildcard in the pattern, find the place in the text
            // where the character(s) after the wildcard match up with what's in
            // the text.
            if (wildcardPos == patternPos) {
                patternPos += 1;
                if (patternPos >= patternEnd) {
                    // Pattern ends with '*', we're all good.
                    return true;
                }

                // TODO - don't worry about having two '*' in a row?
                
                // Find the end of the pattern piece we need to match.
                int patternPieceEnd = pattern.indexOf('*', patternPos);
                if (patternPieceEnd == -1) {
                    patternPieceEnd = patternEnd;
                }
                
                boolean matched = false;
                int patternPieceLen = patternPieceEnd - patternPos;
                while ((textPos + patternPieceLen <= textEnd) && !matched) {
                    // See if patternPieceLen chars from text at textPos match chars from pattern at patternPos
                    matched = true;
                    for (int i = 0; i < patternPieceLen && matched; i++) {
                        if (text.charAt(textPos + i) != pattern.charAt(patternPos + i)) {
                            matched = false;
                        }
                    }
                    
                    // If we matched, we're all set, otherwise we have to advance textPos
                    if (!matched) {
                        textPos += 1;
                    }
                }
                
                // If we matched, we're all set, otherwise we failed
                if (!matched) {
                    return false;
                }
            } else {
                // See if the pattern from patternPos to wildcardPos matches the text
                // starting at textPos
                while ((patternPos < wildcardPos) && (textPos < textEnd)) {
                    if (text.charAt(textPos++) != pattern.charAt(patternPos++)) {
                        return false;
                    }
                }
            }
        }
        
        // If we didn't reach the end of the pattern, make sure we're not at a wildcard, sa
        // that's a 0 or more match, so then we're still OK.
        while ((patternPos < patternEnd) && (pattern.charAt(patternPos) == '*')) {
            patternPos += 1;
        }
        
        // We're at the end, so we have a match if the pattern was completely consumed,
        // and either we consumed all the text or we didn't have to match it all (no '$' at end
        // of the pattern)
        return (patternPos == patternEnd) && ((textPos == textEnd) || !containsEndChar);
    }

    /**
     * In order to match up with Google's convention, we want to match rules from longest to shortest.
     * So sort the rules.
     */
    public void sortRules() {
        Collections.sort(rules);
    }
    
    /**
     * Is our ruleset set up to allow all access?
     * 
     * @return true if all URLs are allowed.
     */
    @Override
    public boolean isAllowAll() {
        return mode == RobotRulesMode.ALLOW_ALL;
    }

    /**
     * Is our ruleset set up to disallow all access?
     * 
     * @return true if no URLs are allowed.
     */
    @Override
    public boolean isAllowNone() {
        return mode == RobotRulesMode.ALLOW_NONE;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((rules == null) ? 0 : rules.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtendedRobotRules other = (ExtendedRobotRules) obj;
        if (mode != other.mode)
            return false;
        if (rules == null) {
            if (other.rules != null)
                return false;
        } else if (!rules.equals(other.rules))
            return false;
        return true;
    }
    
}
