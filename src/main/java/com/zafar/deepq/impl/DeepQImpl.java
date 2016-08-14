package com.zafar.deepq.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import rx.Observable;

import com.zafar.deepq.DeepQ;
import com.zafar.deepq.UnacknowledgedPackets;
import com.zafar.deepq.WritablePacket;
import com.zafar.deepq.util.Constants;
import com.zafar.deepq.util.Utilities;
import com.zafar.executors.ExecutorUtil;

@Service
public class DeepQImpl extends DeepQ{
	private static Logger logger = LoggerFactory.getLogger(DeepQImpl.class);
	
	/**
	 * This is a lookup table for mappping UUId with read timestamps
	 */
	private ConcurrentHashMap<String, Long> readTimestamps=new ConcurrentHashMap<String, Long>();

	@Autowired
	protected UnacknowledgedPackets backlog=new UnacknowledgedPacketsImpl();

	@Autowired
	private Utilities util;
	
	@Autowired
	private ExecutorUtil executorUtil;
	
	@Override
	public DeferredResult<WritablePacket> read() {
		Observable<Supplier<WritablePacket>> res=Observable.just(() -> {
			logger.debug("polling the head of the queue");
			return queue.pollFirst();
		});
		
		DeferredResult<WritablePacket> result=util.emptyResponse();
		Observable<Supplier<WritablePacket>> response=res.subscribeOn(executorUtil.getReadExecutors());
		response.subscribe(
				(packet) -> {
					logger.debug("reading the queue");
					WritablePacket payload=packet.get();
					if(payload!=null){
						//generate the read timestamp  
						readTimestamps.put(payload.getUuid(), System.currentTimeMillis());
						backlog.addToUnacknowldged(payload);
						logger.debug("setting the result");
						result.setResult(payload);
					}else
						result.setResult(new WritablePacket("", "",Constants.STATUS_EMPTY));
				},
				(exception) -> {
					result.setErrorResult(new WritablePacket("","",Constants.STATUS_ERROR));
					logger.debug("Oops: {}",exception);
				}
		);
		return result;	
	}

	@Override
	public DeferredResult<WritablePacket> write(String data) {
		Observable<WritablePacket> transformed=Observable.just(data).flatMap(
			(argument) ->  {
				String uuid=Utilities.generateUUID();
				WritablePacket packet= new WritablePacket(argument, uuid);
				try {
					logger.debug("Writing the packet {}",packet);
					queue.put(packet);
				} catch (Exception e) {
					logger.error("Oops:{}",e);
					throw new RuntimeException(e); // this will be caught later in the subscriber
				}
				return Observable.just(packet);
			}
		);
		
		DeferredResult<WritablePacket> result=util.emptyResponseWithTimeout();
		Observable<WritablePacket> response=transformed.subscribeOn(executorUtil.getWriteExecutors());
		response.subscribe(
			(output) -> {
				logger.debug("got result {}", output);
				result.setResult(output);
			},
			(exception) -> {
				result.setErrorResult(new WritablePacket("","",Constants.STATUS_ERROR));
				logger.error("Oops:{}",exception);					
			}
		);
		return result;
	}

	@Override
	public DeferredResult<WritablePacket> readWithBlocking(){
		Observable<Supplier<WritablePacket>> res=Observable.just(() -> {
			try {
				return queue.take();
			} catch (Exception e) {
				logger.error("Oops:{}",e);
				throw new RuntimeException(e);
			}
		});
		DeferredResult<WritablePacket> result=util.emptyResponseWithTimeout();
		Observable<Supplier<WritablePacket>> response=res.subscribeOn(executorUtil.getReadExecutors());
		response.subscribe(
				(packet) -> {					
					WritablePacket payload=packet.get();
					if(payload!=null){
						readTimestamps.put(payload.getUuid(), System.currentTimeMillis());
						backlog.addToUnacknowldged(payload);
						result.setResult(payload);
					}
				},
				(exception) -> {
					result.setErrorResult(new WritablePacket("","",Constants.STATUS_ERROR));
					logger.debug("Oops: {}",exception);
				}
		);
		return result;	
	}

	public void processAck(String uuid) {
		Long time=readTimestamps.get(uuid);
		if(time==null){
			logger.debug("There is no record of any read happening for this packet {}",uuid);
			return;
		}else{
			long lapse=System.currentTimeMillis()-time;
			logger.debug("time difference {}",lapse);
			if((lapse)<=(expiryTime.getTimeInMs()))
				backlog.acknowledgePacket(uuid);
			else
				logger.debug("late ack");
		}
	}

	@Override
	public void pushToHead(WritablePacket packet) {
		logger.debug("Pushing unacknowledged packet {} to head",packet);
		readTimestamps.remove(packet.getUuid());
		try {
			queue.putFirst(packet);
		} catch (InterruptedException e) {
			logger.error("Could not put back {}",e);
		}
	}
	public ConcurrentHashMap<String, Long> getReadTimestamps() {
		return readTimestamps;
	}

	public void setReadTimestamps(ConcurrentHashMap<String, Long> readTimestamps) {
		this.readTimestamps = readTimestamps;
	}
}
