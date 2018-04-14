package discopolord.entity;

public class User {

    private int userId;
    private String identifier;
    private String username;
    private String password;
    private String email;

    public User() {
    }

    public User(String identifier, String username, String password, String email) {
        this.identifier = identifier;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(int userId, String identifier, String username, String password, String email) {
        this.userId = userId;
        this.identifier = identifier;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", identifier='" + identifier + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
