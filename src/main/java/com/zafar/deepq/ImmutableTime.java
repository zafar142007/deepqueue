package com.zafar.deepq;

import java.util.concurrent.TimeUnit;
/**
 * immutable time container
 * @author zafar
 *
 */
public final class ImmutableTime {
	private long time=0;
	private TimeUnit unit;
	/**
	 * Set the time
	 * @param time the value of time you want to set
	 * @param timeUnit the unit of the time the first parameter is in
	 */
	public ImmutableTime(long time, TimeUnit timeUnit){
		this.time=time;
		this.unit=timeUnit;
	}
	public long getTimeInMs(){
		return TimeUnit.MILLISECONDS.convert(time, unit);
	}
}
