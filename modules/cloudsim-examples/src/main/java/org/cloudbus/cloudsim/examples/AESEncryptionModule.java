package org.cloudbus.cloudsim.examples;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class AESEncryptionModule {

    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String ENCODING = "UTF-8";
    private static final byte[] IV = new byte[16];

    private SecretKeySpec secretKeySpec;

    public AESEncryptionModule(String secretKey) {
        byte[] key = secretKey.getBytes();
        secretKeySpec = new SecretKeySpec(key, AES);
    }

    public String encrypt(String dataToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] encryptedData = cipher.doFinal(dataToEncrypt.getBytes(ENCODING));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String dataToDecrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] originalData = cipher.doFinal(Base64.getDecoder().decode(dataToDecrypt));
        return new String(originalData, ENCODING);
    }

    public void encryptFile(String inputFilePath, String outputFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(new File(inputFilePath));
        FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] buffer = new byte[1024];
        int read;
        while ((read = fis.read(buffer)) != -1) {
            fos.write(cipher.update(buffer, 0, read));
        }
        fos.write(cipher.doFinal());
        fis.close();
        fos.close();
    }

    public void decryptFile(String inputFilePath, String outputFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(new File(inputFilePath));
        FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] buffer = new byte[1024];
        int read;
        while ((read = fis.read(buffer)) != -1) {
            fos.write(cipher.update(buffer, 0, read));
        }
        fos.write(cipher.doFinal());
        fis.close();
        fos.close();
    }

    public static void main(String[] args) {
        try {
            // Initialize
            String secretKey = "1234567890123456";
            AESEncryptionModule aesModule = new AESEncryptionModule(secretKey);

            // Store file lengths for later
            ArrayList<Long> fileLengths = new ArrayList<>();

            // Concatenate files specified in command-line arguments
            FileOutputStream concatFos = new FileOutputStream("concatenated");
            for (String filePath : args) {
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    concatFos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                fis.close();
                fileLengths.add(totalBytes);
            }
            concatFos.close();

            // Encrypt concatenated file
            aesModule.encryptFile("concatenated", "encrypted.enc");

            // Decrypt the encrypted file
            aesModule.decryptFile("encrypted.enc", "decrypted");

            // Split the decrypted file back into original files
            FileInputStream fis = new FileInputStream("decrypted");
            long totalBytesRead = 0;
            for (int i = 0; i < fileLengths.size(); i++) {
                FileOutputStream fos = new FileOutputStream("decrypted_" + (i + 1) + ".jpg");
                byte[] buffer = new byte[1024];
                long remainingBytes = fileLengths.get(i);

                while (remainingBytes > 0) {
                    int bytesRead = fis.read(buffer, 0, (int) Math.min(remainingBytes, 1024));
                    if (bytesRead == -1) {
                        break;
                    }
                    fos.write(buffer, 0, bytesRead);
                    remainingBytes -= bytesRead;
                    totalBytesRead += bytesRead;
                }
                fos.close();
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
