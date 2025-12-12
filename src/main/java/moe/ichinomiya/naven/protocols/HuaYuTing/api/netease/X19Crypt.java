package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease;

import moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.RandomUtils;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.aes.Aes;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.aes.AesMode;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.aes.PaddingMode;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
public class X19Crypt {
    private static final byte[][] keys;
    private static final String tokenSolt = "0eGsBkhl";
    private static final Aes algorithm = Aes.create();

    static {
        algorithm.mode = AesMode.CBC;
        algorithm.paddingMode = PaddingMode.Zero;

        keys = new byte[16][];
        keys[0] = "MK6mipwmOUedplb6".getBytes(StandardCharsets.US_ASCII);
        keys[1] = "OtEylfId6dyhrfdn".getBytes(StandardCharsets.US_ASCII);
        keys[2] = "VNbhn5mvUaQaeOo9".getBytes(StandardCharsets.US_ASCII);
        keys[3] = "bIEoQGQYjKd02U0J".getBytes(StandardCharsets.US_ASCII);
        keys[4] = "fuaJrPwaH2cfXXLP".getBytes(StandardCharsets.US_ASCII);
        keys[5] = "LEkdyiroouKQ4XN1".getBytes(StandardCharsets.US_ASCII);
        keys[6] = "jM1h27H4UROu427W".getBytes(StandardCharsets.US_ASCII);
        keys[7] = "DhReQada7gZybTDk".getBytes(StandardCharsets.US_ASCII);
        keys[8] = "ZGXfpSTYUvcdKqdY".getBytes(StandardCharsets.US_ASCII);
        keys[9] = "AZwKf7MWZrJpGR5W".getBytes(StandardCharsets.US_ASCII);
        keys[10] = "amuvbcHw38TcSyPU".getBytes(StandardCharsets.US_ASCII);
        keys[11] = "SI4QotspbjhyFdT0".getBytes(StandardCharsets.US_ASCII);
        keys[12] = "VP4dhjKnDGlSJtbB".getBytes(StandardCharsets.US_ASCII);
        keys[13] = "UXDZx4KhZywQ2tcn".getBytes(StandardCharsets.US_ASCII);
        keys[14] = "NIK73ZNvNqzva4kd".getBytes(StandardCharsets.US_ASCII);
        keys[15] = "WeiW7qU766Q1YQZI".getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] encrypt(String deBody) {
        try {
            int index = RandomUtils.nextInt(0, 15);
            byte[] bytes = deBody.getBytes(StandardCharsets.UTF_8);
            byte[] numArray = new byte[16];
            RandomUtils.nextBytes(numArray);
            ByteArrayOutputStream stream = new ByteArrayOutputStream(16 - bytes.length % 16 + bytes.length + 33);
            stream.write(numArray, 0, numArray.length);
            byte[] array;
            algorithm.Key = keys[index];
            algorithm.IV = numArray;

            ByteArrayOutputStream request = new ByteArrayOutputStream();
            {
                request.write(bytes);
                request.write(RandomUtils.randomString(16).getBytes(StandardCharsets.US_ASCII), 0, 16);
                stream.write(algorithm.encrypt(request.toByteArray()));
                request.close();
            }
            stream.write((byte) (index << 4 | 2));
            array = stream.toByteArray();
            stream.close();
            return array;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException | IOException e) {
            return new byte[0];
        }
    }


    public static String decrypt(byte[] enBody) {
        try {
            algorithm.Key = keys[(int) enBody[enBody.length - 1] >> 4 & 15];
            algorithm.IV = Arrays.copyOfRange(enBody, 0, 16);
            byte[] bytes = algorithm.decrypt(enBody, 16, enBody.length - 17);
            for (int index = bytes.length - 1; index >= 0; --index) {
                if (bytes[index] != (byte) 0)
                    return new String(bytes, 0, index - 15);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IOException e) {
        }
        return "";
    }

    public static String computeDynamicToken(String token, String uri, String body) {
        if (!uri.startsWith("/"))
            uri = "/" + uri;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hash1 = bytesToHex(md.digest((bytesToHex(md.digest(token.getBytes(StandardCharsets.UTF_8))) + body + tokenSolt + uri).getBytes(StandardCharsets.UTF_8)));
            String binary = bytesToBinary(hash1.getBytes(StandardCharsets.UTF_8));
            String str = binary.substring(6) + binary.substring(0, 6);

            byte[] bytes = hash1.getBytes(StandardCharsets.UTF_8);
            for (int index1 = 0; index1 < 12; ++index1) {
                byte num = 0;
                int index2 = index1 * 8 + 7;
                for (int index3 = 0; index3 < 8; ++index3) {
                    if (str.charAt(index2) == '1')
                        num |= (byte) (1 << index3);
                    --index2;
                }
                bytes[index1] ^= num;
            }

            StringBuilder stringBuilder = new StringBuilder(17);
            stringBuilder.append(Base64.getEncoder().encodeToString(Arrays.copyOf(bytes, 12)));
            stringBuilder.append('1');
            return stringBuilder.toString().replace('+', 'm').replace('/', 'o');
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String bytesToBinary(byte[] bytes) {
        StringBuilder binaryString = new StringBuilder();
        for (byte aByte : bytes) {
            String binary = Integer.toBinaryString(0xff & aByte);
            while (binary.length() < 8) binary = "0" + binary;
            binaryString.append(binary);
        }
        return binaryString.toString();
    }


    public static String generateMACAddress() {
        String uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
        StringBuilder macAddress = new StringBuilder();
        for (int i = 0; i < uuid.length(); i += 2) {
            macAddress.append(uuid, i, i + 2).append("-");
        }
        macAddress.deleteCharAt(macAddress.length() - 1);
        return macAddress.toString();
    }

    public static String generateUDID() {
        return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public static String generateDiskID() {
        return RandomUtils.random(8, "0123456789ABCDEF");
    }
}
