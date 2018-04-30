package discopolord.client;

import discopolord.database.UserService;
import discopolord.entity.Contact;
import discopolord.entity.User;
import discopolord.event.UserConnectedEvent;
import discopolord.event.UserDisconnectedEvent;
import discopolord.protocol.Succ;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Client extends Thread implements ClientStatusListener{

    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    private Socket socket;
    private ClientStatusServer clientStatusServer;
    private Map<String, Succ.Message.UserStatus> userStatuses = new HashMap<>();

    private User user;
    private String encryptionKey;

    private UserService userService = new UserService();

    private InputStream input;
    private OutputStream output;

    private Succ.Message message;
    private Succ.Message response;

    public Client(Socket socket, ClientStatusServer clientStatusServer) {
        this.socket = socket;
        this.clientStatusServer = clientStatusServer;
    }


    public void run() {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            // First step - Diffi Hellman
            //TODO generate public numbers and send them

            // Second step - Auth/Register User
            while(user == null) {
                message = getMessage();
                switch (message.getMessageType()) {
                    case LOGIN:
                        user = userService.getUser(message.getLoginData().getEmail(), message.getLoginData().getPassword());
                        if (user != null) {
                            clientStatusServer.sendEvent(new UserConnectedEvent(user.getIdentifier()));
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.AUTH).build());
                        } else {
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.NAUTH).build());
                        }
                        break;
Succ.Message.newBuilder().addAddresses()
                    case REGISTER:
                        int registrationStatus = userService.saveOrUpdateUser(
                                new User(message.getRegistrationData().getIdentifier(),
                                        message.getRegistrationData().getUsername(),
                                        message.getRegistrationData().getPassword(),
                                        message.getRegistrationData().getEmail()));
                        if(registrationStatus == 1) {
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.REGISTRATION_SUCC).build());
                        } else {
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.REGISTRATION_FAILED).addErrorCauses(registrationStatus).build());
                        }
                        break;

                    default:
                        response = Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.NAUTH).build();
                        sendMessage(response);
                        break;
                }
            }


            // Third step - Communication
            while(true) {
                message = getMessage();
                switch(message.getMessageType()) {
                    case C_REQ:
                        List<Succ.Message.UserStatus> userContacts= userService.getUserRelations(user.getUserId());
                        sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_LIST).addAllUsers(userContacts).build());
                        break;

                    case C_UPD:
                        for(Succ.Message.UserStatus status : message.getUsersList()) {
                            int id = userService.getUserId(status.getIdentifier());
                           switch (status.getStatus()) {
                               case DELETED:
                                   userService.deleteUserContact(user.getUserId(), id);
                                   sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_UPD).addUsers(status).build());
                                   break;

                               case BLOCKED:
                                   userService.saveOrUpdateUserContact(new Contact(user.getUserId(), id, Contact.RELATION_TYPE_BLOCKED));
                                   sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_UPD).addUsers(status).build());
                                   break;

                               case FRIEND:
                                   userService.saveOrUpdateUserContact(new Contact(user.getUserId(), id, Contact.RELATION_TYPE_FRIEND));
                                   sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_UPD).addUsers(status).build());
                                   break;
                           }
                        }
                        break;

                    case CL_INV:

                }
            }


            // Last step - Cleaning


        } catch (IOException e) {
            logger.warning(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }

            try {
                input.close();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }

            try {
                output.close();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
        clientStatusServer.sendEvent(new UserDisconnectedEvent(user.getIdentifier()));
    }

    private Succ.Message getMessage() {
        Succ.Message message = null;
        try {
            message = Succ.Message.parseFrom(input);
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return message;
    }

    private void sendMessage(Succ.Message message) {
        try {
            message.writeTo(output);
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return user.equals(client.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public void userConnected(String identifier) {
        if(userStatuses.keySet().contains(identifier) && userStatuses.get(identifier).getStatusValue() != 2) {
            userStatuses.put(identifier, Succ.Message.UserStatus.newBuilder()
                    .mergeFrom(userStatuses.get(identifier))
                    .setStatusValue(Succ.Message.Status.ONLINE_VALUE)
                    .build());
        }
    }

    @Override
    public void userDisconnected(String identifier) {
        if(userStatuses.keySet().contains(identifier) && userStatuses.get(identifier).getStatusValue() != 2) {
            userStatuses.put(identifier, Succ.Message.UserStatus.newBuilder()
                    .mergeFrom(userStatuses.get(identifier))
                    .setStatusValue(Succ.Message.Status.OFFLINE_VALUE)
                    .build());
        }
    }
}
