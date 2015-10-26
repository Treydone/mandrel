package io.mandrel.endpoints.contracts;

import io.mandrel.endpoints.rest.Apis;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = Apis.PREFIX + "/controllers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public interface ControllerContract {

	@RequestMapping(value = "/create")
	public Long create(@PathVariable Long id);

	@RequestMapping(value = "/{id}/start")
	public void start(@PathVariable Long id);

	@RequestMapping(value = "/{id}/pause")
	public void pause(@PathVariable Long id);

	@RequestMapping(value = "/{id}/kill")
	public void kill(@PathVariable Long id);
}
