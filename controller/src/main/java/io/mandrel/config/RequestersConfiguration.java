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

import io.mandrel.requests.Requesters;
import io.mandrel.requests.ftp.FtpRequester;
import io.mandrel.requests.ftp.FtpRequester.FtpRequesterDefinition;
import io.mandrel.requests.http.ApacheHttpRequester;
import io.mandrel.requests.http.ApacheHttpRequester.ApacheHttpRequesterDefinition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestersConfiguration {

	@Bean
	public ApacheHttpRequester defaultHttpRequester() {
		ApacheHttpRequester hcRequester = new ApacheHttpRequesterDefinition().build(null);
		hcRequester.init();
		Requesters.add(hcRequester);
		return hcRequester;
	}

	@Bean
	public FtpRequester defaultFtpRequester() {
		FtpRequester ftpRequester = new FtpRequesterDefinition().build(null);
		ftpRequester.init();
		Requesters.add(ftpRequester);
		return ftpRequester;
	}
}
