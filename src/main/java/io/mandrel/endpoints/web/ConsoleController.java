package io.mandrel.endpoints.web;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/console")
@Controller
public class ConsoleController {

	@RequestMapping
	public String console(Map<String, Object> model) {
		model.put("time", new Date());
		model.put("message", "Hello World");
		model.put("title", "Hello App");
		return "console";
	}
}
