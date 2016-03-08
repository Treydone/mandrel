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
package io.mandrel.metadata;

import static io.mandrel.requests.FetchStatusCodes.S_BLOCKED_BY_CUSTOM_PROCESSOR;
import static io.mandrel.requests.FetchStatusCodes.S_BLOCKED_BY_USER;
import static io.mandrel.requests.FetchStatusCodes.S_CONNECT_FAILED;
import static io.mandrel.requests.FetchStatusCodes.S_CONNECT_LOST;
import static io.mandrel.requests.FetchStatusCodes.S_DEEMED_CHAFF;
import static io.mandrel.requests.FetchStatusCodes.S_DEFERRED;
import static io.mandrel.requests.FetchStatusCodes.S_DELETED_BY_USER;
import static io.mandrel.requests.FetchStatusCodes.S_DNS_SUCCESS;
import static io.mandrel.requests.FetchStatusCodes.S_DOMAIN_PREREQUISITE_FAILURE;
import static io.mandrel.requests.FetchStatusCodes.S_DOMAIN_UNRESOLVABLE;
import static io.mandrel.requests.FetchStatusCodes.S_OTHER_PREREQUISITE_FAILURE;
import static io.mandrel.requests.FetchStatusCodes.S_OUT_OF_SCOPE;
import static io.mandrel.requests.FetchStatusCodes.S_PREREQUISITE_UNSCHEDULABLE_FAILURE;
import static io.mandrel.requests.FetchStatusCodes.S_PROCESSING_THREAD_KILLED;
import static io.mandrel.requests.FetchStatusCodes.S_ROBOTS_PRECLUDED;
import static io.mandrel.requests.FetchStatusCodes.S_ROBOTS_PREREQUISITE_FAILURE;
import static io.mandrel.requests.FetchStatusCodes.S_RUNTIME_EXCEPTION;
import static io.mandrel.requests.FetchStatusCodes.S_SERIOUS_ERROR;
import static io.mandrel.requests.FetchStatusCodes.S_TIMEOUT;
import static io.mandrel.requests.FetchStatusCodes.S_TOO_MANY_EMBED_HOPS;
import static io.mandrel.requests.FetchStatusCodes.S_TOO_MANY_LINK_HOPS;
import static io.mandrel.requests.FetchStatusCodes.S_TOO_MANY_RETRIES;
import static io.mandrel.requests.FetchStatusCodes.S_UNATTEMPTED;
import static io.mandrel.requests.FetchStatusCodes.S_UNFETCHABLE_URI;
import io.mandrel.common.net.Uri;
import io.mandrel.data.Link;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FetchMetadata implements Serializable {

	private static final long serialVersionUID = 4900693151305950280L;

	private Uri uri;

	private int statusCode;
	private String statusText;
	private LocalDateTime lastCrawlDate;
	private long timeToFetch;
	private Set<Link> outlinks;
	private int iteration;

	public static String fetchStatusCodesToString(int code) {
		switch (code) {
		// DNS
		case S_DNS_SUCCESS:
			return "DNS-1-OK";
			// HTTP Informational 1xx
		case 100:
			return "HTTP-100-Info-Continue";
		case 101:
			return "HTTP-101-Info-Switching Protocols";
			// HTTP Successful 2xx
		case 200:
			return "HTTP-200-Success-OK";
		case 201:
			return "HTTP-201-Success-Created";
		case 202:
			return "HTTP-202-Success-Accepted";
		case 203:
			return "HTTP-203-Success-Non-Authoritative";
		case 204:
			return "HTTP-204-Success-No Content ";
		case 205:
			return "HTTP-205-Success-Reset Content";
		case 206:
			return "HTTP-206-Success-Partial Content";
			// HTTP Redirection 3xx
		case 300:
			return "HTTP-300-Redirect-Multiple Choices";
		case 301:
			return "HTTP-301-Redirect-Moved Permanently";
		case 302:
			return "HTTP-302-Redirect-Found";
		case 303:
			return "HTTP-303-Redirect-See Other";
		case 304:
			return "HTTP-304-Redirect-Not Modified";
		case 305:
			return "HTTP-305-Redirect-Use Proxy";
		case 307:
			return "HTTP-307-Redirect-Temporary Redirect";
			// HTTP Client Error 4xx
		case 400:
			return "HTTP-400-ClientErr-Bad Request";
		case 401:
			return "HTTP-401-ClientErr-Unauthorized";
		case 402:
			return "HTTP-402-ClientErr-Payment Required";
		case 403:
			return "HTTP-403-ClientErr-Forbidden";
		case 404:
			return "HTTP-404-ClientErr-Not Found";
		case 405:
			return "HTTP-405-ClientErr-Method Not Allowed";
		case 407:
			return "HTTP-406-ClientErr-Not Acceptable";
		case 408:
			return "HTTP-407-ClientErr-Proxy Authentication Required";
		case 409:
			return "HTTP-408-ClientErr-Request Timeout";
		case 410:
			return "HTTP-409-ClientErr-Conflict";
		case 406:
			return "HTTP-410-ClientErr-Gone";
		case 411:
			return "HTTP-411-ClientErr-Length Required";
		case 412:
			return "HTTP-412-ClientErr-Precondition Failed";
		case 413:
			return "HTTP-413-ClientErr-Request Entity Too Large";
		case 414:
			return "HTTP-414-ClientErr-Request-URI Too Long";
		case 415:
			return "HTTP-415-ClientErr-Unsupported Media Type";
		case 416:
			return "HTTP-416-ClientErr-Requested Range Not Satisfiable";
		case 417:
			return "HTTP-417-ClientErr-Expectation Failed";
			// HTTP Server Error 5xx
		case 500:
			return "HTTP-500-ServerErr-Internal Server Error";
		case 501:
			return "HTTP-501-ServerErr-Not Implemented";
		case 502:
			return "HTTP-502-ServerErr-Bad Gateway";
		case 503:
			return "HTTP-503-ServerErr-Service Unavailable";
		case 504:
			return "HTTP-504-ServerErr-Gateway Timeout";
		case 505:
			return "HTTP-505-ServerErr-HTTP Version Not Supported";
			// Mandrel internal codes (all negative numbers)
		case S_BLOCKED_BY_USER:
			return "Mandrel(" + S_BLOCKED_BY_USER + ")-Blocked by user";
		case S_BLOCKED_BY_CUSTOM_PROCESSOR:
			return "Mandrel(" + S_BLOCKED_BY_CUSTOM_PROCESSOR + ")-Blocked by custom prefetch processor";
		case S_DELETED_BY_USER:
			return "Mandrel(" + S_DELETED_BY_USER + ")-Deleted by user";
		case S_CONNECT_FAILED:
			return "Mandrel(" + S_CONNECT_FAILED + ")-Connection failed";
		case S_CONNECT_LOST:
			return "Mandrel(" + S_CONNECT_LOST + ")-Connection lost";
		case S_DEEMED_CHAFF:
			return "Mandrel(" + S_DEEMED_CHAFF + ")-Deemed chaff";
		case S_DEFERRED:
			return "Mandrel(" + S_DEFERRED + ")-Deferred";
		case S_DOMAIN_UNRESOLVABLE:
			return "Mandrel(" + S_DOMAIN_UNRESOLVABLE + ")-Domain unresolvable";
		case S_OUT_OF_SCOPE:
			return "Mandrel(" + S_OUT_OF_SCOPE + ")-Out of scope";
		case S_DOMAIN_PREREQUISITE_FAILURE:
			return "Mandrel(" + S_DOMAIN_PREREQUISITE_FAILURE + ")-Domain prerequisite failure";
		case S_ROBOTS_PREREQUISITE_FAILURE:
			return "Mandrel(" + S_ROBOTS_PREREQUISITE_FAILURE + ")-Robots prerequisite failure";
		case S_OTHER_PREREQUISITE_FAILURE:
			return "Mandrel(" + S_OTHER_PREREQUISITE_FAILURE + ")-Other prerequisite failure";
		case S_PREREQUISITE_UNSCHEDULABLE_FAILURE:
			return "Mandrel(" + S_PREREQUISITE_UNSCHEDULABLE_FAILURE + ")-Prerequisite unschedulable failure";
		case S_ROBOTS_PRECLUDED:
			return "Mandrel(" + S_ROBOTS_PRECLUDED + ")-Robots precluded";
		case S_RUNTIME_EXCEPTION:
			return "Mandrel(" + S_RUNTIME_EXCEPTION + ")-Runtime exception";
		case S_SERIOUS_ERROR:
			return "Mandrel(" + S_SERIOUS_ERROR + ")-Serious error";
		case S_TIMEOUT:
			return "Mandrel(" + S_TIMEOUT + ")-Timeout";
		case S_TOO_MANY_EMBED_HOPS:
			return "Mandrel(" + S_TOO_MANY_EMBED_HOPS + ")-Too many embed hops";
		case S_TOO_MANY_LINK_HOPS:
			return "Mandrel(" + S_TOO_MANY_LINK_HOPS + ")-Too many link hops";
		case S_TOO_MANY_RETRIES:
			return "Mandrel(" + S_TOO_MANY_RETRIES + ")-Too many retries";
		case S_UNATTEMPTED:
			return "Mandrel(" + S_UNATTEMPTED + ")-Unattempted";
		case S_UNFETCHABLE_URI:
			return "Mandrel(" + S_UNFETCHABLE_URI + ")-Unfetchable URI";
		case S_PROCESSING_THREAD_KILLED:
			return "Mandrel(" + S_PROCESSING_THREAD_KILLED + ")-" + "Processing thread killed";
			// Unknown return code
		default:
			return Integer.toString(code);
		}
	}
}
