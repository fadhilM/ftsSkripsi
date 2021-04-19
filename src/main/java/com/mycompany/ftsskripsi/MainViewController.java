/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * FXML Controller class
 *
 * @author HP
 */
public class MainViewController implements Initializable {

    SidebarController sc;
    HeaderController h;
    private modelPeramalan TSSheet;

    @FXML
    BorderPane outerBorderpane;
    @FXML
    BorderPane innerBorderpane;

    @FXML
    private Button importedData;
    @FXML
    private AnchorPane thisPane;
    @FXML
    private TextField excelPathTree;
    @FXML
    private Button downloadTemplate;
    @FXML
    private Button calculate;

    @FXML
    private ChoiceDialog chooseExcelSheet;
    @FXML
    private TableView<FileData> sheetDataTable;
    @FXML
    private TableColumn<FileData, String> timeColumn;
    @FXML
    private TableColumn<FileData, String> dataColumn;
    @FXML
    private Label totalDataDisplay;
    @FXML
    private Label maxDataDisplay;
    @FXML
    private Label minDataDisplay;
    @FXML
    private Label minPercentChangeDisplay;
    @FXML
    private Label maxPercentChangeDisplay;
    @FXML
    private LineChart<String, Number> dataChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private TextField d1Textfield;
    @FXML
    public TextField header;
    @FXML
    private TextField d2Textfield;
    private double d1, d2;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfInt = new DecimalFormat("0");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setTSSheet(modelPeramalan TSSheet) {
        this.TSSheet = TSSheet;
    }

    public void displayData() {
        displayDatatoTable(TSSheet.getTanggal());
        displaySheetInformation();
        displayLineChart(TSSheet.getTanggal());
        excelPathTree.setText(TSSheet.getDataPath());
        d1Textfield.setText(String.valueOf(TSSheet.getD1()));
        d2Textfield.setText(String.valueOf(TSSheet.getD2()));
    }

    public void setupPane(String mainPane, String headerTxt) throws IOException {
        FXMLLoader sidebar = App.FXMLoad("sidebar");
        FXMLLoader header = App.FXMLoad("header");
        FXMLLoader pane = App.FXMLoad(mainPane);

        outerBorderpane.setLeft(sidebar.load());
        innerBorderpane.setTop(header.load());
        innerBorderpane.setCenter(pane.load());

        sc = sidebar.getController();
        h = header.getController();

        h.setHeader(headerTxt);
    }

    @FXML
    public void importData(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        File excelFile;
        FileChooser excelFileChooser = new FileChooser();
        excelFileChooser.setTitle("Open Excel File");
        excelFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel File", "*.xlsx")
        );
        excelFile = excelFileChooser.showOpenDialog(thisPane.getScene().getWindow());
        if (!excelFile.getName().endsWith("xlsx")) {
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("File Yang Diimport Tidak Berextensi .xlsx");
            alert.showAndWait();
        } else {
            excelPathTree.setText(excelFile.getAbsolutePath());
            getSheet(excelFile);
            TSSheet.setImpor(true);
            TSSheet.setDataPath(excelFile.getAbsolutePath());
        }
    }

    public void getSheet(File excelFile) {
        int index = -1;
        Sheet currentSheet;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        try {
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
            TSSheet = new modelPeramalan(currentSheet);
            workbooks.close();
            displayDatatoTable(TSSheet.getTanggal());
            displaySheetInformation();
            displayLineChart(TSSheet.getTanggal());
        } catch (NegativeArraySizeException nae) {
            alert.setContentText("File Yang Diimport Tidak Ada Isinya");
            alert.showAndWait();
        } catch (EncryptedDocumentException ex) {
            alert.setContentText("File Yang Diimport Gagal di Proses"
                    + " Karena Terenkripsi");
            alert.showAndWait();
        } catch (IOException ex) {
             alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }

    }

    private void displayDatatoTable(LocalDate[] date) {
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().getTime());
        dataColumn.setCellValueFactory(cellData -> cellData.getValue().getData());
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        ObservableList<FileData> data = FXCollections.observableArrayList();
        for (int i = 0; i < TSSheet.getDatasetLength(); i++) {
            data.add(new FileData(String.valueOf(TSSheet.getTimeseries()[i]), date[i].format(formatters)));
        }
        sheetDataTable.setItems(data);
    }

    private void displaySheetInformation() {
        totalDataDisplay.setText(String.valueOf(TSSheet.getDatasetLength()));
        maxDataDisplay.setText(String.valueOf(TSSheet.getMaxData()));
        minDataDisplay.setText(String.valueOf(TSSheet.getMinData()));
        minPercentChangeDisplay.setText(df.format(TSSheet.getMinPercentChange()) + "%");
        maxPercentChangeDisplay.setText(df.format(TSSheet.getPersentasePerubahanMax()) + "%");
    }

    private void displayLineChart(LocalDate[] date) {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        yAxis.setLabel("Harga Beras(Rp)");
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Harga Beras");
        for (int i = 0; i < TSSheet.getDatasetLength(); i++) {
            series1.getData().add(new XYChart.Data(date[i].format(formatters), TSSheet.getTimeseries()[i]));
        }
        dataChart.getData().addAll(series1);
    }

    @FXML
    private void openProsesPeramalan() {
        if (TSSheet != null) {
            if (TSSheet.isImpor()) {
                if (validate(d1Textfield.getText()) && validate(d2Textfield.getText())) {
                    d1 = Double.valueOf(d1Textfield.getText());
                    d2 = Double.valueOf(d2Textfield.getText());
                    TSSheet.hitungModel(d1, d2);
                    try {
                        displayProsesPeramalanPane();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UTILITY);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("D1/D2 tidak valid, silahkan masukkan kembali");
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Silahkan import data timeseries yang akan digunakan");
            alert.showAndWait();
        }
    }

    public void exportExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Persons");

        Row header = sheet.createRow(0);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("tanggal");

        headerCell = header.createCell(1);
        headerCell.setCellValue("Harga");

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("d/m/yy ")
        );

        Instant instant = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.now();
        Date date = java.util.Date.from(zdt.toInstant());

        Row row = sheet.createRow(1);
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(date);
        cell1.setCellStyle(cellStyle);

        cell1 = row.createCell(1);
        cell1.setCellValue(9000);

        Row row2 = sheet.createRow(2);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(date);
        cell2.setCellStyle(cellStyle);

        cell2 = row2.createCell(1);
        cell2.setCellValue(9500);

        File contoh = new File(".");
        String path = contoh.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "contohTableExcel.xlsx";
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }

    private boolean validate(String inputText) {
        return inputText.matches("^[\\+\\-]{0,1}[0-9]+[\\.\\,]{1}[0-9]+$") || inputText.matches("^([+-]?[1-9]\\d*|0)$");
    }

    public void displayProsesPeramalanPane() throws IOException {
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
        TSSheet.setProses(true);
        proPerCont.setTSSheet(TSSheet);
        proPerCont.setD1(d1);
        proPerCont.setD2(d2);
        proPerCont.displayProsesPeramalan();
        stage.show();
    }

    @FXML
    public void goProsesPeramalan() throws IOException {
        openProsesPeramalan();
    }

    @FXML
    public void goHalamanMape() throws IOException {
        if (TSSheet.isProses()) {
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
        halPerCont.setTSSheet(TSSheet);
        stage.show();

    }

}
