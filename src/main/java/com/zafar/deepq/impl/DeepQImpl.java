package com.zafar.deepq.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import rx.Observable;

import com.zafar.deepq.DeepQ;
import com.zafar.deepq.UnacknowledgedPackets;
import com.zafar.deepq.domain.Response;
import com.zafar.deepq.domain.WritablePacket;
import com.zafar.deepq.executors.ExecutorUtil;
import com.zafar.deepq.util.Constants;
import com.zafar.deepq.util.Utilities;

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
	public DeferredResult<Response<WritablePacket>> read() {
		Observable<Supplier<WritablePacket>> res=Observable.just(() -> {
			logger.debug("polling the head of the queue");
			return queue.pollFirst();
		});
		
		DeferredResult<Response<WritablePacket>> result=util.emptyResponse();
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
						result.setResult(new Response<WritablePacket>(payload));
					}else
						result.setResult(new Response<WritablePacket>(new WritablePacket("", ""),Constants.STATUS_EMPTY));
				},
				(exception) -> {
					result.setErrorResult(new Response<WritablePacket>(new WritablePacket("",""),Constants.STATUS_ERROR));
					logger.debug("Oops: {}",exception);
				}
		);
		return result;	
	}

	@Override
	public DeferredResult<Response<WritablePacket>> write(String data) {
		Observable<WritablePacket> transformed=Observable.just(data).flatMap(
			(argument) ->  {
				String uuid=Utilities.generateUUID();
				WritablePacket packet= new WritablePacket(argument, uuid);
				Boolean status=false;
				try {
					logger.debug("Writing the packet {}",packet);
					status=queue.offer(packet);
				} catch (Exception e) {
					logger.error("Oops:{}",e);
					throw new RuntimeException(e); // this will be caught later in the subscriber
				}
				if(status)
					return Observable.just(packet);
				else
					return Observable.just(new WritablePacket());
			}
		);
		
		DeferredResult<Response<WritablePacket>> result=util.emptyResponseWithTimeout();
		Observable<WritablePacket> response=transformed.subscribeOn(executorUtil.getWriteExecutors());
		response.subscribe(
			(output) -> {
				logger.debug("got result {}", output);
				if(!StringUtils.isEmpty(output.getUuid())){
					output.setPayload("");//We will not return the payload as it can be big
					result.setResult(new Response<WritablePacket>(output,Constants.STATUS_OK));
				}else
					result.setResult(new Response<WritablePacket>(null,Constants.STATUS_ERROR));	
			},
			(exception) -> {
				result.setErrorResult(new Response<WritablePacket>(null,Constants.STATUS_ERROR));
				logger.error("Oops:{}",exception);					
			}
		);
		return result;
	}

	@Override
	public DeferredResult<Response<WritablePacket>> readWithBlocking(){
		Observable<Supplier<WritablePacket>> res=Observable.just(() -> {
			try {
				return queue.take();
			} catch (Exception e) {
				logger.error("Oops:{}",e);
				throw new RuntimeException(e);
			}
		});
		DeferredResult<Response<WritablePacket>> result=util.emptyResponseWithTimeout();
		Observable<Supplier<WritablePacket>> response=res.subscribeOn(executorUtil.getReadExecutors());
		response.subscribe(
				(packet) -> {					
					WritablePacket payload=packet.get();
					if(payload!=null){
						readTimestamps.put(payload.getUuid(), System.currentTimeMillis());
						backlog.addToUnacknowldged(payload);
						result.setResult(new Response<WritablePacket>(payload));
					}
				},
				(exception) -> {
					result.setErrorResult(new Response<WritablePacket>(new WritablePacket("",""),Constants.STATUS_ERROR));
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
	/**
	 * Getter method
	 * @return the map with uuid-timestamp mappings
	 */
	public ConcurrentHashMap<String, Long> getReadTimestamps() {
		return readTimestamps;
	}
	/**
	 * Setter method
	 * @param readTimestamps the map to set
	 */
	public void setReadTimestamps(ConcurrentHashMap<String, Long> readTimestamps) {
		this.readTimestamps = readTimestamps;
	}
}
