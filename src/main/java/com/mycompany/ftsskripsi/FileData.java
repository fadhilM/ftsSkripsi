/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import java.text.DecimalFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author HP
 */
class FileData {
    private SimpleStringProperty data;
    private SimpleStringProperty time;
    private SimpleStringProperty fuzzifikasi;
    private SimpleStringProperty percentChange;
    private SimpleStringProperty flr;
    
    private static DecimalFormat dfInt = new DecimalFormat("0");

    public FileData(String data, String time) {
        this.data = new SimpleStringProperty(data);
        this.time = new SimpleStringProperty(time);
    }
    
    public FileData(String data, String time, String percentChange) {
        this.data = new SimpleStringProperty(data);
        this.time = new SimpleStringProperty(time);
        this.percentChange = new SimpleStringProperty(percentChange);
    }
    
    public FileData(String percentChange, String fuzzifikasi,boolean tr) {
        this.percentChange = new SimpleStringProperty(percentChange);
        this.fuzzifikasi = new SimpleStringProperty(fuzzifikasi);
    }
    
    public FileData(String percentChange, String fuzzifikasi,String flr, boolean al){
        this.percentChange = new SimpleStringProperty(percentChange);
        this.fuzzifikasi = new SimpleStringProperty(fuzzifikasi);
        this.flr = new SimpleStringProperty(flr);
        
    }

    public SimpleStringProperty getFlr() {
        return flr;
    }
    
    public SimpleStringProperty getPercentChange() {
        return percentChange;
    }
    
    public SimpleStringProperty getData() {
        return data;
    }

    public SimpleStringProperty getTime() {
        return time;
    }

    public void setData(SimpleStringProperty data) {
        this.data = data;
    }

    public void setTime(SimpleStringProperty waktu) {
        this.time = waktu;
    }

    public SimpleStringProperty getFuzzifikasi() {
        return fuzzifikasi;
    }

    public void setFuzzifikasi(SimpleStringProperty fuzzifikasi) {
        this.fuzzifikasi = fuzzifikasi;
    }
    
    
    
    
}
