package com.example.lab3.analysis;

import com.example.lab3.models.ArticleNode;

import java.util.List;

/**
 * Bir makale için h-index, h-core ve h-median sonuçlarını tutar.
 */
public class HIndexResult {

    private final int hIndex;
    private final double hMedian;
    private final List<ArticleNode> hCoreNodes;

    public HIndexResult(int hIndex, double hMedian, List<ArticleNode> hCoreNodes) {
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

    public List<ArticleNode> getHCoreNodes() {
        return hCoreNodes;
    }
}


