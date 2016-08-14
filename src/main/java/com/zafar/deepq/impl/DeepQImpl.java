package com.zafar.deepq.impl;

import java.util.Map;
import java.util.TreeMap;
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
						payload.setUuid(Utilities.generateUUID());//send back the read timestamp
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
		long lapse=System.currentTimeMillis()-Utilities.getTimeStampFromId(uuid);
		logger.debug("time difference {}",lapse);
		if((lapse)<=(expiryTime.getTimeInMs()))
			backlog.acknowledgePacket(uuid);
		else
			logger.debug("late ack");
	}

	@Override
	public void pushToHead(Map<Long, WritablePacket> bucket) {
		TreeMap<Long, WritablePacket> sortedMap=new TreeMap<Long,WritablePacket>();//ascending order of timeouts	
		sortedMap.putAll(bucket);
		logger.debug("Pushing unacknowledged packets to head");
		for(WritablePacket packet:sortedMap.values())
			try {
				packet.setUuid(Utilities.generateUUID());
				queue.putFirst(packet);
			} catch (InterruptedException e) {
				logger.debug("Exception",e);
			}
	}

}
