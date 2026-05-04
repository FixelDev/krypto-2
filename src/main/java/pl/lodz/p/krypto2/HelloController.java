package pl.lodz.p.krypto2;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import pl.lodz.p.logic.Dao;
import pl.lodz.p.logic.Elgamal;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import pl.lodz.p.logic.KeyGenerator;

public class HelloController {
    @FXML private TextField pubKeyP;
    @FXML private TextField pubKeyG;
    @FXML private TextField pubKeyH;
    @FXML private TextField privKeyP;
    @FXML private TextField privKeyX;
    @FXML private Text outputFileInfoText;
    @FXML private Text inputFileInfoText;
    @FXML private Button loadInputFileButton;
    @FXML private TextArea inputTextArea;
    @FXML private Button saveOutputFileButton;
    @FXML private TextArea outputTextArea;
    @FXML private Button generateKeyButton;
    @FXML private ToggleGroup inputTypeGroup;
    @FXML private RadioButton radioEncryptMode;
    @FXML private HBox publicKeyBox;
    @FXML private HBox privateKeyBox;
    @FXML private Button convertButton;
    @FXML private ComboBox<String> keyLengthComboBox;

    private byte[] inputDataBytes;
    private byte[] outputDataBytes;
    private int wantedKeyLength;

    private KeyGenerator keyGenerator;

    private enum MODE{
        SAVE,
        LOAD
    }

    private enum DATA_TYPE{
        TEXT,
        FILE
    }

    @FXML
    public void initialize() {

        radioEncryptMode.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            publicKeyBox.setVisible(isNowSelected);
            privateKeyBox.setVisible(!isNowSelected);
        });

        convertButton.setOnAction(event -> {
            if (radioEncryptMode.isSelected()) {
                if (getSelectedDataType() == DATA_TYPE.TEXT) {
                    encipherText();
                } else {
                    encipherFile();
                }
            } else {
                if (getSelectedDataType() == DATA_TYPE.TEXT) {
                    decipherText();
                } else {
                    decipherFile();
                }
            }
        });

        loadInputFileButton.setOnAction(event -> {
            loadFile();
        });

        saveOutputFileButton.setOnAction(event -> {
            saveFile();
        });

        keyLengthComboBox.getItems().addAll("2048", "1024", "512");
        keyLengthComboBox.getSelectionModel().selectFirst();


        generateKeyButton.setOnAction(event -> {
            wantedKeyLength = Integer.parseInt(keyLengthComboBox.getValue());
            IO.println("Generating key...");
            generateKey();
        });
    }

    private void decipherText() {
        try {
            outputFileInfoText.setText("");
            inputDataBytes = Base64.getDecoder().decode(inputTextArea.getText().getBytes());
            decipher();
            outputTextArea.setText(new String(outputDataBytes, StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Brak danych wejściowych");
        }
    }

    private void decipherFile() {
        try {
            outputFileInfoText.setText("");
            decipher();
            showAlert(Alert.AlertType.INFORMATION, "Gotowe", "Deszyfrowanie zakończone. Możesz pobrać plik");
            outputFileInfoText.setText("Odszyfrowany plik gotowy do pobrania");
        } catch (NullPointerException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Brak danych wejściowych");
        }
    }

    private void encipherText() {
        try {
            outputFileInfoText.setText("");
            inputFileInfoText.setText("");
            inputDataBytes = inputTextArea.getText().getBytes();
            encipher();
            outputTextArea.setText(Base64.getEncoder().encodeToString(outputDataBytes));
        } catch(NullPointerException e) {
            IO.print(e);
            showAlert(Alert.AlertType.ERROR, "Błąd", "Brak danych wejściowych");
        }
    }

    private void encipherFile() {
        try {
            outputFileInfoText.setText("");
            encipher();
            showAlert(Alert.AlertType.INFORMATION, "Gotowe", "Szyfrowanie zakończone. Możesz pobrać plik");
            outputFileInfoText.setText("Zaszyfrowany plik gotowy do pobrania");
        } catch(NullPointerException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Brak danych wejściowych");
        }
    }

    private void showAlert(Alert.AlertType alertType, String alertTitle, String alertContent) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertTitle);
        alert.setContentText(alertContent);
        alert.show();
    }

    private void saveFile() {
        Window mainWindow = loadInputFileButton.getScene().getWindow();
        Path path = null;
        try {
            path = chooseFilePath(mainWindow, "Zapisz plik", MODE.SAVE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Dao.writeToFile(path, outputDataBytes);
    }

    private void loadFile() {
        Window mainWindow = loadInputFileButton.getScene().getWindow();
        Path path = null;
        try {
            path = chooseFilePath(mainWindow, "Wczytaj plik", MODE.LOAD);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        inputDataBytes = Dao.readFromFile(path);
        outputFileInfoText.setText("");
        inputFileInfoText.setText("Załadowano plik " + path.getFileName());
    }

    private void encipher() {
        if (inputDataBytes == null || inputDataBytes.length == 0) {
            throw new NullPointerException();
        }

        Elgamal elgamal = new Elgamal(wantedKeyLength / 8, new BigInteger(pubKeyP.getText().trim(), 16), new BigInteger(pubKeyG.getText().trim(), 16), new BigInteger(privKeyX.getText().trim(), 16), new BigInteger(pubKeyH.getText().trim(), 16));

        try {
            outputDataBytes = elgamal.encipher(inputDataBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decipher() {
        if (inputDataBytes == null || inputDataBytes.length == 0) {
            throw new NullPointerException();
        }

        Elgamal elgamal = new Elgamal(wantedKeyLength / 8, new BigInteger(pubKeyP.getText().trim(), 16), new BigInteger(pubKeyG.getText().trim(), 16), new BigInteger(privKeyX.getText().trim(), 16), new BigInteger(pubKeyH.getText().trim(), 16));
        try {
            outputDataBytes = elgamal.decipher(inputDataBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateKey() {
        keyGenerator = new KeyGenerator(wantedKeyLength / 8);
        updateKeyTextField();
    }

    private void updateKeyTextField() {
        pubKeyP.setText(keyGenerator.getP().toString(16));
        pubKeyG.setText(keyGenerator.getG().toString(16));
        pubKeyH.setText(keyGenerator.getH().toString(16));
        privKeyX.setText(keyGenerator.getA().toString(16));
    }

    private Path chooseFilePath(Window ownerWindow, String windowName, MODE mode) throws FileNotFoundException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowName);

        File selectedFile = switch (mode) {
            case MODE.SAVE -> fileChooser.showSaveDialog(ownerWindow);
            case MODE.LOAD -> fileChooser.showOpenDialog(ownerWindow);
        };


        if (selectedFile != null) {
            return Paths.get(selectedFile.getAbsolutePath());
        }
        throw new FileNotFoundException();
    }

    private DATA_TYPE getSelectedDataType() {
        RadioButton selectedRadioButton = (RadioButton) inputTypeGroup.getSelectedToggle();
        String selectedValue = selectedRadioButton.getText();

        return switch (selectedValue) {
            case "plik" -> DATA_TYPE.FILE;
            default -> DATA_TYPE.TEXT;
        };

    }
}
