package com.example.lab3.analysis;

import com.example.lab3.models.MakaleDugumu;

import java.util.List;


public class HIndexSonuc {

    private final int hIndex;
    private final double hMedian;
    private final List<MakaleDugumu> hCoreNodes;

    public HIndexSonuc(int hIndex, double hMedian, List<MakaleDugumu> hCoreNodes) {
        this.hIndex = hIndex;
        this.hMedian = hMedian;
        this.hCoreNodes = hCoreNodes;
    }

    public int getHIndex() {
        return hIndex;
    }

    public double getHMedian() {
        return hMedian;
    }

    public List<MakaleDugumu> getHCoreNodes() {
        return hCoreNodes;
    }
}


