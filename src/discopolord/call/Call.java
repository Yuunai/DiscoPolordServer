package discopolord.call;

public class Call {

    public static final byte CALL_SEND = 1;

    public static final byte CALL_ONLINE = 2;

    private String callerIdentifier;

    private String targetIdentifier;

    private byte callStatus;

}
