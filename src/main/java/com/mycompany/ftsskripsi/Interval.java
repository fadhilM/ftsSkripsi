/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftsskripsi;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author HP
 */
public class Interval {
    private SimpleIntegerProperty interval;
    private SimpleStringProperty range;
    private SimpleStringProperty flrg;
    private SimpleStringProperty median;
    private SimpleStringProperty nilaiFuzzy;
    
    public Interval(int interval, String min, String max, String median) {
        this.interval = new SimpleIntegerProperty(interval);
        this.range = new SimpleStringProperty("[ "+min+" , "+max+" ]");
        this.median = new SimpleStringProperty(median);
    }
    
    public Interval(int interval, String flrg) {
        this.interval = new SimpleIntegerProperty(interval);
        this.flrg = new SimpleStringProperty(flrg);
    }
    
    public Interval(String nilaiFuzzy, String flrg) {
        this.nilaiFuzzy = new SimpleStringProperty(nilaiFuzzy);
        this.flrg = new SimpleStringProperty(flrg);
    }

    public SimpleIntegerProperty getInterval() {
        return interval;
    }

    public SimpleStringProperty getRange() {
        return range;
    }

    public SimpleStringProperty getFlrg() {
        return flrg;
    }

    public SimpleStringProperty getMedian() {
        return median;
    }

    public SimpleStringProperty getNilaiFuzzy() {
        return nilaiFuzzy;
    }
    
    
}
