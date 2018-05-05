package discopolord.entity;

public class Contact {

    public static final byte CONTACT_TYPE_FRIEND = 1;
    public static final byte CONTACT_TYPE_BLOCKED = 2;

    private int userId;
    private int contactId;
    private byte contactType;

    public Contact() {
    }

    public Contact(int userId, int contactId, byte contactType) {
        this.userId = userId;
        this.contactId = contactId;
        this.contactType = contactType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public byte getContactType() {
        return contactType;
    }

    public void setContactType(byte contactType) {
        this.contactType = contactType;
    }
}
