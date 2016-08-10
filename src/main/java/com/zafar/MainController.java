package com.zafar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zafar.miniq.impl.MiniQImpl;

@Controller
public class MainController {

	@Autowired
	private MiniQImpl<String> queue;
	
	@ResponseBody
	@RequestMapping(value = "/read", method = RequestMethod.GET)
	public String read(ModelMap model) {
		return queue.read();
	}

	@ResponseBody
	@RequestMapping(value = "/readWithBlocking", method = RequestMethod.GET)
	public String readWithBlocking(ModelMap model){
		try {
			return queue.readWithBlocking();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}

	@ResponseBody
	@RequestMapping(value = "/write/{payload}", method = RequestMethod.GET)
	public String write(ModelMap model, @PathVariable String payload){
		return queue.write(payload);
	}
	
	@RequestMapping(value = "/ack/{uuid}", method = RequestMethod.GET)
	public void ack(ModelMap model, @PathVariable String uuid){
		queue.delete(uuid);
	}
}
