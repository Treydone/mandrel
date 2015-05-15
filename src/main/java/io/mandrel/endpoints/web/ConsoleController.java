package io.mandrel.endpoints.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/console")
@Controller
public class ConsoleController {

	@RequestMapping
	public String console(Map<String, Object> model) {
		return "console";
	}
}
