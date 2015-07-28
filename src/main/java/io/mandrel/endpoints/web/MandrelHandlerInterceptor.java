package io.mandrel.endpoints.web;

import io.mandrel.cluster.state.StateService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MandrelHandlerInterceptor implements HandlerInterceptor {

	private final static BeansWrapper BEANSWRAPPER = new BeansWrapperBuilder(Configuration.VERSION_2_3_23).build();

	private final StateService stateService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		modelAndView.getModelMap().addAttribute("clusterTime", stateService.getClusterTime());
		modelAndView.getModelMap().addAttribute("statics", BEANSWRAPPER.getStaticModels());
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}
