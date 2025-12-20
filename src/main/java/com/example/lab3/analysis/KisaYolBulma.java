package com.example.lab3.analysis;

import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;

import java.util.*;

public class KisaYolBulma {

    public static List<MakaleDugumu> findShortestPath(MakaleGrafı graph, String startId, String endId) {

        MakaleDugumu startNode = graph.getNode(startId);
        MakaleDugumu endNode = graph.getNode(endId);


        if (startNode == null || endNode == null) { // ID yok/yanlış
            return Collections.emptyList();
        }

        if (startId.equals(endId)) { //start ve end aynı durumu
            return List.of(startNode);
        }

        Queue<MakaleDugumu> queue = new LinkedList<>(); // kuyruk

        Map<MakaleDugumu, MakaleDugumu> parentMap = new HashMap<>(); // ziyaret edilenler

        queue.add(startNode);
        parentMap.put(startNode, null);

        while (!queue.isEmpty()) {
            MakaleDugumu current = queue.poll(); //ekleme işlemi

            if (current.equals(endNode)) { // endId ile sonraki aynı mı kontrolü
                return reconstructPath(parentMap, endNode);
            }

            for (MakaleDugumu neighbor : current.getOutgoing()) {
                if (!parentMap.containsKey(neighbor)) {
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }


    private static List<MakaleDugumu> reconstructPath(Map<MakaleDugumu, MakaleDugumu> parentMap, MakaleDugumu endNode) {
        List<MakaleDugumu> path = new LinkedList<>();
        MakaleDugumu curr = endNode;

        while (curr != null) {
            path.add(0, curr);
            curr = parentMap.get(curr);
        }
        return path;
    }
}