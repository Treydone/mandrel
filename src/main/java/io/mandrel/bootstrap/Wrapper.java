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
package io.mandrel.bootstrap;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class Wrapper implements WrapperListener {

	private Application application;
	
	public static void main(String[] args) {
		WrapperManager.start(new Wrapper(), args);
	}

	@Override
	public void controlEvent(int event) {

	}

	@Override
	public Integer start(String[] args) {
		WrapperManager.signalStarting(20000);
		
		application = new Application();
		application.start(args);
		return null;
	}

	@Override
	public int stop(int exitCode) {
		WrapperManager.signalStopping(20000);
		application.stop();
		return exitCode;
	}
}
