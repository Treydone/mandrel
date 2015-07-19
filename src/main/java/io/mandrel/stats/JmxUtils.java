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
package io.mandrel.stats;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class JmxUtils {

	private static final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

	private static final Method getMaxFileDescriptorCountField;
	private static final Method getOpenFileDescriptorCountField;

	static {
		Method method = null;
		try {
			method = osMxBean.getClass().getDeclaredMethod("getMaxFileDescriptorCount");
			method.setAccessible(true);
		} catch (Exception e) {
			// not available
		}
		getMaxFileDescriptorCountField = method;

		method = null;
		try {
			method = osMxBean.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
			method.setAccessible(true);
		} catch (Exception e) {
			// not available
		}
		getOpenFileDescriptorCountField = method;
	}

	public static long getMaxFileDescriptorCount() {
		if (getMaxFileDescriptorCountField == null) {
			return -1;
		}
		try {
			return (Long) getMaxFileDescriptorCountField.invoke(osMxBean);
		} catch (Exception e) {
			return -1;
		}
	}

	public static long getOpenFileDescriptorCount() {
		if (getOpenFileDescriptorCountField == null) {
			return -1;
		}
		try {
			return (Long) getOpenFileDescriptorCountField.invoke(osMxBean);
		} catch (Exception e) {
			return -1;
		}
	}
}
