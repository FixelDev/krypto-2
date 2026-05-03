package pl.lodz.p.logic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class Elgamal {
    private final SecureRandom secureRandom;
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger a;
    private final BigInteger h;

    public Elgamal() {
        secureRandom = new SecureRandom();
        p = BigInteger.probablePrime(2048, secureRandom);
        g = generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        a = generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        h = g.modPow(a, p);
    }

    private BigInteger generateRandomNumber(BigInteger minNumber, BigInteger maxNumber) {
        BigInteger randomNumber;

        do {
            randomNumber = new BigInteger(maxNumber.bitLength(), secureRandom);
        } while (randomNumber.compareTo(minNumber) < 0 || randomNumber.compareTo(maxNumber) > 0);

        return randomNumber;
    }

    public byte[] encipher(byte[] data) {
        BigInteger[] dataBlocks = divideIntoBlocks(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(outputStream)) {

            for (BigInteger block : dataBlocks) {
                BigInteger r = generateRandomNumber(BigInteger.ONE, p.subtract(BigInteger.ONE));

                BigInteger c1 = encipherBlockC1(block, r);
                BigInteger c2 = encipherBlockC2(block, r);

                byte[] c1Bytes = c1.toByteArray();
                byte[] c2Bytes = c2.toByteArray();


                IO.println("C1: " + c1Bytes.length);
                IO.println("C2: " + c2Bytes.length);

                if (c1Bytes.length > 0 && c1Bytes[0] == 0) {
                    dos.writeInt(c1Bytes.length - 1);
                    dos.write(c1Bytes, 1, c1Bytes.length - 1);
                } else {
                    dos.writeInt(c1Bytes.length);
                    dos.write(c1Bytes);
                }

                if (c2Bytes.length > 0 && c2Bytes[0] == 0) {
                    dos.writeInt(c2Bytes.length - 1);
                    dos.write(c2Bytes, 1, c2Bytes.length - 1);
                } else {
                    dos.writeInt(c2Bytes.length);
                    dos.write(c2Bytes);
                }
            }

            dos.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private BigInteger encipherBlockC1(BigInteger block, BigInteger r) {
        return g.modPow(r, p);
    }

    private BigInteger encipherBlockC2(BigInteger block, BigInteger r) {
        return block.multiply(h.modPow(r, p)).mod(p);
    }

    public byte[] decipher(byte[] data) {
        BigInteger[] dataBlocks = divideEncipheredIntoBlocks(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < dataBlocks.length - 1; i++) {
            BigInteger c1 = dataBlocks[i];
            BigInteger c2 = dataBlocks[i + 1];

            BigInteger decipheredBlock = c2.multiply(c1.modPow(a, p).modInverse(p)).mod(p);
            byte[] decipheredBlockBytes = decipheredBlock.toByteArray();

            if (decipheredBlockBytes.length > 0 && decipheredBlockBytes[0] == 0) {
                outputStream.write(decipheredBlockBytes, 1, decipheredBlockBytes.length - 1);
            } else {
                outputStream.writeBytes(decipheredBlockBytes);
            }
        }

        return outputStream.toByteArray();
    }


    public BigInteger[] divideIntoBlocks(byte[] data) {
        int blocksCount = data.length / 255;

        int finalBlocksCount = blocksCount;
        if ((blocksCount * 255) < data.length ) {
            finalBlocksCount += 1;
        }

        BigInteger[] result = new BigInteger[finalBlocksCount];

        for (int i = 0; i < blocksCount; i++) {
            result[i] = new BigInteger(Arrays.copyOfRange(data, i * 255, (i + 1) * 255));
        }

        if (finalBlocksCount > blocksCount) {
            int i = finalBlocksCount - 1;
            result[i] = new BigInteger(Arrays.copyOfRange(data, i * 255, (i + 1) * 255));
        }

        return result;
    }


    public BigInteger[] divideEncipheredIntoBlocks(byte[] data) {

        int c1_count = 0;
        int i = 0;
        while (i < data.length){


            int c1length = data[i];
            c1length <<= 8;
            c1length = c1length | data[i + 1] & 0xFF;


            IO.println("C1: " + c1length);
            int c2length = data[i + 1 + c1length];
            c2length <<= 8;
            c2length = c2length | data[i + c1length + 1] & 0xFF;

            IO.println("C2: " + c2length);

            int c2_end = i + 1 + c1length + 1 + c2length;
            c1_count++;
            i = c2_end;
        }


        BigInteger[] result = new BigInteger[c1_count];

        int currentElement = 0;
        i = 0;
        while (i < data.length){
            int c1length = data[i];
            c1length <<= 8;
            c1length = c1length | data[i + 1] & 0xFF;


            IO.println("C1: " + c1length);
            int c2length = data[i + 1 + c1length];
            c2length <<= 8;
            c2length = c2length | data[i + c1length + 1] & 0xFF;

            IO.println("C2: " + c2length);


            int c1_end = i + 1 + c1length;
            int c2_end = c1_end + 1 + c2length;
            BigInteger c1 = new BigInteger(Arrays.copyOfRange(data, i, c1_end));
            BigInteger c2 = new BigInteger(Arrays.copyOfRange(data, c1_end, c2_end));

            i = c2_end;

            result[currentElement] = c1;
            result[currentElement + 1] = c2;
            currentElement += 2;
        }

        return result;
    }

}




