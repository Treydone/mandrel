package io.mandrel.cluster.yarn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.yarn.client.YarnClient;

@EnableAutoConfiguration
public class ClientApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);
		YarnClient yarnClient = context.getBean(YarnClient.class);

		// -- start
		yarnClient.submitApplication();

		// -- status
		// ApplicationReport report = yarnClient.getApplicationReport(null);
		// report.getProgress();

		// -- stop
		// yarnClient.killApplication(arg0);
	}
}