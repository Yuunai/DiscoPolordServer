package discopolord.security;

public class DHTest {

    public static void main(String[] args) {
        try {
            DHServer dhServer = new DHServer();
            DHClient dhClient = new DHClient();

            long start = System.currentTimeMillis();
            byte[] serverPublicKeys = dhServer.getPublicKeys();

            byte[] clientPublicKeys = dhClient.getPublicKey(serverPublicKeys);

            String serverSecret = dhServer.getSecret(clientPublicKeys);
            String clientSecret = dhClient.getSecret();

            System.out.println(serverSecret);
            System.out.println(clientSecret);

            System.out.println("Time: " + (System.currentTimeMillis() - start)/1000.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
