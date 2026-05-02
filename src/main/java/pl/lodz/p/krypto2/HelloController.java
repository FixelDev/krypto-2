package pl.lodz.p.krypto2;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import pl.lodz.p.logic.Dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class HelloController {
    @FXML
    private Text outputFileInfoText;
    @FXML
    private Text inputFileInfoText;
    @FXML
	private Button loadInputFileButton;
    @FXML
	private TextArea inputTextArea;
    @FXML
	private Button encipherButton;
    @FXML
	private Button decipherButton;
    @FXML
	private Button saveOutputFileButton;
    @FXML
	private TextArea outputTextArea;
    @FXML
    private Button saveKeyButton;
    @FXML
    private TextField keyTextField;
    @FXML
    private Button generateKeyButton;
    @FXML
    private Button loadKeyButton;
    @FXML
    private ToggleGroup inputTypeGroup;

//    private KeyGenerator keyGenerator;
    private byte[] keyBytes;
    private byte[] inputDataBytes;
    private byte[] outputDataBytes;

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

//        final int MAX_CHARS = 24;
        keyTextField.setTextFormatter(new TextFormatter<String>(change -> {
//            if (change.getControlNewText().length() <= MAX_CHARS) {
//                return change;
//            }
//            return null;
            return change;
        }));

//        keyGenerator = new KeyGenerator();

        generateKeyButton.setOnAction(event -> generateKey());

        loadKeyButton.setOnAction(event -> {
            loadKeyFromFile();
        });

        saveKeyButton.setOnAction(event -> {
            saveKeyToFile();
        });

        encipherButton.setOnAction(event -> {
            keyBytes = keyTextField.getText().getBytes();

            if(isKeyInvalid()) return;

            DATA_TYPE selectedDataType = getSelectedDataType();

            switch (selectedDataType) {
                case DATA_TYPE.FILE:
                    break;
                case DATA_TYPE.TEXT:
                    break;
            }

        });

        decipherButton.setOnAction(event -> {
            keyBytes = keyTextField.getText().getBytes();

            if (isKeyInvalid()) return;

            DATA_TYPE selectedDataType = getSelectedDataType();

            switch (selectedDataType) {
                case DATA_TYPE.FILE:
                    break;
                case DATA_TYPE.TEXT:
                    break;
            }

        });

        loadInputFileButton.setOnAction(event -> {
            loadFile();
        });

        saveOutputFileButton.setOnAction(event -> {
            saveFile();
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

    private boolean isKeyInvalid() {
        if (keyBytes.length != 24) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Podany klyucz nie ma 24 bajtów. Podaj poprawny klucz");
            return true;
        }
        return false;
    }

    private void saveFile() {
        Window mainWindow = loadKeyButton.getScene().getWindow();
        Path path = null;
        try {
            path = chooseFilePath(mainWindow, "Zapisz plik", MODE.SAVE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Dao.writeToFile(path, outputDataBytes);
    }

    private void loadFile() {
        Window mainWindow = loadKeyButton.getScene().getWindow();
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

    private void saveKeyToFile() {
        Window mainWindow = loadKeyButton.getScene().getWindow();
        Path path = null;
        try {
            path = chooseFilePath(mainWindow, "Zapisz klucz", MODE.SAVE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        keyBytes = keyTextField.getText().getBytes();
        Dao.writeToFile(path, keyBytes);
    }

    private void loadKeyFromFile() {
        Window mainWindow = loadKeyButton.getScene().getWindow();
        Path path = null;
        try {
            path = chooseFilePath(mainWindow, "Wczutaj plik z kluczem", MODE.LOAD);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            keyBytes = Dao.readFromFile(path);

            updateKeyTextField();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long[] keyBytesToLong() {
        long[] key = new long[3];

        for (int i = 0; i < 3; i++) {
            byte[] keyB = new byte[8];
            System.arraycopy(keyBytes, i * 8, keyB, 0, 8);


        }
        return key;
    }

    private void encipher() {
        if (inputDataBytes == null || inputDataBytes.length == 0) {
            throw new NullPointerException();
        }


    }

    private void decipher() {
        if (inputDataBytes == null || inputDataBytes.length == 0) {
            throw new NullPointerException();
        }


    }

    private void generateKey() {
//        keyBytes = keyGenerator.getRandomKey();
//        updateKeyTextField();
    }

    private void updateKeyTextField() {
        String keyString = new String(keyBytes, StandardCharsets.UTF_8);
        keyTextField.setText(keyString);

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
