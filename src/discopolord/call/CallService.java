package discopolord.call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallService {

    Map<String, List<Call>> usersCalls = new HashMap<>();

    public int call(String callerIdentifier, String targetIdentifier) {
        if(usersCalls.get(callerIdentifier) == null)
            usersCalls.put(callerIdentifier, new ArrayList<>());

        if(usersCalls.get(targetIdentifier) == null)
            usersCalls.put(targetIdentifier, new ArrayList<>());

        if(!usersCalls.get(targetIdentifier).isEmpty())
            return -1;

        List<Call> userCalls = usersCalls.get(callerIdentifier);
        if(userCalls.isEmpty()) {
//            TODO call target with CALL_INV
        } else {
//            TODO call target with CNF_INV
        }

        return 1;
    }

    public int acceptCall(String accepterIdentifier, String callerIdentifier) {
//        TODO send CALL_ACC with params
        return 0;
    }

    public int declineCall(String declinerIdentifier, String callerIdentifier) {
//        TODO send CAL_DEN
        return 0;
    }

}
