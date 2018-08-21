package discopolord.client;

import com.google.protobuf.ByteString;
import com.mysql.jdbc.StringUtils;
import discopolord.call.Call;
import discopolord.call.CallService;
import discopolord.database.UserService;
import discopolord.entity.Contact;
import discopolord.entity.User;
import discopolord.event.UserConnectedEvent;
import discopolord.event.UserDisconnectedEvent;
import discopolord.protocol.Succ;
import discopolord.security.DHServer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.Socket;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
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
    private SecretKeySpec secretKeySpec;
    private Cipher encCipher;
    private Cipher decCipher;
    private boolean encryptionInitialized = false;
    private MessageDigest messageDigest;

    private UserService userService = new UserService();

    private InputStream input;
    private byte[] inputBuffer = new byte[2048];
    private OutputStream output;

    private Succ.Message message;
    private Succ.Message response;

    public Client(Socket socket, ClientStatusServer clientStatusServer) {
        this.socket = socket;
        this.clientStatusServer = clientStatusServer;
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            //Impossible
        }
    }


    public void run() {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            // First step - Diffi Hellman
            while (StringUtils.isNullOrEmpty(encryptionKey)) {
                try {
                    DHServer dhServer = new DHServer();
                    sendMessage(Succ.Message.newBuilder()
                            .setMessageType(Succ.Message.MessageType.DHN)
                            .setDH(ByteString.copyFrom(dhServer.getPublicKeys()))
                            .build());
                    message = getMessage();
                    if(message.getMessageType().equals(Succ.Message.MessageType.DHN)) {
                        encryptionKey = dhServer.getSecret(message.getDH().toByteArray());
                        secretKeySpec = new SecretKeySpec(encryptionKey.getBytes(), 0, 16, "AES");
                        try {
                            encCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            decCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                            aesParams.init(message.getEPS().toByteArray());
                            decCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, aesParams);
                            encCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.EP)
                                    .setEPS(ByteString.copyFrom(encCipher.getParameters().getEncoded())).build());
                            encryptionInitialized = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    logger.warning("Implementation error: " + e.getMessage());
                } catch (InvalidKeySpecException e) {
                    logger.warning("Client DH error! " + e.getMessage());
                }

            }

            // Second step - Auth/Register User
            while(user == null) {
                message = getMessage();
                switch (message.getMessageType()) {
                    case LOGIN:
                        user = userService.getUser(message.getLoginData().getEmail(), new HexBinaryAdapter()
                                .marshal(messageDigest.digest(message
                                        .getLoginData()
                                        .getPassword()
                                        .getBytes("UTF-8"))));
                        if (user != null) {
                            clientStatusServer.sendEvent(new UserConnectedEvent(user.getIdentifier()));
                            clientStatusServer.addListener(this);
                            CallService.addClient(user.getIdentifier(), this);
                            sendMessage(Succ.Message.newBuilder()
                                    .setMessageType(Succ.Message.MessageType.AUTH)
                                    .addUsers(Succ.Message.UserStatus.newBuilder()
                                            .setUsername(user.getUsername())
                                            .setIdentifier(user.getIdentifier())
                                            .build())
                                    .build());
                            for(Succ.Message.UserStatus s : getContacts(user.getUserId())) {
                                userStatuses.put(s.getIdentifier(), s);
                            }
                        } else {
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.NAUTH).build());
                        }
                        break;

                    case REGISTER:
                        int registrationStatus = userService.saveOrUpdateUser(
                                new User(message.getRegistrationData().getIdentifier(),
                                        message.getRegistrationData().getUsername(),
                                        new HexBinaryAdapter()
                                                .marshal(messageDigest.digest(message
                                                        .getRegistrationData()
                                                        .getPassword()
                                                        .getBytes("UTF-8"))),
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
                        sendMessage(Succ.Message.newBuilder()
                                .setMessageType(Succ.Message.MessageType.C_LIST)
                                .addAllUsers(getContacts(user.getUserId()))
                                .build());
                        break;

                    case C_UPD:
                        for(Succ.Message.UserStatus status : message.getUsersList()) {
                            int id = userService.getUserId(status.getIdentifier());
                            if(id == 0) {
                                sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_UPD).build());
                                continue;
                            }

                            User statusUser = userService.getUser(id);
                           switch (status.getStatus()) {
                               case DELETED:
                                   userService.deleteUserContact(user.getUserId(), id);
                                   sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.C_UPD).addUsers(status).build());
                                   userStatuses.remove(statusUser.getIdentifier());
                                   break;

                               case BLOCKED:
                                   Succ.Message.UserStatus updStatusBlocked = Succ.Message.UserStatus.newBuilder()
                                           .setIdentifier(statusUser.getIdentifier())
                                           .setUsername(statusUser.getUsername())
                                           .setStatus(Succ.Message.Status.BLOCKED).build();
                                   userStatuses.put(statusUser.getIdentifier(), updStatusBlocked);
                                   userService.saveOrUpdateUserContact(new Contact(user.getUserId(), id, Contact.CONTACT_TYPE_BLOCKED));
                                   sendMessage(Succ.Message.newBuilder()
                                           .setMessageType(Succ.Message.MessageType.C_UPD)
                                           .addUsers(updStatusBlocked)
                                           .build());
                                   break;

                               case FRIEND:
                                   Succ.Message.UserStatus updStatusFriend = Succ.Message.UserStatus.newBuilder()
                                           .setIdentifier(statusUser.getIdentifier())
                                           .setUsername(statusUser.getUsername())
                                           .setStatus(clientStatusServer.isUserOnline(statusUser.getIdentifier()) ? Succ.Message.Status.ONLINE : Succ.Message.Status.OFFLINE)
                                           .build();
                                   userStatuses.put(statusUser.getIdentifier(), updStatusFriend);
                                   userService.saveOrUpdateUserContact(new Contact(user.getUserId(), id, Contact.CONTACT_TYPE_FRIEND));
                                   sendMessage(Succ.Message.newBuilder()
                                           .setMessageType(Succ.Message.MessageType.C_UPD)
                                           .addUsers(updStatusFriend)
                                           .build());
                                   break;
                           }
                        }
                        break;

                    case CL_INV:
                        if(!ClientStatusServer.isUserOnline(message.getAddresses(0).getUserIdentifier())
                                || CallService.call(Succ.Message.UserAddress.newBuilder()
                                    .setUserIdentifier(user.getIdentifier())
                                    .setPort(message.getAddresses(0).getPort())
                                    .setIp(socket.getInetAddress().getHostAddress()).build(), message.getAddresses(0)
                                .getUserIdentifier()) != 1) {
                            sendMessage(Succ.Message.newBuilder().setMessageType(Succ.Message.MessageType.CL_DEN).build());
                        }
                        break;

                    case CL_ACC:
                        CallService.acceptCall(user.getIdentifier(), Succ.Message.UserAddress.newBuilder()
                                .setUserIdentifier(user.getIdentifier())
                                .setIp(socket.getInetAddress().getHostAddress())
                                .setPort(message.getAddresses(0).getPort())
                                .build());
                        break;

                    case CL_DEN:
                        CallService.denyCall(user.getIdentifier());
                        break;

                    case DISC:
                        CallService.disconnect(user.getIdentifier());
                        break;

                        default:
//                            TODO incorrect message
                }
            }


            // Last step - Cleaning


        } catch (IOException e) {
            logger.warning(e.getMessage());
        } catch (ClientDisconnectedException e) {
            if(user != null) {
                CallService.denyCall(user.getIdentifier());
                CallService.disconnect(user.getIdentifier());
            }
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
        if(user != null) {
            clientStatusServer.removeListener(this);
            clientStatusServer.sendEvent(new UserDisconnectedEvent(user.getIdentifier()));
            CallService.removeClient(user.getIdentifier());
        }
    }

    private List<Succ.Message.UserStatus> getContacts(int id) {
        List<Succ.Message.UserStatus> userContacts= userService.getUserRelations(id);
        List<Succ.Message.UserStatus> userContactsStatuses = new ArrayList<>();
        for(Succ.Message.UserStatus st : userContacts) {
            if(st.getStatusValue() == Contact.CONTACT_TYPE_FRIEND
                    && clientStatusServer.isUserOnline(st.getIdentifier())) {
                userContactsStatuses.add(Succ.Message.UserStatus.newBuilder()
                        .setUsername(st.getUsername())
                        .setStatus(Succ.Message.Status.ONLINE)
                        .setIdentifier(st.getIdentifier())
                        .build());
            } else {
                userContactsStatuses.add(st);
            }
        }

        return userContactsStatuses;
    }

    private Succ.Message getMessage() throws ClientDisconnectedException {
//        TODO add decryption
        Succ.Message message = null;
        try {
            if(encryptionInitialized) {
                //TODO IllegalArgumentException
                int dataLen = input.read(inputBuffer);
                message = Succ.Message.parseFrom(decCipher.doFinal(inputBuffer, 0, dataLen));
            } else {
                message = Succ.Message.parseDelimitedFrom(input);
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
            throw new ClientDisconnectedException();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return message;
    }

    public void sendMessage(Succ.Message message) {
//        TODO add encryption
        try {
            if(encryptionInitialized) {
                output.write(encCipher.doFinal(message.toByteArray()));
            } else {
                message.writeDelimitedTo(output);
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
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
    public void userConnected(User us) {
        String identifier = us.getIdentifier();
        if(userStatuses.keySet().contains(identifier) && userStatuses.get(identifier).getStatus() != Succ.Message.Status.BLOCKED) {
            Succ.Message.UserStatus updateStatus = Succ.Message.UserStatus.newBuilder()
                    .mergeFrom(userStatuses.get(identifier))
                    .setStatusValue(Succ.Message.Status.ONLINE_VALUE)
                    .setUsername(us.getUsername())
                    .build();
            userStatuses.put(identifier, updateStatus);
            sendMessage(Succ.Message.newBuilder()
                    .setMessageType(Succ.Message.MessageType.C_UPD)
                    .addUsers(updateStatus)
                    .build());
        }
    }

    @Override
    public void userDisconnected(User us) {
        String identifier = us.getIdentifier();
        if(userStatuses.keySet().contains(identifier) && userStatuses.get(identifier).getStatus() != Succ.Message.Status.BLOCKED) {
            Succ.Message.UserStatus updateStatus = Succ.Message.UserStatus.newBuilder()
                    .mergeFrom(userStatuses.get(identifier))
                    .setStatusValue(Succ.Message.Status.OFFLINE_VALUE)
                    .setUsername(us.getUsername())
                    .build();
            userStatuses.put(identifier, updateStatus);
            sendMessage(Succ.Message.newBuilder()
                    .setMessageType(Succ.Message.MessageType.C_UPD)
                    .addUsers(updateStatus)
                    .build());
        }
    }
}
