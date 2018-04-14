package discopolord.event;

public class UserEvent {

    private String userIdentifier;

    public UserEvent(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }
}
