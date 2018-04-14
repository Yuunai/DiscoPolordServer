package discopolord.client;

import discopolord.event.UserConnectedEvent;
import discopolord.event.UserDisconnectedEvent;
import discopolord.event.UserEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientStatusServer {

    List<ClientStatusListener> listeners = new ArrayList<>();
    List<String> onlineUsers = new ArrayList<>();

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

        for(ClientStatusListener listener : listeners) {
            if(event instanceof UserConnectedEvent) {
                listener.userConnected(userIdentifier);
            } else if(event instanceof UserDisconnectedEvent) {
                listener.userDisconnected(userIdentifier);
            }
        }
    }

}
