package io.mandrel.endpoints.contracts;

import io.mandrel.common.data.Spider;
import io.mandrel.endpoints.rest.Apis;
import io.mandrel.frontier.Frontier;

import java.net.URI;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = Apis.PREFIX + "/frontiers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public interface FrontierContract {

	@RequestMapping(value = "/{id}")
	public Frontier id(@PathVariable Long id, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/next")
	public URI next(@PathVariable Long id, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/delete")
	public void delete(@PathVariable Long id, URI uri, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/schedule")
	public void schedule(@PathVariable Long id, URI uri, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/schedule")
	public void schedule(@PathVariable Long id, Set<URI> uri, @RequestHeader URI target);

	@RequestMapping(value = "/create")
	public void create(@RequestBody Spider spider, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/start")
	public void start(@PathVariable Long id, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/pause")
	public void pause(@PathVariable Long id, @RequestHeader URI target);

	@RequestMapping(value = "/{id}/kill")
	public void kill(@PathVariable Long id, @RequestHeader URI target);
}
