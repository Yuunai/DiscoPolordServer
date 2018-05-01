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

    public static int call(String callerIdentifier, String targetIdentifier) {
        if(usersCalls.get(callerIdentifier) == null)
            usersCalls.put(callerIdentifier, new ArrayList<>());

        if(usersCalls.get(targetIdentifier) == null)
            usersCalls.put(targetIdentifier, new ArrayList<>());

        if(!usersCalls.get(targetIdentifier).isEmpty())
            return -1;

        List<Call> callerCalls = usersCalls.get(callerIdentifier);
        Client target = clients.get(targetIdentifier);
        callerCalls.add(new Call(targetIdentifier, Call.CALL_SEND));
        usersCalls.get(callerIdentifier).add(new Call(callerIdentifier, Call.CALL_SEND));

        if(callerCalls.size() == 1) {
            target.sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CL_INV).build());
//            TODO add caller address/port
        } else {
            target.sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CNF_INV).build());
//            TODO add callers addresses/ports
        }

        return 1;
    }

    public static int acceptCall(String accepterIdentifier) {
        List<Call> accepterCalls = usersCalls.get(accepterIdentifier);
        String callerIdentifier = accepterCalls.get(0).getCallerIdentifier();
        List<Call> callerCalls = usersCalls.get(callerIdentifier);

//        TODO remember to set addresses everywhere
        if(clients.get(callerIdentifier) != null)
            clients.get(callerIdentifier).sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.ADR).build());
        for(Call call : callerCalls) {
            if(call.getCallerIdentifier().equals(accepterIdentifier)) {
                call.setCallStatus(Call.CALL_ONLINE);
            } else {
                accepterCalls.add(call);
                if(clients.get(call.getCallerIdentifier()) != null)
                    clients.get(call.getCallerIdentifier()).sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.ADR).build());
            }
        }

        return 0;
    }

    public static void denyCall(String declinerIdentifier) {
        String callerIdentifier = usersCalls.get(declinerIdentifier).get(0).getCallerIdentifier();
        if(clients.get(callerIdentifier) != null)
            clients.get(callerIdentifier).sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CL_DEN).build());
        usersCalls.get(declinerIdentifier).clear();
        usersCalls.get(callerIdentifier).removeIf(c -> c.getCallerIdentifier().equals(declinerIdentifier));
    }

    public static void disconnect(String identifier) {
        for(Call call : usersCalls.get(identifier)) {
            usersCalls.get(call.getCallerIdentifier()).removeIf(c -> c.getCallerIdentifier().equals(identifier));
        }
        usersCalls.get(identifier).clear();
    }

    public static void addClient(String identifier, Client client) {
        clients.put(identifier, client);
    }

    public static void removeClient(String identifier) {
        clients.remove(identifier);
    }

}
