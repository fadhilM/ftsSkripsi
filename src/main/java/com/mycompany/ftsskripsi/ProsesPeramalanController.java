/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author HP
 */
public class ProsesPeramalanController implements Initializable {

    private modelPeramalan TSSheet;
    private double d1, d2;

    @FXML
    private Label totalData;
    @FXML
    private Label persentasePerubahanMin;
    @FXML
    private Label persentasePerubahanMax;
    @FXML
    private Label d1Label;
    @FXML
    private Label d2Label;
    @FXML
    private Label limitBawah;
    @FXML
    private Label limitAtas;
    @FXML
    private Label totalInterval;
    @FXML
    private Label panjangInterval;
    @FXML
    private TableView<FileData> TabelData;
    @FXML
    private TableColumn<FileData, String> kolomTanggal;
    @FXML
    private TableColumn<FileData, String> kolomData;
    @FXML
    private TableColumn<FileData, String> kolomPersentasePerubahan;
    @FXML
    private TableView<Interval> tabelInterval;
    @FXML
    private TableColumn<Interval, Integer> kolomInterval;
    @FXML
    private TableColumn<Interval, String> kolomRange;
    @FXML
    private TableColumn<Interval, String> kolomMedian;
    @FXML
    private TableView<FileData> tabelFuzzy;
    @FXML
    private TableColumn<FileData, String> kolomPersentasePerubahan2;
    @FXML
    private TableColumn<FileData, String> kolomFuzzy;
    @FXML
    private TableView<FileData> FlrTable;
    @FXML
    private TableColumn<FileData, String> percentChange3Column;
    @FXML
    private TableColumn<FileData, String> fuzzification2Column;
    @FXML
    private TableColumn<FileData, String> flrColumn;
    @FXML
    private TableView<Interval> FlrgTable;
    @FXML
    private TableColumn<Interval, Integer> interval2Column;
    @FXML
    private TableColumn<Interval, String> flrgColumn;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfInt = new DecimalFormat("0");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setTSSheet(modelPeramalan TSSheet) {
        this.TSSheet = TSSheet;
    }

    public double getD1() {
        return d1;
    }

    public void setD1(double d1) {
        this.d1 = d1;
    }

    public double getD2() {
        return d2;
    }

    public void setD2(double d2) {
        this.d2 = d2;
    }

    public void displayProsesPeramalan() {
        displayAditionalInformation();
        displayPercentChangeToTable(TSSheet.getTanggal());
        displayInterval();
        displayFuzzifikasi();
        displayFlr();
        displayFlrg();
    }

    private void displayAditionalInformation() {
        totalData.setText(String.valueOf(TSSheet.getDatasetLength()));
        persentasePerubahanMin.setText(df.format(TSSheet.getMinPercentChange()));
        persentasePerubahanMax.setText(df.format(TSSheet.getPersentasePerubahanMax()));
        d1Label.setText(String.valueOf(d1));
        d2Label.setText(String.valueOf(d2));
        limitBawah.setText(df.format(TSSheet.getLimitBawah()));
        limitAtas.setText(df.format(TSSheet.getLimitAtas()));
        totalInterval.setText(dfInt.format(TSSheet.getTotalInterval()));
        panjangInterval.setText(df.format(TSSheet.getPanjangInterval()));
    }

    private void displayPercentChangeToTable(LocalDate[] date) {
        kolomTanggal.setCellValueFactory(cellData -> cellData.getValue().getTime());
        kolomData.setCellValueFactory(cellData -> cellData.getValue().getData());
        kolomPersentasePerubahan.setCellValueFactory(cellData -> cellData.getValue().getPercentChange());
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        ObservableList<FileData> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getTimeseries().length; i++) {
            if (i == 0) {
                data.add(new FileData(
                        String.valueOf(TSSheet.getTimeseries()[i]),
                        date[i].format(formatters),
                        "-"
                ));
            } else {
                data.add(new FileData(
                        String.valueOf(TSSheet.getTimeseries()[i]),
                        date[i].format(formatters),
                        df.format(TSSheet.getPersentasePerubahan()[i - 1])
                ));
            }

        }
        TabelData.setItems(data);
    }

    private void displayInterval() {
        kolomInterval.setCellValueFactory(cellData -> cellData.getValue().getInterval().asObject());
        kolomRange.setCellValueFactory(cellData -> cellData.getValue().getRange());
        kolomMedian.setCellValueFactory(cellData -> cellData.getValue().getMedian());
        ObservableList<Interval> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getFuzzySet().length - 1; i++) {
            data.add(new Interval(
                    i + 1,
                    df.format(TSSheet.getFuzzySet()[i]),
                    df.format(TSSheet.getFuzzySet()[i + 1]),
                    df.format((TSSheet.getFuzzySet()[i] + TSSheet.getFuzzySet()[i + 1]) / 2)
            ));
        }
        tabelInterval.setItems(data);
    }

    private void displayFuzzifikasi() {
        kolomPersentasePerubahan2.setCellValueFactory(cellData -> cellData.getValue().getPercentChange());
        kolomFuzzy.setCellValueFactory(cellData -> cellData.getValue().getFuzzifikasi());
        ObservableList<FileData> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getPersentasePerubahan().length; i++) {
            data.add(new FileData(
                    df.format(TSSheet.getPersentasePerubahan()[i]),
                    "A" + dfInt.format(TSSheet.getNilaiFuzzy()[i] + 1),
                    true
            ));
        }
        tabelFuzzy.setItems(data);
    }

    private void displayFlr() {
        percentChange3Column.setCellValueFactory(cellData -> cellData.getValue().getPercentChange());
        fuzzification2Column.setCellValueFactory(cellData -> cellData.getValue().getFuzzifikasi());
        flrColumn.setCellValueFactory(cellData -> cellData.getValue().getFlr());
        ObservableList<FileData> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getPersentasePerubahan().length - 1; i++) {
            data.add(new FileData(
                    df.format(TSSheet.getPersentasePerubahan()[i]),
                    "A" + dfInt.format(TSSheet.getNilaiFuzzy()[i]),
                    "A" + dfInt.format(TSSheet.getNilaiFuzzy()[i]) + " -> " + "A" + dfInt.format(TSSheet.getNilaiFuzzy()[i + 1]),
                    true
            ));
        }
        data.add(new FileData(
                df.format(TSSheet.getPersentasePerubahan()[TSSheet.getPersentasePerubahan().length - 1]),
                "A" + dfInt.format(TSSheet.getNilaiFuzzy()[TSSheet.getPersentasePerubahan().length - 1]),
                " - ",
                true
        ));
        FlrTable.setItems(data);
    }

    private void displayFlrg() {
        String flrg = "[ ]";
        interval2Column.setCellValueFactory(cellData -> cellData.getValue().getInterval().asObject());
        flrgColumn.setCellValueFactory(cellData -> cellData.getValue().getFlrg());
        ObservableList<Interval> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getTotalInterval(); i++) {
            for (int j = 0; j < TSSheet.getFlrg()[i].size(); j++) {
            }
        }
        for (int i = 0; i < TSSheet.getTotalInterval(); i++) {
            if (TSSheet.getFlrg()[i].size() != 0) {
                flrg = "[ ";
                if (TSSheet.getFlrg()[i].size() == 1) {

                    flrg = flrg.concat(dfInt.format(TSSheet.getFlrg()[i].get(0) + 1) + " ]");
                } else {
                    for (int j = 0; j < TSSheet.getFlrg()[i].size() - 1; j++) {
                        flrg = flrg.concat(dfInt.format(TSSheet.getFlrg()[i].get(j) + 1) + ", ");
                    }
                    flrg = flrg.concat(dfInt.format(TSSheet.getFlrg()[i].get(TSSheet.getFlrg()[i].size() - 1) + 1) + " ]");
                }
            }
            data.add(new Interval(
                    i + 1,
                    flrg
            ));
        }
        FlrgTable.setItems(data);
    }

    public void displayHalamanMape() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "halamanMape.fxml"
                )
        );
        Stage stage = App.getStage();
        stage.setScene(
                new Scene(loader.load())
        );

        HalamanMapeController halPerCont = loader.getController();
        halPerCont.setTSSheet(TSSheet);
        stage.show();
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
        halPerCont.setTSSheet(TSSheet);
        halPerCont.displayData();
        stage.show();
    }

    @FXML
    public void goHalamanPeramalan() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "halamanPeramalan.fxml"
                )
        );
        Stage stage = App.getStage();
        stage.setScene(
                new Scene(loader.load())
        );

        HalamanPeramalanController halPerCont = loader.getController();
        stage.show();
    }

}
