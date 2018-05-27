package discopolord.call;

import discopolord.protocol.Succ;

public class Call {

    public static final byte CALL_SEND = 1;

    public static final byte CALL_ONLINE = 2;

    private String callerIdentifier;

    private Succ.Message.UserAddress callerAddress;

    private byte callStatus;

    public Call() {
    }

    public Call(String callerIdentifier, byte callStatus, Succ.Message.UserAddress callerAddress) {
        this.callerIdentifier = callerIdentifier;
        this.callStatus = callStatus;
        this.callerAddress = callerAddress;
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

    public Succ.Message.UserAddress getCallerAddress() {
        return callerAddress;
    }

    public void setCallerAddress(Succ.Message.UserAddress address) {
        this.callerAddress = address;
    }
}
