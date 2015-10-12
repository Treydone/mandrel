package io.mandrel.endpoints.web;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.mandrel.cluster.state.StateService;
import io.mandrel.common.settings.InfoSettings;
import io.mandrel.common.settings.NetworkSettings;

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

	private final static BeansWrapper BEANSWRAPPER = new BeansWrapperBuilder(Configuration.VERSION_2_3_22).build();

	private final StateService stateService;

	private final NetworkSettings networkSettings;

	private final InfoSettings infoSettings;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView != null && modelAndView.getModelMap() != null) {
			modelAndView.getModelMap().addAttribute("clusterTime", stateService.getClusterTime());
			modelAndView.getModelMap().addAttribute("now", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
			modelAndView.getModelMap().addAttribute("statics", BEANSWRAPPER.getStaticModels());

			modelAndView.getModelMap().addAttribute("networkSettings", networkSettings);
			modelAndView.getModelMap().addAttribute("infoSettings", infoSettings);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}
