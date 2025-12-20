package com.example.lab3.analysis;

import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KCoreHesaplama {

    public static List<MakaleDugumu> computeKCore(MakaleGrafı graph, int k) { // Ana Metod
        List<MakaleDugumu> activeNodes = new ArrayList<>(graph.getAllNodes()); // Kopyalama

        Set<MakaleDugumu> activeSet = new HashSet<>(activeNodes);

        boolean removedAny;
        do {
            removedAny = false;

            List<MakaleDugumu> toRemove = new ArrayList<>(); // Silinecekler listesi

            for (MakaleDugumu node : activeNodes) {
                int currentDegree = calculateDegreeInSet(node, activeSet);

                if (currentDegree < k) {
                    toRemove.add(node);
                }
            }

            if (!toRemove.isEmpty()) {
                activeNodes.removeAll(toRemove);
                activeSet.removeAll(toRemove);
                removedAny = true;
            }

        } while (removedAny);

        return activeNodes; // K şartı sağlanan düğümleri dönderiyoruz
    }


    private static int calculateDegreeInSet(MakaleDugumu node, Set<MakaleDugumu> activeSet) {
        int degree = 0;

        for (MakaleDugumu source : node.getIncoming()) {
            if (activeSet.contains(source)) {
                degree++;
            }
        }

        for (MakaleDugumu target : node.getOutgoing()) {
            if (activeSet.contains(target)) {
                degree++;
            }
        }

        return degree;
    }


    public static List<MakaleDugumu> computeLocalKCore(List<MakaleDugumu> visibleNodes, int k) {
        List<MakaleDugumu> activeNodes = new ArrayList<>(visibleNodes);

        // Hızlı arama için Set
        Set<MakaleDugumu> activeSet = new HashSet<>(activeNodes);

        boolean removedAny;
        do {
            removedAny = false;
            List<MakaleDugumu> toRemove = new ArrayList<>();

            for (MakaleDugumu node : activeNodes) {
                int currentDegree = calculateDegreeInSet(node, activeSet);

                if (currentDegree < k) {
                    toRemove.add(node);
                }
            }

            if (!toRemove.isEmpty()) {
                activeNodes.removeAll(toRemove);
                activeSet.removeAll(toRemove);
                removedAny = true;
            }

        } while (removedAny);

        return activeNodes;
    }
}