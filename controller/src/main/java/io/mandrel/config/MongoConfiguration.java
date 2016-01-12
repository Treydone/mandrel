/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoConfiguration {

	// @Autowired
	// private MongoProperties properties;

	// @Bean
	// public MongoClientOptions options() {
	// CodecRegistry codecRegistry =
	// CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
	// CodecRegistries.fromCodecs(new URICodec()
	// // , new LocalDateTimeCodec()
	// ));
	// return MongoClientOptions.builder().codecRegistry(codecRegistry).build();
	// }

	// @Bean
	// public MongoClient mongo() throws UnknownHostException {
	// return new MongoClient(new ServerAddress(properties.getHost(),
	// properties.getPort()), options());
	// }
}
