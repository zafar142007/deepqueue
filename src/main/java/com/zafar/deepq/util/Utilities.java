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
import com.zafar.deepq.domain.Response;
import com.zafar.deepq.domain.WritablePacket;

/**
 * often used methods
 * @author zafar
 *
 */
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
	 * factory method
	 * @return an empty result with error scenarios prefilled with timeout
	 */
	public DeferredResult<Response<WritablePacket>> emptyResponseWithTimeout(){
		DeferredResult<Response<WritablePacket>> result=
				new DeferredResult<Response<WritablePacket>>(timeout.getTimeInMs(),
						new Response<WritablePacket>(new WritablePacket("",""),Constants.STATUS_TIMEOUT));
		result.onCompletion(() -> {
			//put something to do here in case of completion event
			//such as kafka lognew Response<WritablePacket>
			logger.debug("result received.");
		});
		result.onTimeout(() -> {
			//put something to do here in case of timeout
		});
		return result;
	}
	/**
	 * An empty response factory method
	 * @return an empty result without timeout
	 */
	public DeferredResult<Response<WritablePacket>> emptyResponse(){
		DeferredResult<Response<WritablePacket>> result=new DeferredResult<Response<WritablePacket>>();
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
}
