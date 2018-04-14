package discopolord.entity;

public class Relation {

    public static final byte RELATION_TYPE_FRIEND = 1;
    public static final byte RELATION_TYPE_BLOCKED = 2;

    private int user1Id;
    private int user2Id;
    private byte relationType;

    public Relation() {
    }

    public Relation(int user1Id, int user2Id, byte relationType) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.relationType = relationType;
    }

    public int getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(int user1Id) {
        this.user1Id = user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(int user2Id) {
        this.user2Id = user2Id;
    }

    public byte getRelationType() {
        return relationType;
    }

    public void setRelationType(byte relationType) {
        this.relationType = relationType;
    }
}
