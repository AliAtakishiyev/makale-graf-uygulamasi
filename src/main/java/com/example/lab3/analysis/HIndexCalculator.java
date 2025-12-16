package com.example.lab3.analysis;

import com.example.lab3.models.ArticleGraph;
import com.example.lab3.models.ArticleNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bir makale için h-index, h-core ve h-median hesaplamasını yapan yardımcı sınıf.
 *
 * Bu hesaplama sadece bellekteki tam graf (ArticleGraph) üzerinde yapılır.
 */
public class HIndexCalculator {

    /**
     * Verilen makale id'si için h-index, h-core ve h-median hesaplar.
     *
     * @param graph  Tam makale grafı
     * @param paperId Merkez makale id'si
     * @return HIndexResult
     */
    public static HIndexResult computeForArticle(ArticleGraph graph, String paperId) {
        ArticleNode center = graph.getNode(paperId);
        if (center == null) {
            throw new IllegalArgumentException("Verilen id ile makale bulunamadı: " + paperId);
        }

        // 1) Bu makaleye atıf yapan makaleleri (incoming) al
        List<ArticleNode> citers = new ArrayList<>(center.getIncoming());

        if (citers.isEmpty()) {
            // Hiç atıf yoksa h-index = 0, h-median = 0, h-core boş
            return new HIndexResult(0, 0.0, List.of());
        }

        // 2) Her bir citer'ın kendisinin aldığı atıf sayısını (in-degree) hesapla
        //    ve bu değere göre azalan sırada sırala
        citers.sort(Comparator.comparingInt(ArticleNode::getInDegree).reversed());

        List<Integer> citationCounts = new ArrayList<>();
        for (ArticleNode node : citers) {
            citationCounts.add(node.getInDegree());
        }

        // 3) h-index'i bul
        int h = 0;
        for (int i = 0; i < citationCounts.size(); i++) {
            int c = citationCounts.get(i);
            int candidateH = i + 1;
            if (c >= candidateH) {
                h = candidateH;
            } else {
                break;
            }
        }

        if (h == 0) {
            return new HIndexResult(0, 0.0, List.of());
        }

        // 4) h-core: en çok atıf alan ilk h makale
        List<ArticleNode> hCore = new ArrayList<>(citers.subList(0, h));

        // 5) h-median: h-core makalelerinin atıf sayılarının ortancası
        List<Integer> hCoreCitations = citationCounts.subList(0, h);
        double hMedian;
        if (h % 2 == 1) {
            // tek sayıda eleman -> ortadaki
            hMedian = hCoreCitations.get(h / 2);
        } else {
            // çift sayıda eleman -> ortadaki iki değerin ortalaması
            int mid1 = hCoreCitations.get(h / 2 - 1);
            int mid2 = hCoreCitations.get(h / 2);
            hMedian = (mid1 + mid2) / 2.0;
        }

        return new HIndexResult(h, hMedian, hCore);
    }
}


