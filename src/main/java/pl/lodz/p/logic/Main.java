package pl.lodz.p.logic;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String input = "ala ma kota, psa i telewizor";

        Elgamal elgamal = new Elgamal();
        byte[] enciphered = elgamal.encipher(input.getBytes());
        IO.println(Arrays.toString(enciphered));
//        byte[] deciphered = elgamal.decipher(enciphered);

//        IO.println(Arrays.toString(deciphered));
    }

}
