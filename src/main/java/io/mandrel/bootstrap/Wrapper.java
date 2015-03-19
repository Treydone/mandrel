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
