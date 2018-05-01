package discopolord.security;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static sun.security.pkcs11.wrapper.Functions.toHexString;

public class DHClient {

    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
    private KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
    private KeyFactory keyFac = KeyFactory.getInstance("DH");

    public DHClient() throws NoSuchAlgorithmException {
    }

    public byte[] getPublicKey(byte[] otherPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(otherPublicKey);
        PublicKey otherPubKey = keyFac.generatePublic(x509KeySpec);

        DHParameterSpec otherDhParam = ((DHPublicKey)otherPubKey).getParams();

        keyPairGenerator.initialize(otherDhParam);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(otherPubKey, true);

        return keyPair.getPublic().getEncoded();
    }

    public String getSecret() {
        return toHexString(keyAgreement.generateSecret());
    }

}
