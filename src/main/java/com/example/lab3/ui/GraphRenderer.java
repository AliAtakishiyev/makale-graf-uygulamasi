package com.example.lab3.ui;

import com.example.lab3.models.ArticleNode;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Basit bir graf çizici.
 *
 * Şimdilik sadece:
 * - Merkez makaleyi ortada daire olarak çizer.
 * - h-core düğümlerini etrafına dağıtır.
 * - h-core düğümlerinden merkeze yönlü kenarları (ok ucu olmadan çizgi) çizer.
 */
public class GraphRenderer {

    private static final double NODE_RADIUS = 25;

    private GraphRenderer() {
    }

    public static void drawHCoreGraph(Pane pane, ArticleNode center, List<ArticleNode> hCoreNodes) {
        pane.getChildren().clear();

        double width = pane.getWidth() > 0 ? pane.getWidth() : pane.getPrefWidth();
        double height = pane.getHeight() > 0 ? pane.getHeight() : pane.getPrefHeight();

        if (width <= 0) {
            width = 500;
        }
        if (height <= 0) {
            height = 400;
        }

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        Group group = new Group();

        // Merkez düğüm
        drawNode(group, centerX, centerY, center, Color.SALMON);

        // h-core düğümlerini dairesel olarak yerleştir
        int n = hCoreNodes.size();
        double radius = Math.min(width, height) / 2.5;

        for (int i = 0; i < n; i++) {
            ArticleNode node = hCoreNodes.get(i);
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            // Kenar: h-core düğümünden merkeze
            Line edge = new Line(x, y, centerX, centerY);
            edge.setStroke(Color.BLACK);
            edge.setStrokeWidth(1.5);
            group.getChildren().add(edge);

            drawNode(group, x, y, node, Color.BEIGE);
        }

        pane.getChildren().add(group);
    }

    private static void drawNode(Group group, double x, double y, ArticleNode node, Color fill) {
        Circle circle = new Circle(x, y, NODE_RADIUS);
        circle.setFill(fill);
        circle.setStroke(Color.DARKGRAY);

        // ID yerine daha kısa gösterim için son birkaç karakteri al
        String id = node.getArticle().getId();
        String shortId = id.length() > 8 ? id.substring(id.length() - 8) : id;

        Text text = new Text(shortId);
        text.setFill(Color.BLACK);
        text.setStyle("-fx-font-size: 11;");
        // Text'i merkeze yakın konumlandır
        text.setX(x - text.getLayoutBounds().getWidth() / 2);
        text.setY(y + 4);

        group.getChildren().addAll(circle, text);
    }
}


