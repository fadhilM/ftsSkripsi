/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author HP
 */
public class HalamanPeramalanController implements Initializable {

    modelPeramalan TSSheet;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfInt = new DecimalFormat("0");

    @FXML
    private BorderPane outerBorderPane;
    @FXML
    private TextField modelFilePath;
    @FXML
    private TextField price;
    @FXML
    private TextField priceIPlus1;
    @FXML
    private TextField predictionPrice;
    @FXML
    private TableView<Interval> intervalTable;
    @FXML
    private TableColumn<Interval, Integer> intervalColumn;
    @FXML
    private TableColumn<Interval, String> rangeColumn;
    @FXML
    private TableColumn<Interval, String> medianColumn;
    @FXML
    private TableView<Interval> defuzzifikasiTable;
    @FXML
    private TableColumn<Interval, String> nilaiFuzzyColumn;
    @FXML
    private TableColumn<Interval, String> defuzzifikasiColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void importData(ActionEvent e) throws IOException {
        File modelFts;
        String defaultCurrentDirectoryPath = "C:\\Users\\HP\\Desktop";
        FileChooser modelFileChooser = new FileChooser();
        modelFileChooser.setTitle("Open Model Peramalan");
        modelFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Model Peramalan", "*.txt")
        );
        modelFts = modelFileChooser.showOpenDialog(outerBorderPane.getScene().getWindow());
        if (!modelFts.getName().endsWith(".txt")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("File yang Dimasukkan Tidak Dapat Dibaca.");
            alert.showAndWait();
        } else {
            modelFilePath.setText(modelFts.getAbsolutePath());
            Scanner reader = new Scanner(modelFts);
            double max = Double.valueOf(reader.nextLine());
            double intervalLength = Double.valueOf(reader.nextLine());
            ArrayList<Double> ftsModel = new ArrayList<Double>();
            while (reader.hasNextLine()) {
                ftsModel.add(Double.valueOf(reader.nextLine()));
            }
            reader.close();
            TSSheet = new modelPeramalan(max, intervalLength, ftsModel);
            displayData();
        }

    }

    public modelPeramalan getTSSheet() {
        return TSSheet;
    }

    public void setTSSheet(modelPeramalan TSSheet) {
        this.TSSheet = TSSheet;
    }

    public void displayData() {
        displayTableFuzzySet();
        displayDefuzzifikasi();
    }

    public void displayTableFuzzySet() {
        intervalColumn.setCellValueFactory(cellData -> cellData.getValue().getInterval().asObject());
        rangeColumn.setCellValueFactory(cellData -> cellData.getValue().getRange());
        medianColumn.setCellValueFactory(cellData -> cellData.getValue().getMedian());
        ObservableList<Interval> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getFuzzySet().length - 1; i++) {
            data.add(new Interval(
                    i + 1,
                    df.format(TSSheet.getFuzzySet()[i]),
                    df.format(TSSheet.getFuzzySet()[i + 1]),
                    df.format((TSSheet.getFuzzySet()[i] + TSSheet.getFuzzySet()[i + 1]) / 2)
            ));
        }
        intervalTable.setItems(data);
    }

    public void displayDefuzzifikasi() {
        defuzzifikasiColumn.setCellValueFactory(cellData -> cellData.getValue().getFlrg());
        nilaiFuzzyColumn.setCellValueFactory(cellData -> cellData.getValue().getNilaiFuzzy());
        ObservableList<Interval> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getNilaiDefuzzifikasi().length; i++) {
            data.add(new Interval(
                    "A" + String.valueOf(i + 1),
                    df.format(TSSheet.getNilaiDefuzzifikasi()[i])
            ));
        }
        defuzzifikasiTable.setItems(data);
    }

    private boolean validate(String inputText) {
        return inputText.matches("^[1-9][0-9]*|0$");
    }

    public void prediksi() {
        String price = this.price.getText();
        String priceIPlus1 = this.priceIPlus1.getText();
        if (TSSheet == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Silahkan Memasukkan Model Peramalan Yang Akan Digunakan.");
            alert.showAndWait();
            return;
        } else if (validate(price) && validate(priceIPlus1)) {
            int p = Integer.valueOf(price);
            int pIPlus1 = Integer.valueOf(priceIPlus1);
            int predictedPrice = TSSheet.hitungPrediksiHarga(p, pIPlus1);
            predictionPrice.setText(dfInt.format(predictedPrice));
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Salah Satu Harga Yang Diperlukan Kosong Atau Salah");
            alert.showAndWait();
        }

    }

    @FXML
    public void goMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "mainView.fxml"
                )
        );
        Stage stage = App.getStage();
        stage.setScene(
                new Scene(loader.load())
        );

        MainViewController halPerCont = loader.getController();
        stage.show();
    }
}
