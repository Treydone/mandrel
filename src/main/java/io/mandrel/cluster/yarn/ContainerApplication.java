package io.mandrel.cluster.yarn;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.stereotype.Component;
import org.springframework.yarn.boot.condition.ConditionalOnYarnContainer;

@EnableAutoConfiguration
public class ContainerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContainerApplication.class, args);
	}

	@Component
	@Slf4j
	public static class HelloPojo {

		@Autowired
		private Configuration configuration;

		@ConditionalOnYarnContainer
		public void publicVoidNoArgsMethod() throws IOException {
			log.info("Hello from HelloPojo");
			log.info("About to list from hdfs root content");
			try (FsShell shell = new FsShell(configuration)) {
				for (FileStatus s : shell.ls(false, "/")) {
					log.info(s.toString());
				}
			}
		}
	}
}
