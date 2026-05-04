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

//        BigInteger[] dataBlocks = divideEncipheredIntoBlocks(data);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        for (int i = 0; i < dataBlocks.length - 1; i++) {
//            BigInteger c1 = dataBlocks[i];
//            BigInteger c2 = dataBlocks[i + 1];
//
//            BigInteger decipheredBlock = c2.multiply(c1.modPow(a, p).modInverse(p)).mod(p);
//            byte[] decipheredBlockBytes = decipheredBlock.toByteArray();
//
//            if (decipheredBlockBytes.length > 0 && decipheredBlockBytes[0] == 0) {
//                outputStream.write(decipheredBlockBytes, 1, decipheredBlockBytes.length - 1);
//            } else {
//                outputStream.writeBytes(decipheredBlockBytes);
//            }
//        }
//
//        return outputStream.toByteArray();
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

    private BigInteger[] divideEncipheredIntoBlocks(byte[] data) {
        List<BigInteger> blocks = new ArrayList<>();

        int index = 0;

        while (index < data.length) {
            short blockLength = data[index];
            blockLength = (short) ((blockLength << 8) | data[index + 1]);
            BigInteger block = new BigInteger(Arrays.copyOfRange(data, index + 2, index + blockLength + 2));
            blocks.add(block);

            index += blockLength + 2;
        }

        return blocks.toArray(new BigInteger[0]);
    }

    public BigInteger[] divideEncipheredIntoBlocks2(byte[] data) {
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




