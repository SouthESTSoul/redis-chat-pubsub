package guhao.controller;

import guhao.entity.Message;
import guhao.entity.User;
import guhao.exception.GlobalException;
import guhao.service.ChatSessionService;
import guhao.utils.CoreUtil;
import guhao.utils.R;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Slf4j
@Component
@ServerEndpoint(value = "/chat/{id}")
public class WebsocketServerEndpoint {

    private static ChatSessionService chatSessionService;
    @Autowired
    public void setChatSessionService(ChatSessionService chatSessionService) {
        WebsocketServerEndpoint.chatSessionService = chatSessionService;
    }

    //在线连接数
    private static long online = 0;

    //用于存放当前Websocket对象的Set集合
    public  static ConcurrentHashMap<String,Session> sessionMap = new ConcurrentHashMap<>();

    //与客户端的会话Session
    private Session session;
    

    //当前会话窗口ID
    private String fromId = "";

    /**
     * 链接成功调用的方法
     *
     * @param session
     * @param id
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
    	
    	
        log.info("onOpen >> 链接成功");
        this.session = session;

        //将当前session对象存入到Map集合中
        
        sessionMap.put(id, session);

        //在线人数+1
        addOnlineCount();

        log.info("有新窗口开始监听：" + id + ", 当前在线人数为：" + getOnlineCount());

        this.fromId = id;
        try {
            User user = chatSessionService.findById(fromId);
            //群发消息
            Map<String, Object> map = new HashMap<>();
            map.put("msg", "用户 " + user.getName() + " 已上线");
            sendMore(JSONObject.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        log.info("onClose >> 链接关闭");

        //移除当前session对象
        
        sessionMap.remove(id);
        //在内线人数-1
        subOnLineCount();

        log.info("链接关闭，当前在线人数：" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("接收到窗口：" + fromId + " 的信息：" + message);

        chatSessionService.pushMessage(fromId, null, message);

        //群发消息
        sendMore(getData(null, message));
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

 

    /**
     * 封装返回消息
     *
     * @param toId    指定窗口ID
     * @param message 消息内容
     * @return
     * @throws IOException
     */
    private String getData(String toId, String message) throws IOException {
        Message entity = new Message();
        entity.setMessage(message);
        entity.setTime(CoreUtil.format(new Date()));
        entity.setFrom(chatSessionService.findById(fromId));
        entity.setTo(chatSessionService.findById(toId));
        return JSONObject.toJSONString(new R(entity));
    }

    /**
     * 群发消息
     *
     * @param data
     */
    private void sendMore(String data) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            try {
            	entry.getValue().getBasicRemote().sendText(data);
               
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定窗口推送消息
     *
     * @param entity 推送消息
     * @param toId   接收方ID
     */
    public void sendTo(String toId, Message entity) {
        fromId = entity.getFrom().getId().toString();
        if (sessionMap.size() <= 1) {
            throw new GlobalException("用户未上线");
        }
        
        if(sessionMap.get(toId)!=null) {
        	log.info(entity.getFrom().getId() + " 推送消息到窗口：" + toId + " ，推送内容：" + entity.getMessage());
        	try {
				sessionMap.get(toId).getBasicRemote().sendText(entity.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	chatSessionService.pushMessage(fromId, toId, entity.getMessage());
        }else {
        	throw new GlobalException("推送失败，找不到该窗口");
        	
        }
       
        
    }

    private void subOnLineCount() {
        WebsocketServerEndpoint.online--;
    }

    private synchronized long getOnlineCount() {
        return online;
    }

    private void addOnlineCount() {
        WebsocketServerEndpoint.online++;
    }
}
