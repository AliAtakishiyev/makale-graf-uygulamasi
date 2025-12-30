package com.example.lab3.analysis;

import com.example.lab3.models.MakaleDugumu;
import java.util.List;

public class HIndexSonuc {

    private final int hIndeksi;
    private final double hMedyan;
    private final List<MakaleDugumu> hCekirdekDugumleri;

    public HIndexSonuc(int hIndeksi, double hMedyan, List<MakaleDugumu> hCekirdekDugumleri) {
        this.hIndeksi = hIndeksi;
        this.hMedyan = hMedyan;
        this.hCekirdekDugumleri = hCekirdekDugumleri;
    }

    public int getHIndex() {
        return hIndeksi;
    }

    public double getHMedian() {
        return hMedyan;
    }

    public List<MakaleDugumu> getHCoreNodes() {
        return hCekirdekDugumleri;
    }
}