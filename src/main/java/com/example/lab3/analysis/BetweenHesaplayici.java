package com.example.lab3.analysis;

import com.example.lab3.models.MakaleDugumu;
import java.util.*;

public class BetweenHesaplayici {
    public static int computeCentrality(List<MakaleDugumu> visibleNodes, MakaleDugumu targetNode) {
        int score = 0;
        int n = visibleNodes.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                MakaleDugumu startNode = visibleNodes.get(i);
                MakaleDugumu endNode = visibleNodes.get(j);

                if (startNode.equals(targetNode) || endNode.equals(targetNode)) {
                    continue;
                }

                List<MakaleDugumu> path = findUndirectedShortestPath(visibleNodes, startNode, endNode);

                if (!path.isEmpty() && path.contains(targetNode)) {
                    score++;
                }
            }
        }
        return score;
    }

    private static List<MakaleDugumu> findUndirectedShortestPath(List<MakaleDugumu> scope, MakaleDugumu start, MakaleDugumu end) {
        if (start.equals(end)) return List.of(start);

        Queue<MakaleDugumu> queue = new LinkedList<>();
        Map<MakaleDugumu, MakaleDugumu> parentMap = new HashMap<>();
        Set<MakaleDugumu> visited = new HashSet<>();

        Set<MakaleDugumu> validNodes = new HashSet<>(scope);

        queue.add(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            MakaleDugumu current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(parentMap, end);
            }


            Set<MakaleDugumu> allNeighbors = new HashSet<>();
            if (current.getOutgoing() != null) allNeighbors.addAll(current.getOutgoing());
            if (current.getIncoming() != null) allNeighbors.addAll(current.getIncoming());

            for (MakaleDugumu neighbor : allNeighbors) {

                if (validNodes.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList(); // Yol yok
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