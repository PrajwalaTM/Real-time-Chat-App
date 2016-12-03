package chat;

import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {
	// this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    // Session - conversation between two websocket end points
	// Session is created after a websocket handshake is completed.
	// Message Handlers are used by socket endpoints to receive incoming messages
	// RemoteEndPoints are used to send messages to other web socket endpoint on the Session
	// must be static as they have to be class variables necessarily
	static Map<Session, String> userUsernameMap = new ConcurrentHashMap<Session, String>();
    static int nextUserNumber = 1; //Used for creating the next username

    public static void main(String[] args) {
    	
    	//path for static files 
        staticFileLocation("/public"); //index.html is served at localhost:4567 (default port)
        // path "/chat" is handled by the "ChatWebSocketHandler" class
        webSocket("/chat", ChatWebSocketHandler.class);
        // init() as no other HTTP routes are defined later
        init();
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                    .put("userlist", userUsernameMap.values())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    //Builds a HTML element with a sender-name, a message, and a timestamp using j2html
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }
}
