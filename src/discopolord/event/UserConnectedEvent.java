package discopolord.event;

public class UserConnectedEvent extends UserEvent {
    public UserConnectedEvent(String userIdentifier) {
        super(userIdentifier);
    }
}
