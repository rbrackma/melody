package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessingStartedEvent extends ProcessorEvent {

	public ProcessingStartedEvent(IProcessorManager engine) {
		super(engine);
	}

}