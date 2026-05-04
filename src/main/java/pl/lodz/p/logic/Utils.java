package pl.lodz.p.logic;

import java.math.BigInteger;
import java.util.Arrays;

public final class Utils {
    public static byte[] bigIntegerToByteArray(BigInteger value) {
        byte[] array = value.toByteArray();
        if (array[0] == 0 && array.length > 1) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }

        return array;
    }
}
