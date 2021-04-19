/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * FXML Controller class
 *
 * @author HP
 */
public class HalamanMapeController implements Initializable {

    modelPeramalan TSSheet;
    int[] timeseries;
    LocalDate[] tanggal;
    double nilaiMape;
    double[] hargaPrediksi,ep;
    

    @FXML
    private BorderPane outerBorderpane;
    @FXML
    private TextField lokasiTimeseries;
    @FXML
    private TableView<Mape> tabelDataPrediksi;
    @FXML
    private TableColumn<Mape, String> kolomTanggal;
    @FXML
    private TableColumn<Mape, String> kolomHargaPrediksi;
    @FXML
    private TableColumn<Mape, String> kolomHargaAsli;
    @FXML
    private TableColumn<Mape, String> kolomMape;
    @FXML
    private LineChart<String, Number> dataChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private Label mape;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfInt = new DecimalFormat("0");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setTSSheet(modelPeramalan TSSheet) {
        this.TSSheet = TSSheet;
    }

    @FXML
    public void importData(ActionEvent e) throws IOException {
        File excelFile;
        String defaultCurrentDirectoryPath = "C:\\Users\\HP\\Desktop";
        FileChooser excelFileChooser = new FileChooser();
        excelFileChooser.setTitle("Open Excel File");
        excelFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel File", "*.xlsx")
        );
        excelFile = excelFileChooser.showOpenDialog(outerBorderpane.getScene().getWindow());

        if (excelFile == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Silahkan Memasukkan File Excel Yang Akan Digunakan.");
            alert.showAndWait();
            return;
        } else if (!excelFile.getName().endsWith("xlsx")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("File yang Dimasukkan Tidak Dapat Dibaca.");
            alert.showAndWait();
        } else {
            lokasiTimeseries.setText(excelFile.getAbsolutePath());
            getSheet(excelFile);
            hitungNilaiMape();
            displayData();
        }
    }

    public void displayData() {
        displayTabelMape();
        displayLineChart();
    }
    
    private void hitungNilaiMape(){
        hargaPrediksi = new double[timeseries.length-1]; 
        ep = new double[timeseries.length-1]; 
        double totalEp = 0;
        for (int i = 0; i < timeseries.length; i++) {
            if (i > 0) {
                hargaPrediksi[i-1] = TSSheet.hitungPrediksiHarga(timeseries[i - 1], timeseries[i]);
                ep[i-1] = TSSheet.errorPercent(timeseries[i], hargaPrediksi[i-1]);
                totalEp += ep[i-1];
            }
        }
        nilaiMape = totalEp / (timeseries.length - 1);
    }


    public void getSheet(File excelFile) throws IOException {
        int index = -1;
        Sheet currentSheet;
        Workbook workbooks = WorkbookFactory.create(excelFile);
        String[] strs = new String[workbooks.getNumberOfSheets()];
        for (int i = 0; i < strs.length; i++) {
            strs[i] = workbooks.getSheetName(i);
        }
        ChoiceDialog<String> sheetDialog = new ChoiceDialog<String>(strs[0]);
        ObservableList<String> sheetList = sheetDialog.getItems();
        for (int i = 0; i < strs.length; i++) {
            sheetList.add(strs[i]);
        }
        sheetDialog.setTitle("Konfirmasi");
        sheetDialog.setContentText("Pilih Sheet yang Digunakan :");
        sheetDialog.setHeaderText("Pilih Excel Sheet");
        sheetDialog.showAndWait();
        for (int i = 0; i < strs.length; i++) {
            if (workbooks.getSheetName(i).equalsIgnoreCase(sheetDialog.getSelectedItem())) {
                index = i;
            }
        }
        currentSheet = workbooks.getSheet(strs[index]);
        getDataFromSheet(currentSheet);
        workbooks.close();

    }

    private void getDataFromSheet(Sheet timeseriesSheet) {

        int datasetLength = timeseriesSheet.getLastRowNum();

        Date[] time = new Date[datasetLength];
        timeseries = new int[datasetLength];
        this.tanggal = new LocalDate[datasetLength];

        for (int i = 0; i < datasetLength; i++) {
            Row r = timeseriesSheet.getRow(i + 1);
            time[i] = r.getCell(0).getDateCellValue();
            Instant instant = time[i].toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            this.tanggal[i] = zdt.toLocalDate();
            timeseries[i] = (int) r.getCell(1).getNumericCellValue();
        }
    }

    public void displayTabelMape() {
        kolomTanggal.setCellValueFactory(cellData -> cellData.getValue().getWaktu());
        kolomHargaPrediksi.setCellValueFactory(cellData -> cellData.getValue().getHargaPrediksi());
        kolomHargaAsli.setCellValueFactory(cellData -> cellData.getValue().getHargaAsli());
        kolomMape.setCellValueFactory(cellData -> cellData.getValue().getMape());
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        ObservableList<Mape> data = FXCollections.observableArrayList();
        double totalEp = 0;
        for (int i = 0; i < timeseries.length; i++) {
            if (i > 0) {
                System.out.println("FV :" + String.valueOf(timeseries[i - 1]) + ", SV:" + String.valueOf(timeseries[i]));
                data.add(new Mape(
                        tanggal[i].format(formatters),
                        String.valueOf(timeseries[i]),
                        String.valueOf(hargaPrediksi[i-1]),
                        df.format(ep[i-1])
                ));
            } else {
                data.add(new Mape(
                        tanggal[i].format(formatters),
                        String.valueOf(timeseries[i]),
                        "-",
                        "-"
                ));
            }
        }
        mape.setText(df.format(nilaiMape) + "%");
        tabelDataPrediksi.setItems(data);
    }

    private void displayLineChart() {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        yAxis.setLabel("Harga Beras(Rp)");
        xAxis.setLabel("Bulan/Tahun");
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        series1.setName("Harga Beras Aktual");
        series2.setName("Harga Beras Prediksi");
        dataChart.setLegendSide(Side.BOTTOM);
        for (int i = 0; i < timeseries.length; i++) {
            series1.getData().add(new XYChart.Data(this.tanggal[i].format(formatters), timeseries[i]));
            if (i > 1) {
                series2.getData().add(new XYChart.Data(this.tanggal[i].format(formatters), hargaPrediksi[i-1]));
            }

        }

        dataChart.getData().addAll(series1, series2);
    }

    public void downloadModel() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Model Peramalan.txt", false));
            writer.write(Double.toString(TSSheet.getLimitAtas()));
            writer.newLine();
            writer.write(Double.toString(TSSheet.getPanjangInterval()));
            writer.newLine();
            for (int i = 0; i < TSSheet.getNilaiDefuzzifikasi().length; i++) {
                writer.write(Double.toString(TSSheet.getNilaiDefuzzifikasi()[i]));
                writer.newLine();
            }
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Pemberitahuan");
            alert.setHeaderText(null);
            alert.setContentText("Export FLRG Berhasil. Silahkan Lihat Folder Program");
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void goProsesPeramalan() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "ProsesPeramalan.fxml"
                )
        );
        Stage stage = App.getStage();
        stage.setScene(
                new Scene(loader.load())
        );
        ProsesPeramalanController proPerCont = loader.getController();
        proPerCont.setTSSheet(TSSheet);
        proPerCont.setD1(TSSheet.getD1());
        proPerCont.setD2(TSSheet.getD2());
        proPerCont.displayProsesPeramalan();
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
//        halPerCont.setTSSheet(TSSheet);
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
}
