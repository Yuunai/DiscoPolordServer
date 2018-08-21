package discopolord.call;

import discopolord.client.Client;
import discopolord.protocol.Succ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallService {
//    TODO add synchronization
    private static Map<String, Client> clients = new HashMap<>();

    private static Map<String, List<Call>> usersCalls = new HashMap<>();

    public static synchronized int call(Succ.Message.UserAddress address, String targetIdentifier) {
        String callerIdentifier = address.getUserIdentifier();

        if(usersCalls.get(callerIdentifier) == null)
            usersCalls.put(callerIdentifier, new ArrayList<>());

        if(usersCalls.get(targetIdentifier) == null)
            usersCalls.put(targetIdentifier, new ArrayList<>());

        if(!usersCalls.get(targetIdentifier).isEmpty())
            return -1;

        List<Call> callerCalls = usersCalls.get(callerIdentifier);
        Client target = clients.get(targetIdentifier);
        callerCalls.add(new Call(targetIdentifier, Call.CALL_SEND, null));
        usersCalls.get(targetIdentifier).add(new Call(callerIdentifier, Call.CALL_SEND, address));

        if(callerCalls.size() == 1) {
            target.sendMessage(Succ.Message.newBuilder()
                    .setMessageType(Succ.Message.MessageType.CL_INV)
                    .addAddresses(address)
                    .build());
//            TODO add caller address/port
        } else {
            target.sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CNF_INV).build());
//            TODO add callers addresses/ports
        }

        return 1;
    }

    public static synchronized int acceptCall(String accepterIdentifier, Succ.Message.UserAddress address) {
        List<Call> accepterCalls = usersCalls.get(accepterIdentifier);
        String callerIdentifier = accepterCalls.get(0).getCallerIdentifier();
        List<Call> callerCalls = usersCalls.get(callerIdentifier);

        for(Call call : callerCalls) {
            if(call.getCallerIdentifier().equals(accepterIdentifier)) {
                call.setCallerAddress(address);
            }
        }

//        TODO remember to set addresses everywhere
        if(clients.get(callerIdentifier) != null)
            clients.get(callerIdentifier).sendMessage(Succ.Message.newBuilder()
                    .setMessageType(Succ.Message.MessageType.ADR)
                    .addAddresses(address)
                    .build());

        for(Call call : callerCalls) {
            if(call.getCallerIdentifier().equals(accepterIdentifier)) {
                call.setCallStatus(Call.CALL_ONLINE);
            } else {
                accepterCalls.add(call);
                if(clients.get(call.getCallerIdentifier()) != null)
                    clients.get(call.getCallerIdentifier())
                            .sendMessage(Succ.Message.newBuilder()
                                    .setMessageType(Succ.Message.MessageType.ADR)
                                    .addAddresses(address)
                                    .build());
            }
        }

        Succ.Message.Builder msgBuilder = Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.ADR);
        for(Call call : accepterCalls) {
            call.setCallStatus(Call.CALL_ONLINE);
            msgBuilder.addAddresses(call.getCallerAddress());
        }

        clients.get(accepterIdentifier).sendMessage(msgBuilder.build());

        return 0;
    }

    public static synchronized void denyCall(String declinerIdentifier) {
        List<Call> declinerCalls = usersCalls.get(declinerIdentifier);
        if(declinerCalls == null || declinerCalls.isEmpty())
            return;


        String callerIdentifier = usersCalls.get(declinerIdentifier).get(0).getCallerIdentifier();
        if(clients.get(callerIdentifier) != null)
            clients.get(callerIdentifier).sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CL_DEN).build());
        usersCalls.get(declinerIdentifier).clear();
        usersCalls.get(callerIdentifier).removeIf(c -> c.getCallerIdentifier().equals(declinerIdentifier));
    }

    public static synchronized void disconnect(String identifier) {
        if(usersCalls.get(identifier) == null)
            return;
        for(Call call : usersCalls.get(identifier)) {
            clients.get(call.getCallerIdentifier()).sendMessage(Succ.Message.newBuilder()
                    .setMessageType(Succ.Message.MessageType.DISC)
                    .build());
            usersCalls.get(call.getCallerIdentifier()).removeIf(c -> c.getCallerIdentifier().equals(identifier));
        }
        usersCalls.get(identifier).clear();
    }

    public static synchronized void addClient(String identifier, Client client) {
        clients.put(identifier, client);
    }

    public static synchronized void removeClient(String identifier) {
        clients.remove(identifier);
    }

}
