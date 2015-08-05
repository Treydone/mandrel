package io.mandrel.endpoints.web;

import java.time.LocalDateTime;

import io.mandrel.common.MandrelIllegalArgumentException;
import io.mandrel.common.NotFoundException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ErrorsController {

	private final ErrorAttributes errorAttributes;

	@RequestMapping("/error")
	public String error(Model model, HttpServletRequest request) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		model.addAttribute("path", request.getContextPath());
		model.addAttribute("time", LocalDateTime.now());
		model.addAllAttributes(errorAttributes.getErrorAttributes(requestAttributes, true));
		return "views/error";
	}

	@RequestMapping("/500/test")
	public String exemple500() {
		throw new MandrelIllegalArgumentException("This exception have to be shown only for admin!");
	}

	@RequestMapping("/404/test")
	public String exemple404() {
		throw new NotFoundException("This exception have to be shown only for admin!");
	}
}
