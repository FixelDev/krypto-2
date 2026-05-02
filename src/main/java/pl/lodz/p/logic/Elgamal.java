package pl.lodz.p.logic;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Elgamal {
    private final SecureRandom secureRandom;

    public Elgamal() {
        secureRandom = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(2048, secureRandom);
        BigInteger g = generateG(p);
        BigInteger a = generateG(p);
        BigInteger h = g.modPow(a, p);
    }

    private BigInteger generateG(BigInteger p) {
        BigInteger min = BigInteger.TWO;
        BigInteger max = p.subtract(BigInteger.TWO);

        BigInteger g;

        do {
            g = new BigInteger(p.bitLength(), secureRandom);
        } while (g.compareTo(min) < 0 || g.compareTo(max) > 0);

        return g;
    }
}
