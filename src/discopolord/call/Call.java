package discopolord.call;

public class Call {

    public static final byte CALL_SEND = 1;

    public static final byte CALL_ONLINE = 2;

    private String callerIdentifier;

    private byte callStatus;

    public Call() {
    }

    public Call(String callerIdentifier, byte callStatus) {
        this.callerIdentifier = callerIdentifier;
        this.callStatus = callStatus;
    }

    public String getCallerIdentifier() {
        return callerIdentifier;
    }

    public void setCallerIdentifier(String callerIdentifier) {
        this.callerIdentifier = callerIdentifier;
    }

    public byte getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(byte callStatus) {
        this.callStatus = callStatus;
    }
}
