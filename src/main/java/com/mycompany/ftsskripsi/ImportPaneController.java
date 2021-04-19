/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import static com.mycompany.ftsskripsi.App.FXMLoad;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * FXML Controller class
 *
 * @author HP
 */
public class ImportPaneController implements Initializable {

    private modelPeramalan TSSheet;
    boolean imported = false;
//
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
    private TextField d2Textfield;
    private double d1, d2;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfInt = new DecimalFormat("0");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
        excelFile = excelFileChooser.showOpenDialog(thisPane.getScene().getWindow());

        if (excelFile == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select any Excel file.");
            alert.showAndWait();
            return;
        } else if (!excelFile.getName().endsWith("xlsx")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select only Excel file.");
            alert.showAndWait();
        } else {
            excelPathTree.setText(excelFile.getAbsolutePath());
            getSheet(excelFile);
            imported = true;
        }
    }
//

    public void getSheet(File excelFile) throws IOException {
        int index = -1;
        Sheet currentSheet;
        Workbook workbooks = WorkbookFactory.create(excelFile);
        String[] strs = new String[workbooks.getNumberOfSheets()];
        //get all sheet name from selected workbook
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
        displayDatatoTable(TSSheet.getTimeseries(), TSSheet.getTanggal());
        displaySheetInformation(
                TSSheet.getDatasetLength(),
                TSSheet.getMaxData(),
                TSSheet.getMinData(),
                TSSheet.getPersentasePerubahanMax(),
                TSSheet.getMinPercentChange()
        );
        displayLineChart(TSSheet.getTimeseries(), TSSheet.getTanggal());
        workbooks.close();
    }

    private void displayDatatoTable(int[] inputData, LocalDate[] date) {
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().getTime());
        dataColumn.setCellValueFactory(cellData -> cellData.getValue().getData());
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        ObservableList<FileData> data = FXCollections.observableArrayList();
        for (int i = 0; i < inputData.length; i++) {
            data.add(new FileData(String.valueOf(inputData[i]), date[i].format(formatters)));
        }
        sheetDataTable.setItems(data);
//        System.out.println("aaa");
    }

    private void displaySheetInformation(int totalData, int max, int min, double maxPercentChange, double minPercentChange) {
        totalDataDisplay.setText(String.valueOf(totalData));
        maxDataDisplay.setText(String.valueOf(max));
        minDataDisplay.setText(String.valueOf(min));
        minPercentChangeDisplay.setText(df.format(minPercentChange) + "%");
        maxPercentChangeDisplay.setText(df.format(maxPercentChange) + "%");
    }

    private void displayLineChart(int[] inputData, LocalDate[] date) {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM/uuuu");
        yAxis.setLabel("Harga Beras(Rp)");
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Harga Beras");
        for (int i = 0; i < inputData.length; i++) {
            series1.getData().add(new XYChart.Data(date[i].format(formatters), inputData[i]));
        }
        dataChart.getData().addAll(series1);
    }

    @FXML
    private void openProsesPeramalan() {
        if (imported) {
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
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Silahkan import data timeseries yang akan digunakan");
            alert.showAndWait();
        }

    }

    private boolean validate(String inputText) {
        return inputText.matches("^[\\+\\-]{0,1}[0-9]+[\\.\\,]{1}[0-9]+$") || inputText.matches("^([+-]?[1-9]\\d*|0)$");
    }
    
    public void displayProsesPeramalanPane() throws IOException {
        FXMLLoader prosesPeramalanLoader = App.FXMLoad("prosesPeramalan");
        Parent ppp = (Parent) prosesPeramalanLoader.load();
        ProsesPeramalanController ppc = prosesPeramalanLoader.getController();
        FXMLLoader loader = App.FXMLoad("prosesPeramalan");
        Parent mv = (Parent) loader.load();
        MainViewController mvc = loader.getController();
        mvc.setupPane("prosesPeramalan", "Halaman Proses Peramalan");

        App.getStage().setScene(
                new Scene(loader.load())
        );
        ppc.setTSSheet(TSSheet);
        ppc.setD1(d1);
        ppc.setD2(d2);
        App.getStage().show();  
        ppc.displayProsesPeramalan();
    }
    
}
