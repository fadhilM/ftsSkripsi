package com.mycompany.ftsskripsi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.*;
import java.util.Date;

public class modelPeramalan {

    private Date[] waktu;
    private LocalDate[] tanggal;
    private String lokasiData;

    private int totalData, min, max;
    private double persentasePerubahanMin, persentasePerubahanMax, totalInterval, limitBawah, limitAtas, rata2, panjangInterval, d1, d2;

    private int[] timeseries;
    private double[] persentasePerubahan, nilaiFuzzy, nilaiDefuzzifikasi;

    private ArrayList<Double>[] flrg;
    private double[] fuzzySet;
    private boolean impor = false, proses = false;

    public modelPeramalan(Sheet timeseriesSheet) {
        getDataFromSheet(timeseriesSheet);
        min = cariDataTerkecil();
        max = cariDataTerbesar();
        hitungPersentasePerubahan();
        persentasePerubahanMin = cariPerubahanDataTerkecil();
        persentasePerubahanMax = cariPerubahanDataTerbesar();
//        checkData();
//      
    }

    public modelPeramalan(double maxPercentChange, double intervalLength, ArrayList<Double> predictedValue) {
        this.limitAtas = maxPercentChange;
        this.panjangInterval = intervalLength;
        int totalInterval = predictedValue.size();
        this.nilaiDefuzzifikasi = new double[totalInterval];
        for (int i = 0; i < predictedValue.size(); i++) {
            this.nilaiDefuzzifikasi[i] = predictedValue.get(i);
        }
        hitungFuzzySet(totalInterval, intervalLength, maxPercentChange - (intervalLength * totalInterval), maxPercentChange);
    }

    public void hitungModel(double d1, double d2) {
        this.d1 = d1;
        this.d2 = d2;
        limitBawah = hitungLimitBawah(persentasePerubahanMin, d1);
        limitAtas = hitungLimitAtas(persentasePerubahanMax, d2);
        rata2 = hitungRata2(persentasePerubahan);
        totalInterval = hitungTotalInterval();
        panjangInterval = hitungPanjangInterval(limitBawah, limitAtas, totalInterval);
        hitungFuzzySet(totalInterval, panjangInterval, limitBawah, limitAtas);
        nilaiFuzzy = fuzzifikasi(fuzzySet);
        hitungFlrg(nilaiFuzzy, totalInterval);
        defuzzifikasi();
    }

    public String getDataPath() {
        return lokasiData;
    }

    public void setDataPath(String dataPath) {
        this.lokasiData = dataPath;
    }

    public double[] getNilaiDefuzzifikasi() {
        return nilaiDefuzzifikasi;
    }

    public double[] getNilaiFuzzy() {
        return nilaiFuzzy;
    }

    public double getTotalInterval() {
        return totalInterval;
    }

    public double getLimitBawah() {
        return limitBawah;
    }

    public double getLimitAtas() {
        return limitAtas;
    }

    public int[] getTimeseries() {
        return timeseries;
    }

    public Date[] getTime() {
        return waktu;
    }

    public LocalDate[] getTanggal() {
        return tanggal;
    }

    public double[] getPersentasePerubahan() {
        return persentasePerubahan;
    }

    public int getMinData() {
        return min;
    }

    public int getMaxData() {
        return max;
    }

    public double getPersentasePerubahanMax() {
        return persentasePerubahanMax;
    }

    public double getMinPercentChange() {
        return persentasePerubahanMin;
    }

    public int getDatasetLength() {
        return totalData;
    }

    public ArrayList<Double>[] getFlrg() {
        return flrg;
    }

    public double[] getFuzzySet() {
        return fuzzySet;
    }

    public double getPanjangInterval() {
        return panjangInterval;
    }

    public double getRata2() {
        return rata2;
    }

    public double getD1() {
        return d1;
    }

    public double getD2() {
        return d2;
    }

    public boolean isImpor() {
        return impor;
    }

    public void setImpor(boolean impor) {
        this.impor = impor;
    }

    public boolean isProses() {
        return proses;
    }

    public void setProses(boolean proses) {
        this.proses = proses;
    }

    private void getDataFromSheet(Sheet sheet) {

        totalData = sheet.getLastRowNum();

        waktu = new Date[totalData];
        timeseries = new int[totalData];
        tanggal = new LocalDate[totalData];

        for (int i = 0; i < totalData; i++) {
            Row r = sheet.getRow(i + 1);
            waktu[i] = r.getCell(0).getDateCellValue();
            Instant instant = waktu[i].toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            tanggal[i] = zdt.toLocalDate();
            timeseries[i] = (int) r.getCell(1).getNumericCellValue();
        }
    }

    private void checkData() {
        System.out.println("===========Data Timeseries===========");
        for (int i = 0; i < timeseries.length; i++) {
            System.out.print(waktu[i]);
            System.out.print(",");
            System.out.println(timeseries[i]);
        }

        System.out.println("===========Data Persentase Perubahan===========");
        for (int i = 0; i < persentasePerubahan.length; i++) {
            System.out.print(i + 1);
            System.out.print("->");
            System.out.println(persentasePerubahan[i]);
        }

        System.out.println("===========Persentase Perubahan Terkecil===========");
        double smallestData = cariPerubahanDataTerkecil();
        System.out.println("Min : " + smallestData);

        System.out.println("===========Persentase Perubahan Terbesar===========");
        double largestData = cariPerubahanDataTerbesar();
        System.out.println("Max : " + largestData);

        System.out.println("===========Nilai Min Baru===========");
        double min = hitungLimitBawah(smallestData, d1);
        System.out.println("New Min : " + min);

        System.out.println("===========Nilai Max Baru===========");
        double max = hitungLimitAtas(largestData, d2);
        System.out.println("New Min : " + max);

        System.out.println("===========Nilai Rata-Rata===========");
        double mean = hitungRata2(persentasePerubahan);
        System.out.println("New Mean : " + mean);

        System.out.println("===========Total Interval===========");
        double totalInterval = hitungTotalInterval();
        System.out.println("Total Interval : " + totalInterval);

        System.out.println("===========Panjang Interval===========");
        double intervalLength = hitungPanjangInterval(min, max, totalInterval);
        System.out.println("Total Interval : " + intervalLength);

        System.out.println("===========Fuzzy Set===========");
        hitungFuzzySet(totalInterval, intervalLength, min, max);
        for (int i = 0; i < intervalLength; i++) {
            System.out.println("Interval ke: " + i);
            System.out.println("Nilai Min : " + fuzzySet[i]);
            System.out.println("Nilai Max : " + fuzzySet[i + 1]);
        }

        System.out.println("===========FLR===========");
        double[] flr = fuzzifikasi(fuzzySet);
        for (int i = 0; i < flr.length; i++) {
            System.out.println("Fuzzyfikasi " + (i + 1) + " : " + flr[i]);
        }

        System.out.println("===========FLRG===========");
        hitungFlrg(flr, totalInterval);
        for (int i = 0; i < flrg.length; i++) {
            System.out.println("-------- Index " + i + " ----------");
            for (int j = 0; j < flrg[i].size(); j++) {
                System.out.println("->" + flrg[i].get(j));
            }
        }
    }

    private void hitungPersentasePerubahan() {
        persentasePerubahan = new double[totalData - 1];
        for (int i = 0; i < totalData - 1; i++) {
            persentasePerubahan[i] = hitungPersentasePerubahanIndividu((double) timeseries[i], timeseries[i + 1]);
        }
    }

    public double hitungPersentasePerubahanIndividu(double data1, double data2) {
        return ((data2 - data1) / data1) * 100;
    }

    private int cariDataTerkecil() {
        int dataTerkecil = timeseries[0];
        for (int i = 0; i < totalData - 1; i++) {
            if (dataTerkecil > timeseries[i]) {
                dataTerkecil = timeseries[i];
            }
        }
        return dataTerkecil;
    }

    private int cariDataTerbesar() {
        int dataTerbesar = timeseries[0];
        for (int i = 0; i < totalData - 1; i++) {
            if (dataTerbesar < timeseries[i]) {
                dataTerbesar = timeseries[i];
            }
        }
        return dataTerbesar;
    }

    private double cariPerubahanDataTerkecil() {
        double dataTerkecil = 0;
        for (int i = 0; i < totalData - 1; i++) {
            if (dataTerkecil > persentasePerubahan[i]) {
                dataTerkecil = persentasePerubahan[i];
            }
        }
        return dataTerkecil;
    }

    private double cariPerubahanDataTerbesar() {
        double dataTerbesar = 0;
        for (int i = 0; i < totalData - 1; i++) {
            if (dataTerbesar < persentasePerubahan[i]) {
                dataTerbesar = persentasePerubahan[i];
            }
        }
        return dataTerbesar;
    }

    private double hitungLimitBawah(double persentasePerubahanMin, double d) {
        double limitBawah = 0;
        limitBawah = persentasePerubahanMin - d;
        return limitBawah;
    }

    private double hitungLimitAtas(double persentasePerubahanMax, double d) {
        double limitAtas = 0;
        limitAtas = persentasePerubahanMax + d;
        return limitAtas;
    }

    private double hitungRata2(double[] persentasePerubahan) {
        double rata2 = 0;
        for (int i = 0; i < totalData - 1; i++) {
            rata2 += persentasePerubahan[i];
        }
        return rata2 / totalData - 1;
    }

    private double hitungTotalInterval() {
        return Math.round(1 + 3.332 * Math.log10(totalData - 1));
    }

    private double hitungPanjangInterval(double min, double max, double totalInterval) {
        double panjangInterval = (max - min) / totalInterval;
        return panjangInterval;
    }

    private void hitungFuzzySet(double totalInterval, double panjangInterval, double min, double max) {
        fuzzySet = new double[(int) totalInterval + 1];
        double tempMin = min;
        for (int i = 0; i < fuzzySet.length; i++) {
            fuzzySet[i] = tempMin;
            tempMin += panjangInterval;
        }
    }

    private double[] fuzzifikasi(double[] fuzzySet) {
        double[] nilaiFuzzy = new double[totalData - 1];
        for (int i = 0; i < nilaiFuzzy.length; i++) {
            for (int j = 0; j < fuzzySet.length; j++) {
                if (persentasePerubahan[i] > fuzzySet[j] && persentasePerubahan[i] <= fuzzySet[j + 1]) {
                    nilaiFuzzy[i] = j;
                }
            }
        }
        return nilaiFuzzy;
    }

    private void hitungFlrg(double[] nilaiFuzzy, double totalInterval) {
        flrg = new ArrayList[(int) totalInterval];
        for (int i = 0; i < (int) totalInterval; i++) {
            ArrayList<Double> flrgroup = new ArrayList<>();
            for (int j = 0; j < nilaiFuzzy.length - 1; j++) {
                if (nilaiFuzzy[j] == i) {
                    if (!flrgroup.contains(nilaiFuzzy[j + 1])) {
                        flrgroup.add(nilaiFuzzy[j + 1]);
                    }
                }
            }
            flrg[i] = flrgroup;
        }
    }

    private void defuzzifikasi() {
        nilaiDefuzzifikasi = new double[flrg.length];
        for (int i = 0; i < flrg.length; i++) {
            if (flrg[i].size() > 1) {
                double temp = 0;
                for (int j = 0; j < flrg[i].size(); j++) {
                    double rVal = flrg[i].get(j);
                    int r = (int) rVal;
                    temp += (fuzzySet[r] + fuzzySet[r + 1]) / 2;
                }
                nilaiDefuzzifikasi[i] = temp / flrg[i].size();
            } else if (flrg[i].size() == 1) {
                double flrIndex = flrg[i].get(0);
                nilaiDefuzzifikasi[i] = (fuzzySet[(int) flrIndex] + fuzzySet[(int) flrIndex + 1]) / 2;
            } else {
                nilaiDefuzzifikasi[i] = (fuzzySet[i] + fuzzySet[i + 1]) / 2;
            }
        }
    }

    private double df(double persentasePerubahan) {
        double prediksiPerubahan = 0;
        boolean isFound = false;
        for (int i = 0; i < fuzzySet.length - 1; i++) {
            if (persentasePerubahan > fuzzySet[i] && persentasePerubahan <= fuzzySet[i + 1]) {
                prediksiPerubahan = this.nilaiDefuzzifikasi[i];
                isFound = true;
            }
        }
        if (isFound == false) {
            double batasAtas = 0;
            double batasBawah = 0;
            if (persentasePerubahan < fuzzySet[0]) {
                batasBawah = fuzzySet[0] - panjangInterval;
                batasAtas = fuzzySet[0];
                while (persentasePerubahan < batasBawah) {
                    batasAtas = batasBawah;
                    batasBawah -= panjangInterval;
                }
            } else {
                batasBawah = fuzzySet[fuzzySet.length - 1];
                batasAtas = fuzzySet[fuzzySet.length - 1] + panjangInterval;
                while (persentasePerubahan > batasAtas) {
                    batasBawah = batasAtas;
                    batasAtas += panjangInterval;
                }
            }
            prediksiPerubahan = (batasBawah + batasAtas) / 2;
        }
        return prediksiPerubahan;
    }

    public int hitungPrediksiHarga(int harga1, int harga2) {
        double persentasePerubahan = hitungPersentasePerubahanIndividu(harga1, harga2);
        double prediksiPerubahan = df(persentasePerubahan);
        return harga1 + (int) Math.round(harga1 * prediksiPerubahan / 100);
    }

    public double errorPercent(int hargaAsli, double hargaPrediksi) {
        double ep = (Math.abs((hargaAsli - hargaPrediksi)) / hargaAsli)*100;
        return ep;
    }

}
