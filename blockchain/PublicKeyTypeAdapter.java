package blockchain;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicKeyTypeAdapter extends TypeAdapter<PublicKey> {

    @Override
    public void write(JsonWriter out, PublicKey value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Convert the PublicKey to a Base64 encoded string
        String encodedKey = Base64.getEncoder().encodeToString(value.getEncoded());
        out.value(encodedKey);
    }

    @Override
    public PublicKey read(JsonReader in) throws IOException {
        String encodedKey = in.nextString();
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new IOException("Failed to decode PublicKey", e);
        }
    }
}
