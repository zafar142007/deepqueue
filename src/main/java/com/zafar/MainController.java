package com.zafar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.zafar.deepq.DeepQ;
import com.zafar.deepq.WritablePacket;

@Controller
public class MainController {

	private final static Logger logger=LoggerFactory.getLogger(MainController.class);
	
	@Autowired
	private DeepQ queue;
	
	@ResponseBody
	@RequestMapping(value = "/read", method = RequestMethod.GET)
	public DeferredResult<WritablePacket> read(ModelMap model) {
		return queue.read();
	}

	@ResponseBody
	@RequestMapping(value = "/readWithBlocking", method = RequestMethod.GET)
	public DeferredResult<WritablePacket> readWithBlocking(ModelMap model){
		return queue.readWithBlocking();
	}

	@ResponseBody
	@RequestMapping(value = "/write/{payload}", method = RequestMethod.GET)
	public DeferredResult<WritablePacket> write(ModelMap model, @PathVariable String payload){
		logger.debug("Writing payload:{}",payload);
		return queue.write(payload);
	}
	
	@RequestMapping(value = "/ack/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<String> ack(ModelMap model, @PathVariable String uuid){
		queue.processAck(uuid);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
}
