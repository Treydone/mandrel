package io.mandrel.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableAsync
@EnableScheduling
@Configuration
public class TaskConfiguration implements AsyncConfigurer, SchedulingConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		return scheduledExecutorService();
	}

	@Bean(destroyMethod = "shutdown")
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(4);
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(new ConcurrentTaskScheduler(scheduledExecutorService()));
	}
}
