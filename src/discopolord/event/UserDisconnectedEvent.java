package discopolord.event;

public class UserDisconnectedEvent extends UserEvent {
    public UserDisconnectedEvent(String userIdentifier) {
        super(userIdentifier);
    }
}
