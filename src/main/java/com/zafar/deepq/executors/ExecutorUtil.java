package com.zafar.deepq.executors;

import rx.Scheduler;

public interface ExecutorUtil {

	public Scheduler getReadExecutors();

	public Scheduler getWriteExecutors();

	public Scheduler getCleanupExecutors();
}
