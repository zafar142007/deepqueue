package com.zafar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zafar.deepq.WritablePacket;
import com.zafar.deepq.impl.DeepQImpl;

@Controller
public class MainController {

	@Autowired
	private DeepQImpl queue;
	
	@ResponseBody
	@RequestMapping(value = "/read", method = RequestMethod.GET)
	public WritablePacket read(ModelMap model) {
		return queue.read();
	}

	@ResponseBody
	@RequestMapping(value = "/readWithBlocking", method = RequestMethod.GET)
	public WritablePacket readWithBlocking(ModelMap model){
		try {
			return queue.readWithBlocking();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/write/{payload}", method = RequestMethod.GET)
	public String write(ModelMap model, @PathVariable String payload){
		return queue.write(payload);
	}
	
	@RequestMapping(value = "/ack/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<String> ack(ModelMap model, @PathVariable String uuid){
		queue.processAck(uuid);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
}
