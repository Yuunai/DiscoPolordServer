package discopolord.security;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static sun.security.pkcs11.wrapper.Functions.toHexString;

public class DHServer {

    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
    private KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
    private KeyFactory keyFactory = KeyFactory.getInstance("DH");

    public DHServer() throws NoSuchAlgorithmException {
        keyPairGenerator.initialize(512);
    }

    public byte[] getPublicKeys() throws InvalidKeyException {
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        keyAgreement.init(keyPair.getPrivate());

        return keyPair.getPublic().getEncoded();
    }

    public String getSecret(byte[] otherPublicKeyEncoded) throws InvalidKeySpecException, InvalidKeyException {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(otherPublicKeyEncoded);
        PublicKey otherPublicKey = keyFactory.generatePublic(x509KeySpec);
        keyAgreement.doPhase(otherPublicKey, true);

        return toHexString(keyAgreement.generateSecret());
    }
}


