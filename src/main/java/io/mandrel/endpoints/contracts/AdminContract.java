package io.mandrel.endpoints.contracts;

import java.net.URI;

import io.mandrel.endpoints.rest.Apis;
import io.mandrel.timeline.Event;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = Apis.PREFIX + "/admin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public interface AdminContract {

	@RequestMapping(value = "/events/", method = RequestMethod.POST)
	void add(@RequestBody Event event, @RequestHeader URI target);
}
