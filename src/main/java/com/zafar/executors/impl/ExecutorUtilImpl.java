package com.zafar.executors.impl;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import rx.Scheduler;
import rx.schedulers.Schedulers;

import com.zafar.deepq.util.DeepQueueThreadFactory;
import com.zafar.executors.ExecutorUtil;

@Service
public class ExecutorUtilImpl implements ExecutorUtil{
	
	private Scheduler readScheduler;

	@Value("${read.thread.pool.size:50}")
	private int readThreads;

	private Scheduler writeScheduler;
	
	@Value("${write.thread.pool.size:50}")
	private int writeThreads; 
	
	private Scheduler cleanupScheduler;
	
	@Value("${cleanup.thread.pool.size:50}")
	private int cleanupThreads; 
	
	@PostConstruct
	public void init(){
		readScheduler=Schedulers.from(Executors.newFixedThreadPool(readThreads,new DeepQueueThreadFactory("Read-pool")));
		writeScheduler=Schedulers.from(Executors.newFixedThreadPool(writeThreads,new DeepQueueThreadFactory("write-pool")));
		cleanupScheduler=Schedulers.from(Executors.newFixedThreadPool(cleanupThreads,new DeepQueueThreadFactory("Cleanup-pool")));
	}
	public Scheduler getReadScheduler() {
		return readScheduler;
	}
	@Override
	public Scheduler getReadExecutors() {
		return readScheduler;
	}
	@Override
	public Scheduler getWriteExecutors() {
		return writeScheduler;
	}
	@Override
	public Scheduler getCleanupExecutors() {
		return cleanupScheduler;
	}

}
