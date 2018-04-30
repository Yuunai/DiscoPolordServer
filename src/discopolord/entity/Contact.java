package discopolord.entity;

public class Contact {

    public static final byte RELATION_TYPE_FRIEND = 1;
    public static final byte RELATION_TYPE_BLOCKED = 2;

    private int userId;
    private int contactId;
    private byte relationType;

    public Contact() {
    }

    public Contact(int userId, int contactId, byte relationType) {
        this.userId = userId;
        this.contactId = contactId;
        this.relationType = relationType;
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

    public byte getRelationType() {
        return relationType;
    }

    public void setRelationType(byte relationType) {
        this.relationType = relationType;
    }
}
