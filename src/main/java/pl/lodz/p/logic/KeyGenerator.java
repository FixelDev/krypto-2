package pl.lodz.p.logic;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyGenerator {
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger a;
    private final BigInteger h;

    public KeyGenerator(int keyBytesSize) {
        SecureRandom secureRandom = new SecureRandom();

        this.p = BigInteger.probablePrime(keyBytesSize * 8, secureRandom);
        this.g = Utils.generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        this.a = Utils.generateRandomNumber(BigInteger.TWO, p.subtract(BigInteger.TWO));
        this.h = g.modPow(a, p);
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getH() {
        return h;
    }
}
