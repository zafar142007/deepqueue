package com.zafar.deepq.util;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.zafar.deepq.ImmutableTime;
import com.zafar.deepq.WritablePacket;


@Component
public class Utilities {
	
	@Value("${timeout.request.s:5}")
	private long requestTimeout;
	
	private static Logger logger=LoggerFactory.getLogger(Utilities.class);
	
	private ImmutableTime timeout;
	
	@PostConstruct
	public void init(){
		timeout=new ImmutableTime(requestTimeout, TimeUnit.SECONDS);
	}
	
	/**
	 * Return with a prefilled DeferredResult
	 * @return
	 */
	public DeferredResult<WritablePacket> emptyResponseWithTimeout(){
		DeferredResult<WritablePacket> result=new DeferredResult<WritablePacket>(timeout.getTimeInMs(),new WritablePacket("","",Constants.STATUS_TIMEOUT));
		result.onCompletion(() -> {
			//put something to do here in case of completion event
			//such as kafka log
			logger.debug("result received.");
		});
		result.onTimeout(() -> {
			//put something to do here in case of timeout
		});
		return result;
	}
	public DeferredResult<WritablePacket> emptyResponse(){
		DeferredResult<WritablePacket> result=new DeferredResult<WritablePacket>();
		result.onCompletion(() -> {
			//put something to do here in case of completion event
			//such as kafka log
			logger.debug("result received.");
		}
		);
		return result;
	}
	public static final String generateUUID(){
		UUID u=UUID.randomUUID();
		long id=u.getLeastSignificantBits();
		return System.currentTimeMillis()+Constants.ID_SEPARATOR+Long.toString(id);
	}
	public static final long getTimeStampFromId(String messageId){
		return Long.parseLong(messageId.substring(0,messageId.indexOf(Constants.ID_SEPARATOR)));
	}
}
