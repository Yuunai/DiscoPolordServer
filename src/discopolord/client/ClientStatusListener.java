package discopolord.client;

import discopolord.entity.User;

public interface ClientStatusListener {

    void userConnected(User user);

    void userDisconnected(User user);

}
