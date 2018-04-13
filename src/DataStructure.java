import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

public class DataStructure {
    private static byte[] getTemplate() {
        byte[] message = new byte[256];
        //signature
        message[0] = 0x41;
        message[1] = 0x53;
        message[2] = 0x43;
        message[3] = 0x50;
        message[4] = 0x00;
        message[5] = 0x01;

        return message;
    }

    public static byte[] getPlainMessage(String text) {
        byte[] message = getTemplate();
        byte[] plainText;
        try {
            if (text.length() > 236) {
                String sub = text.substring(0, 236);
                plainText = sub.getBytes("UTF-8");
            } else {
                plainText = text.getBytes("UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return message;
        }

        for (int i = 0; i < plainText.length; i++) {
            message[i + 20] = plainText[i];
        }

        message[9] = new Integer(plainText.length).byteValue();

        return message;
    }

    public static String getMessage(byte[] data) {
        String message = "";

        for (int i = 0; i < unsignedToBytes(data[9]); ++i) {
            message = message + Character.toString((char) data[20 + i]);
        }

        return message;
    }

    public static byte[] encryptData(byte[] data, String key) throws Exception {
        /*System.out.println(key);
        int missingZeroes = 8 - key.length();
        String completedKey = "";
        for(int i = 0; i < missingZeroes; i++) {
            completedKey += "0";
        }
        completedKey += key;

        System.out.println(completedKey);

        key = completedKey;*/
        System.out.println("full key: "+key);

        byte[] keyBytes = key.getBytes();

        SecretKeySpec secret = new SecretKeySpec(keyBytes, "DES");


        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);

        return cipher.doFinal(data);
    }

    public static byte[] decryptData(byte[] data, String key) throws Exception {
        System.out.println(key);
        int missingZeroes = 8 - key.length();
        String completedKey = "";
        for(int i = 0; i < missingZeroes; i++) {
            completedKey += "0";
        }
        completedKey += key;

        key = completedKey;

        byte[] keyBytes = key.getBytes();
        SecretKeySpec secret = new SecretKeySpec(keyBytes, "DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secret);

        return cipher.doFinal(data);
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }


}
