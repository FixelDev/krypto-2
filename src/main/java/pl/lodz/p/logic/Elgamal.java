package pl.lodz.p.logic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Elgamal {
    private final int KEY_BYTE_SIZE = 64;
    private final int MAX_BLOCK_BYTE_SIZE;

    private final SecureRandom secureRandom;
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger a;
    private final BigInteger h;

    public Elgamal() {
        secureRandom = new SecureRandom();
        p = BigInteger.probablePrime(KEY_BYTE_SIZE * 8, secureRandom);
        g = generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        a = generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        h = g.modPow(a, p);
        MAX_BLOCK_BYTE_SIZE = KEY_BYTE_SIZE - 1;
    }


    private BigInteger generateRandomNumber(BigInteger minNumber, BigInteger maxNumber) {
        BigInteger randomNumber;

        do {
            randomNumber = new BigInteger(maxNumber.bitLength(), secureRandom);
        } while (randomNumber.compareTo(minNumber) < 0 || randomNumber.compareTo(maxNumber) > 0);

        return randomNumber;
    }

    public byte[] encipher(byte[] data) throws IOException {
        ByteArrayOutputStream encipheredDataByteStream = new ByteArrayOutputStream();
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        while (dataBuffer.hasRemaining()) {
            int availableBytes = Math.min(MAX_BLOCK_BYTE_SIZE, dataBuffer.remaining());
            byte[] blockBytes = new byte[availableBytes];
            dataBuffer.get(blockBytes);

            BigInteger block = new BigInteger(1, blockBytes);

            BigInteger r = generateRandomNumber(BigInteger.ONE, p.subtract(BigInteger.ONE));
            BigInteger c1 = encipherBlockC1(r);
            BigInteger c2 = encipherBlockC2(block, r);

            byte[] c1Bytes = Utils.bigIntegerToByteArray(c1);
            byte[] c2Bytes = Utils.bigIntegerToByteArray(c2);
            int originalBlockSize = blockBytes.length;

            int c1Size = c1Bytes.length;
            int c2Size = c2Bytes.length;

            ByteBuffer encipheredBlockBuffer = ByteBuffer.allocate(4 + 4 + c1Size + 4 + c2Size);
            encipheredBlockBuffer.putInt(originalBlockSize);
            encipheredBlockBuffer.putInt(c1Size);
            encipheredBlockBuffer.put(c1Bytes);
            encipheredBlockBuffer.putInt(c2Size);
            encipheredBlockBuffer.put(c2Bytes);

            encipheredDataByteStream.write(encipheredBlockBuffer.array());
        }

        return encipheredDataByteStream.toByteArray();
    }

    private BigInteger encipherBlockC1(BigInteger r) {
        return g.modPow(r, p);
    }

    private BigInteger encipherBlockC2(BigInteger block, BigInteger r) {
        return block.multiply(h.modPow(r, p)).mod(p);
    }

    public byte[] decipher(byte[] data) throws IOException {
        ByteArrayOutputStream decipheredDataByteStream = new ByteArrayOutputStream();
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        while (dataBuffer.hasRemaining()) {
            int originalBlockSize = dataBuffer.getInt();

            int c1Size = dataBuffer.getInt();
            byte[] c1Bytes = new byte[c1Size];
            dataBuffer.get(c1Bytes);
            BigInteger c1 = new BigInteger(1, c1Bytes);

            int c2Size = dataBuffer.getInt();
            byte[] c2Bytes = new byte[c2Size];
            dataBuffer.get(c2Bytes);
            BigInteger c2 = new BigInteger(1, c2Bytes);

            BigInteger decipheredBlock = c2.multiply(c1.modPow(a, p).modInverse(p)).mod(p);
            byte[] decipheredBlockBytes = Utils.bigIntegerToByteArray(decipheredBlock);
            int decipheredBlockSize = decipheredBlockBytes.length;

            byte[] originalBlockBytes = new byte[originalBlockSize];

            int offset = originalBlockSize - decipheredBlockSize;

            System.arraycopy(decipheredBlockBytes, 0, originalBlockBytes, offset, decipheredBlockSize);

            decipheredDataByteStream.write(originalBlockBytes);
        }

        return decipheredDataByteStream.toByteArray();
    }
}