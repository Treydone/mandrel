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
