package discopolord.client;

import discopolord.database.UserService;
import discopolord.entity.User;
import discopolord.event.UserConnectedEvent;
import discopolord.event.UserDisconnectedEvent;
import discopolord.event.UserEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientStatusServer {

    private static UserService userService = new UserService();
    private static List<ClientStatusListener> listeners = new ArrayList<>();
    private static List<String> onlineUsers = new ArrayList<>();

    public synchronized void addListener(ClientStatusListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(ClientStatusListener listener) {
        listeners.remove(listener);
    }

    public synchronized void sendEvent(UserEvent event) {
        if(event instanceof UserConnectedEvent) {
            onlineUsers.add(event.getUserIdentifier());
        } else if(event instanceof UserDisconnectedEvent) {
            onlineUsers.remove(event.getUserIdentifier());
        }

        notifyAll(event);
    }

    private void notifyAll(UserEvent event) {
        String userIdentifier = event.getUserIdentifier();
        User user = userService.getUser(userIdentifier);

        for(ClientStatusListener listener : listeners) {
            if(event instanceof UserConnectedEvent) {
                listener.userConnected(user);
            } else if(event instanceof UserDisconnectedEvent) {
                listener.userDisconnected(user);
            }
        }
    }

    public static boolean isUserOnline(String identifier) {
        if(onlineUsers.contains(identifier))
            return true;

        return false;
    }

}
