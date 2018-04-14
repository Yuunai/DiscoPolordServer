package discopolord.client;

public interface ClientStatusListener {

    void userConnected(String identifier);

    void userDisconnected(String identifier);

}
