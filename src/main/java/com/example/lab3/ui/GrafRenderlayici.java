package com.example.lab3.ui;

import com.example.lab3.models.MakaleDugumu;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GrafRenderlayici {

    private static final double NODE_RADIUS = 25.0;
    private static final double ARROW_SIZE = 6.0; // Ok ucunun boyutu


    public static void drawInteractiveGraph(Pane pane, Map<MakaleDugumu, Point2D> nodePositions, Consumer<MakaleDugumu> onNodeClick) {
        pane.getChildren().clear();

        if (nodePositions == null || nodePositions.isEmpty()) {
            return;
        }

        Group group = new Group();


        for (Map.Entry<MakaleDugumu, Point2D> entry : nodePositions.entrySet()) {
            MakaleDugumu source = entry.getKey();
            Point2D p1 = entry.getValue();

            for (MakaleDugumu target : source.getOutgoing()) {
                if (nodePositions.containsKey(target)) {
                    Point2D p2 = nodePositions.get(target);

                    drawArrow(group, p1, p2, Color.GRAY, 1.0);
                }
            }
        }

        for (Map.Entry<MakaleDugumu, Point2D> entry : nodePositions.entrySet()) {
            MakaleDugumu node = entry.getKey();
            Point2D pos = entry.getValue();
            drawNode(group, pos.getX(), pos.getY(), node, Color.CORNFLOWERBLUE, onNodeClick);
        }

        pane.getChildren().add(group);
    }


    public static void drawInteractiveGraphWithHighlight(Pane pane, Map<MakaleDugumu, Point2D> nodePositions,
                                                         List<MakaleDugumu> highlightList,
                                                         Consumer<MakaleDugumu> onNodeClick) {
        pane.getChildren().clear();
        if (nodePositions == null || nodePositions.isEmpty()) return;

        Group group = new Group();


        for (Map.Entry<MakaleDugumu, Point2D> entry : nodePositions.entrySet()) {
            MakaleDugumu source = entry.getKey();
            Point2D p1 = entry.getValue();

            for (MakaleDugumu target : source.getOutgoing()) {
                if (nodePositions.containsKey(target)) {
                    Point2D p2 = nodePositions.get(target);

                    boolean isHigh = highlightList.contains(source) && highlightList.contains(target);
                    if (isHigh) {
                        // Vurgulu: Kırmızı ve Kalın Ok
                        drawArrow(group, p1, p2, Color.RED, 3.0);
                    } else {
                        // Normal: Gri ve İnce Ok
                        drawArrow(group, p1, p2, Color.LIGHTGRAY, 1.0);
                    }
                }
            }
        }

        for (Map.Entry<MakaleDugumu, Point2D> entry : nodePositions.entrySet()) {
            MakaleDugumu node = entry.getKey();
            Point2D pos = entry.getValue();
            Color color = highlightList.contains(node) ? Color.SALMON : Color.LIGHTGRAY;
            drawNode(group, pos.getX(), pos.getY(), node, color, onNodeClick);
        }
        pane.getChildren().add(group);
    }

    public static void drawPathGraph(Pane pane, List<MakaleDugumu> pathNodes) {
        pane.getChildren().clear();
        if (pathNodes == null || pathNodes.isEmpty()) return;

        double width = pane.getWidth() > 0 ? pane.getWidth() : 800;
        double height = pane.getHeight() > 0 ? pane.getHeight() : 600;
        Group group = new Group();

        int n = pathNodes.size();
        double stepX = (width - 100) / (double) Math.max(1, n - 1);
        double startX = 50;
        double centerY = height / 2.0;

        Point2D[] positions = new Point2D[n];
        for (int i = 0; i < n; i++) {
            positions[i] = new Point2D(startX + i * stepX, centerY);
        }

        for (int i = 0; i < n - 1; i++) {
            drawArrow(group, positions[i], positions[i+1], Color.ORANGE, 3.0);
        }

        for (int i = 0; i < n; i++) {
            Color c = (i == 0) ? Color.LIGHTGREEN : (i == n - 1 ? Color.SALMON : Color.ORANGE);
            drawNode(group, positions[i].getX(), positions[i].getY(), pathNodes.get(i), c, null);
        }
        pane.getChildren().add(group);
    }

    public static void drawGreenLineChain(Pane pane, List<MakaleDugumu> sortedNodes, Consumer<MakaleDugumu> onNodeClick) {
        pane.getChildren().clear();
        if (sortedNodes == null || sortedNodes.isEmpty()) return;

        double width = pane.getWidth() > 0 ? pane.getWidth() : 800;
        Group group = new Group();

        double startX = 80, startY = 80, gapX = 120, gapY = 150;
        int cols = (int) ((width - 100) / gapX);
        if (cols < 2) cols = 2;

        Point2D[] positions = new Point2D[sortedNodes.size()];
        for (int i = 0; i < sortedNodes.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            if (row % 2 == 1) col = (cols - 1) - col;

            double x = startX + col * gapX;
            double y = startY + row * gapY;
            positions[i] = new Point2D(x, y);
        }

        for (int i = 0; i < sortedNodes.size() - 1; i++) {
            Point2D p1 = positions[i];
            Point2D p2 = positions[i+1];

            Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            line.setStroke(Color.GREEN);
            line.setStrokeWidth(2.5);
            line.getStrokeDashArray().addAll(10d, 5d);
            group.getChildren().add(line);
        }

        for (int i = 0; i < sortedNodes.size(); i++) {
            drawNode(group, positions[i].getX(), positions[i].getY(), sortedNodes.get(i), Color.WHITE, onNodeClick);
            Text t = new Text(positions[i].getX() - 5, positions[i].getY() + NODE_RADIUS + 15, "#" + (i+1));
            t.setFont(Font.font("Arial", 10));
            group.getChildren().add(t);
        }

        pane.getChildren().add(group);
        int totalRows = (int) Math.ceil((double) sortedNodes.size() / cols);
        pane.setPrefHeight(Math.max(600, startY + totalRows * gapY + 100));
    }


    private static void drawArrow(Group group, Point2D p1, Point2D p2, Color color, double width) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double angle = Math.atan2(dy, dx);

        double targetX = p2.getX() - NODE_RADIUS * Math.cos(angle);
        double targetY = p2.getY() - NODE_RADIUS * Math.sin(angle);

        double startX = p1.getX() + NODE_RADIUS * Math.cos(angle);
        double startY = p1.getY() + NODE_RADIUS * Math.sin(angle);

        Line line = new Line(startX, startY, targetX, targetY);
        line.setStroke(color);
        line.setStrokeWidth(width);

        double arrowLen = 10.0 + width;
        double arrowWing = Math.PI / 6.0;

        double x1 = targetX - arrowLen * Math.cos(angle - arrowWing);
        double y1 = targetY - arrowLen * Math.sin(angle - arrowWing);
        double x2 = targetX - arrowLen * Math.cos(angle + arrowWing);
        double y2 = targetY - arrowLen * Math.sin(angle + arrowWing);

        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
                targetX, targetY,
                x1, y1,
                x2, y2
        );
        arrowHead.setFill(color);
        arrowHead.setStroke(color);
        arrowHead.setStrokeWidth(1.0);

        group.getChildren().addAll(line, arrowHead);
    }


    private static void drawNode(Group group, double x, double y, MakaleDugumu node, Color fill, Consumer<MakaleDugumu> onClick) {
        String citationCount = String.valueOf(node.getInDegree());
        String rawId = node.getArticle().getId();
        String displayId = rawId.contains("/") ? rawId.substring(rawId.lastIndexOf("/") + 1) : rawId;
        if (displayId.length() > 8) displayId = displayId.substring(0, 8) + "..";

        StringBuilder sb = new StringBuilder();
        List<String> authors = node.getArticle().getAuthors();
        if (authors != null) {
            for (String author : authors) {
                if (author != null && !author.isBlank()) {
                    sb.append(author.trim().substring(0, 1).toUpperCase());
                }
            }
        }
        String authorInitials = sb.length() > 0 ? sb.toString() : "-";
        if (authorInitials.length() > 5) authorInitials = authorInitials.substring(0, 5) + "..";

        String fullAuthors = (authors != null && !authors.isEmpty()) ? String.join(", ", authors) : "Bilinmiyor";

        StackPane stack = new StackPane();
        stack.setLayoutX(x - NODE_RADIUS);
        stack.setLayoutY(y - NODE_RADIUS);

        Circle circle = new Circle(NODE_RADIUS);
        circle.setFill(fill);
        circle.setStroke(Color.DARKSLATEGRAY);
        circle.setStrokeWidth(1.5);

        stack.setOnMouseEntered(e -> circle.setStroke(Color.RED));
        stack.setOnMouseExited(e -> circle.setStroke(Color.DARKSLATEGRAY));

        if (onClick != null) {
            stack.setOnMouseClicked(e -> {
                onClick.accept(node);
                e.consume();
            });
        }

        VBox textBox = new VBox(0);
        textBox.setAlignment(Pos.CENTER);

        Text txtCount = new Text(citationCount);
        txtCount.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Text txtId = new Text(displayId);
        txtId.setFont(Font.font("Arial", 9));

        Text txtAuth = new Text(authorInitials);
        txtAuth.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        textBox.getChildren().addAll(txtCount, txtId, txtAuth);
        stack.getChildren().addAll(circle, textBox);

        Tooltip tooltip = new Tooltip(
                "Başlık: " + node.getArticle().getTitle() + "\n" +
                        "Yazarlar: " + fullAuthors + "\n" +
                        "ID: " + rawId + "\n" +
                        "Yıl: " + node.getArticle().getYear() + "\n" +
                        "Atıf (In): " + node.getInDegree() + "\n" +
                        "Ref (Out): " + node.getOutDegree()
        );
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(stack, tooltip);

        group.getChildren().add(stack);
    }
}