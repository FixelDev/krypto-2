package pl.lodz.p.logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        String input = "ala, ma kota psa i telewizor i ąąąąąą";
        IO.println(Arrays.toString(input.getBytes()));

        Elgamal elgamal = new Elgamal();

        File file = new File("E:\\programowanie\\projekty\\studia\\krypto-2\\src\\main\\java\\pl\\lodz\\p\\logic\\sprawozdanie.pdf");
        Path path = Paths.get(file.getAbsolutePath());

        byte[] fileBytes = Files.readAllBytes(path);

        File fileOut = new File("E:\\programowanie\\projekty\\studia\\krypto-2\\src\\main\\java\\pl\\lodz\\p\\logic\\sprawozdanie-out.pdf");
        Path pathOut = Paths.get(fileOut.getAbsolutePath());

        try {
            byte[] enciphered = elgamal.encipher(fileBytes);
//            IO.println(Arrays.toString(enciphered));

            byte[] deciphered = elgamal.decipher(enciphered);
            Files.write(pathOut, deciphered);
//            IO.println(new String(deciphered, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
