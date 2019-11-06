package net.jfans.shell.explorer.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@ServerEndpoint("/ws/{token}")
@RestController
@RequestMapping("/api")
public class ShellExcutorWebSocket {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShellExcutorWebSocket.class);

	// 用来记录当前连接数的变量
    private static volatile int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<ShellExcutorWebSocket> webSocketSet = new CopyOnWriteArraySet<ShellExcutorWebSocket>();

    // 与某个客户端的连接会话，需要通过它来与客户端进行数据收发
    private Session session;
    
    
    @RequestMapping("/login")
    @ResponseBody
    public Object login(String username,String password) {
    	Map<String, Object> result = new HashMap<String, Object>();
    	result.put("success", true);
    	return result;
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws Exception {
        this.session = session;
        System.out.println(this.session.getId());
        webSocketSet.add(this);
        LOGGER.info("Open a websocket. token={}", token);
    }
    
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        LOGGER.info("Close a websocket. ");
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Receive a message from client: " + message);
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error while websocket. ", error);
    }
    
    public void sendMessage(String message) throws Exception {
        if (this.session.isOpen()) {
            this.session.getBasicRemote().sendText("Send a message from server. ");
        }
    }
    
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
}
