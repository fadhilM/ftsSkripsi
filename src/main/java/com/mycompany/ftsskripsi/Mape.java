/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author HP
 */
class Mape {
    private SimpleStringProperty waktu;
    private SimpleStringProperty hargaAsli;
    private SimpleStringProperty hargaPrediksi;
    private SimpleStringProperty mape;
    
    public Mape(String waktu, String hargaAsli, String hargaPrediksi, String mape) {
        this.waktu = new SimpleStringProperty(waktu);
        this.hargaAsli= new SimpleStringProperty(hargaAsli);
        this.hargaPrediksi = new SimpleStringProperty(hargaPrediksi);
        this.mape = new SimpleStringProperty(mape);
        
    }

    public SimpleStringProperty getWaktu() {
        return waktu;
    }

    public SimpleStringProperty getHargaAsli() {
        return hargaAsli;
    }

    public SimpleStringProperty getHargaPrediksi() {
        return hargaPrediksi;
    }

    public SimpleStringProperty getMape() {
        return mape;
    }
    
    
}
