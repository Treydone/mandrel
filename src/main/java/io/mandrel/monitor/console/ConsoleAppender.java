package io.mandrel.monitor.console;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleAppender extends AppenderBase<ILoggingEvent> {

	private SimpMessageSendingOperations messagingTemplate;

	private PatternLayout layout;
	private String pattern;
	private boolean outputPatternAsHeader;

	@Override
	public void start() {
		PatternLayout patternLayout = new PatternLayout();
		patternLayout.setContext(context);
		patternLayout.setPattern(getPattern());
		patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
		patternLayout.start();
		this.layout = patternLayout;
		super.start();
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}

		messagingTemplate.convertAndSend("/topic/tail", layout.doLayout(event));
	}
}
