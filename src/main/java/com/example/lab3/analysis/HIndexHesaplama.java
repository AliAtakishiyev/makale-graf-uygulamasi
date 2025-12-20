package com.example.lab3.analysis;

import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HIndexHesaplama {

    public static HIndexSonuc computeForArticle(MakaleGrafı graph, String paperId) {
        MakaleDugumu center = graph.getNode(paperId);


        if (center == null) {
            throw new IllegalArgumentException("Verilen id ile makale bulunamadı: " + paperId);
        }


        List<MakaleDugumu> citers = new ArrayList<>(center.getIncoming()); // Atıflar listelenir.

        if (citers.isEmpty()) {
            return new HIndexSonuc(0, 0.0, List.of()); // Atıf yok
        }


        citers.sort(Comparator.comparingInt(MakaleDugumu::getInDegree).reversed()); // Atıfları sıralıyoruz.


        List<Integer> citationCounts = new ArrayList<>(); // Sayıları listeliyoruz.
        for (MakaleDugumu node : citers) {
            citationCounts.add(node.getInDegree());
        }

        int h = 0;
        for (int i = 0; i < citationCounts.size(); i++) { // Hindex bulma algoritması
            int c = citationCounts.get(i); // Atıf Sayısı
            int candidateH = i + 1;        // Sıra Numarası

            if (c >= candidateH) {
                h = candidateH;
            } else {
                break;
            }
        }

        if (h == 0) {
            return new HIndexSonuc(0, 0.0, List.of());
        }

        List<MakaleDugumu> hCore = new ArrayList<>(citers.subList(0, h));

        List<Integer> hCoreCitations = citationCounts.subList(0, h);
        double hMedian;

        if (h % 2 == 1) {
            hMedian = hCoreCitations.get(h / 2);
        } else {
            int mid1 = hCoreCitations.get(h / 2 - 1);
            int mid2 = hCoreCitations.get(h / 2);
            hMedian = (mid1 + mid2) / 2.0;
        }

        return new HIndexSonuc(h, hMedian, hCore);
    }
}