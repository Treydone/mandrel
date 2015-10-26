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
package io.mandrel.endpoints.rest;

import io.mandrel.controller.ControllerContainers;
import io.mandrel.endpoints.contracts.ControllerContract;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ControllerResource implements ControllerContract {

	@Override
	public Long create(Long id) {
		return null;
	}

	@Override
	public void start(Long id) {
		ControllerContainers.get(id).ifPresent(c -> c.start());
	}

	@Override
	public void pause(Long id) {
		ControllerContainers.get(id).ifPresent(c -> c.pause());
	}

	@Override
	public void kill(Long id) {
		ControllerContainers.get(id).ifPresent(c -> c.kill());
	}
}
