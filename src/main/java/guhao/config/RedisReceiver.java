package guhao.config;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import guhao.controller.ChatController;
import guhao.controller.WebsocketServerEndpoint;
import guhao.entity.Message;
import guhao.exception.GlobalException;
import guhao.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisReceiver {

	@Autowired
    private ChatSessionService chatSessionService;
	
	 public void receiveMessage(String messageString) {
	    
	      
	        Message message = JSON.parseObject(messageString,Message.class);
	        String toId=message.getTo().getId().toString();
	        String fromId=message.getFrom().getId().toString();
	        if(WebsocketServerEndpoint.sessionMap.containsKey(toId)) {
	        	
	        	try {
	        		WebsocketServerEndpoint.sessionMap.get(toId).getBasicRemote().sendText(message.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	chatSessionService.pushMessage(fromId, toId, message.getMessage());
	        	log.info(fromId + " 推送消息到窗口：" + toId+ " ，推送内容：" + message.getMessage());
	        	
	        }
	       
	        
	       
	    }
}
