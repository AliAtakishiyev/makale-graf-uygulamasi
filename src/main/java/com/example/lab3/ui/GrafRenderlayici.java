package com.example.lab3.ui;

import com.example.lab3.models.MakaleModeli;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GrafRenderlayici {

    private static final double NODE_RADIUS = 35;

    private GrafRenderlayici() {
    }

    public static void drawInteractiveGraph(Pane pane,
                                            Map<MakaleDugumu, Point2D> positions,
                                            Consumer<MakaleDugumu> onNodeClick) {
        pane.getChildren().clear();

        Group group = new Group();

        // 1. Kenarları Çiz
        for (MakaleDugumu source : positions.keySet()) {
            Point2D p1 = positions.get(source);

            // Giden bağlantıları kontrol et
            for (MakaleDugumu target : source.getOutgoing()) {
                if (positions.containsKey(target)) {
                    Point2D p2 = positions.get(target);
                    drawLine(group, p1.getX(), p1.getY(), p2.getX(), p2.getY());
                }
            }
        }

        // 2. Düğümleri Çiz (Renk: Açık Mavi/Gökyüzü)
        for (Map.Entry<MakaleDugumu, Point2D> entry : positions.entrySet()) {
            MakaleDugumu node = entry.getKey();
            Point2D pos = entry.getValue();

            // Merkez düğüm veya diğerleri için renk ayrımı yapılabilir ama şimdilik standart:
            drawNode(group, pos.getX(), pos.getY(), node, Color.LIGHTSKYBLUE, onNodeClick);
        }

        pane.getChildren().add(group);
    }

    public static void drawKCoreGraph(Pane pane, List<MakaleDugumu> kCoreNodes, String targetId) {
        pane.getChildren().clear();

        if (kCoreNodes.isEmpty()) {
            Text text = new Text(20, 30, "Bu k değeri için düğüm bulunamadı.");
            pane.getChildren().add(text);
            return;
        }

        double width = pane.getWidth() > 0 ? pane.getWidth() : 800;
        double height = pane.getHeight() > 0 ? pane.getHeight() : 600;
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        Group group = new Group();

        int n = kCoreNodes.size();
        double layoutRadius = Math.min(width, height) / 2.5;

        double[][] positions = new double[n][2];
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            positions[i][0] = centerX + layoutRadius * Math.cos(angle);
            positions[i][1] = centerY + layoutRadius * Math.sin(angle);
        }

        for (int i = 0; i < n; i++) {
            MakaleDugumu source = kCoreNodes.get(i);
            double x1 = positions[i][0];
            double y1 = positions[i][1];

            for (MakaleDugumu target : source.getOutgoing()) {
                if (kCoreNodes.contains(target)) {
                    int targetIndex = kCoreNodes.indexOf(target);
                    if (targetIndex != -1) {
                        double x2 = positions[targetIndex][0];
                        double y2 = positions[targetIndex][1];
                        drawLine(group, x1, y1, x2, y2);
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            drawNode(group, positions[i][0], positions[i][1], kCoreNodes.get(i), Color.CORNFLOWERBLUE, null);
        }

        pane.getChildren().add(group);
    }

    public static void drawHCoreGraph(Pane pane, MakaleDugumu center, List<MakaleDugumu> hCoreNodes) {
    }

    private static void drawNode(Group group, double x, double y, MakaleDugumu node, Color fill, Consumer<MakaleDugumu> onClick) {
        MakaleModeli article = node.getArticle();

        String citationCount = String.valueOf(node.getInDegree());

        String rawId = article.getId();
        String displayId = formatId(rawId);

        StringBuilder sb = new StringBuilder();
        if (article.getAuthors() != null) {
            for (String author : article.getAuthors()) {
                if (author != null && !author.isBlank()) {
                    // İsmin ilk harfini al ve büyük harf yap
                    sb.append(author.trim().substring(0, 1).toUpperCase());
                }
            }
        }
        String authorInitials = sb.length() > 0 ? sb.toString() : "-";

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

        VBox textBox = new VBox(1);
        textBox.setAlignment(Pos.CENTER);

        Text txtCount = new Text(citationCount);
        txtCount.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text txtId = new Text(displayId);
        txtId.setFont(Font.font("Arial", 9));

        Text txtAuth = new Text(authorInitials);

        if (authorInitials.length() > 4) {
            txtAuth.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        } else if (authorInitials.length() > 2) {
            txtAuth.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        } else {
            txtAuth.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        }

        textBox.getChildren().addAll(txtCount, txtId, txtAuth);
        stack.getChildren().addAll(circle, textBox);

        Tooltip tooltip = new Tooltip();
        tooltip.setText(
                "Başlık: " + article.getTitle() + "\n" +
                        "ID: " + rawId + "\n" +
                        "Yıl: " + article.getYear() + "\n" +
                        "Yazarlar: " + String.join(", ", article.getAuthors()) + "\n" +
                        "Gelen Atıf: " + node.getInDegree() + "\n" +
                        "Giden Referans: " + node.getOutDegree()
        );
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(stack, tooltip);

        group.getChildren().add(stack);
    }

    private static void drawLine(Group group, double x1, double y1, double x2, double y2) {
        Line edge = new Line(x1, y1, x2, y2);
        edge.setStroke(Color.GRAY);
        edge.setStrokeWidth(1.0);
        group.getChildren().add(edge);
    }

    private static String formatId(String id) {
        if (id == null) return "?";
        if (id.contains("/")) {
            id = id.substring(id.lastIndexOf('/') + 1);
        }
        if (id.length() > 7) {
            return id.substring(0, 7) + "..";
        }
        return id;
    }

    public static void drawPathGraph(Pane pane, List<MakaleDugumu> pathNodes) {
        pane.getChildren().clear();

        if (pathNodes == null || pathNodes.isEmpty()) {
            Text text = new Text(20, 30, "İki makale arasında bağlantı (yol) bulunamadı.");
            pane.getChildren().add(text);
            return;
        }

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
            Point2D p1 = positions[i];
            Point2D p2 = positions[i+1];

            Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            line.setStroke(Color.ORANGE);
            line.setStrokeWidth(3.0);
            group.getChildren().add(line);

        }

        for (int i = 0; i < n; i++) {
            MakaleDugumu node = pathNodes.get(i);
            Color color = Color.LIGHTBLUE;

            if (i == 0) color = Color.LIGHTGREEN;
            else if (i == n - 1) color = Color.SALMON;
            else color = Color.ORANGE;

            drawNode(group, positions[i].getX(), positions[i].getY(), node, color, null);
        }

        pane.getChildren().add(group);
    }


    public static void drawGreenLineChain(Pane pane, List<MakaleDugumu> sortedNodes, Consumer<MakaleDugumu> onNodeClick) {
        pane.getChildren().clear();

        if (sortedNodes == null || sortedNodes.isEmpty()) {
            return;
        }

        double width = pane.getWidth() > 0 ? pane.getWidth() : 800;

        Group group = new Group();

        double startX = 80;
        double startY = 80;
        double gapX = 120;
        double gapY = 150;

        int cols = (int) ((width - 100) / gapX);
        if (cols < 2) cols = 2;

        Point2D[] positions = new Point2D[sortedNodes.size()];

        for (int i = 0; i < sortedNodes.size(); i++) {
            int row = i / cols;
            int col = i % cols;

            if (row % 2 == 1) {
                col = (cols - 1) - col;
            }

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

            Text orderText = new Text(positions[i].getX() - 5, positions[i].getY() + NODE_RADIUS + 15, "#" + (i+1));
            orderText.setFont(Font.font("Arial", 10));
            group.getChildren().add(orderText);
        }

        pane.getChildren().add(group);

        int totalRows = (int) Math.ceil((double) sortedNodes.size() / cols);
        double requiredHeight = startY + totalRows * gapY + 100;
        pane.setPrefHeight(Math.max(600, requiredHeight));
    }


    public static void drawInteractiveGraphWithHighlight(Pane pane, Map<MakaleDugumu, Point2D> nodePositions,
                                                         List<MakaleDugumu> kCoreNodes,
                                                         Consumer<MakaleDugumu> onNodeClick) {
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

                    Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());

                    // --- RENK KARARI ---
                    boolean isCoreEdge = kCoreNodes.contains(source) && kCoreNodes.contains(target);

                    if (isCoreEdge) {
                        line.setStroke(Color.RED);        // Çekirdek bağlantısı
                        line.setStrokeWidth(3.0);         // Kalın
                    } else {
                        line.setStroke(Color.LIGHTGRAY);  // Zayıf bağlantı
                        line.setStrokeWidth(1.0);         // İnce
                    }

                    group.getChildren().add(line);
                }
            }
        }

        for (Map.Entry<MakaleDugumu, Point2D> entry : nodePositions.entrySet()) {
            MakaleDugumu node = entry.getKey();
            Point2D pos = entry.getValue();


            Color nodeColor;
            if (kCoreNodes.contains(node)) {
                nodeColor = Color.SALMON; // K-Core Üyesi
            } else {
                nodeColor = Color.LIGHTGRAY; // Dışlananlar
            }

            drawNode(group, pos.getX(), pos.getY(), node, nodeColor, onNodeClick);
        }

        pane.getChildren().add(group);
    }
}