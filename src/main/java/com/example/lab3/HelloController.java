package com.example.lab3;

import com.example.lab3.analysis.HIndexHesaplama;
import com.example.lab3.analysis.HIndexSonuc;
import com.example.lab3.analysis.KCoreHesaplama;
import com.example.lab3.analysis.KisaYolBulma;
import com.example.lab3.jsonreader.JsonOkuyucu;
import com.example.lab3.models.MakaleModeli;
import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;
import com.example.lab3.ui.GrafRenderlayici;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HelloController {

    private static final Path ARTICLES_JSON_PATH = Path.of(
            "C:/Users/dgknb/OneDrive/Desktop/Prolab3/makale-graf-uygulamasi/src/main/java/com/example/lab3/utlis/makale.json"
    );

    @FXML
    private TextArea outputArea;

    @FXML private TextField pathStartField;
    @FXML private TextField pathEndField;

    @FXML
    private TextField articleIdField;

    @FXML
    private TextField kInput;

    @FXML
    private Label errorLabel;

    @FXML
    private Pane graphPane;

    private MakaleGrafı graph;

    private final Map<MakaleDugumu, Point2D> nodePositions = new HashMap<>();
    private final Random random = new Random();


    @FXML
    protected void onLoadJsonClick() {
        clearError();
        try {
            // 1) JSON'dan makaleleri oku
            List<MakaleModeli> articles = JsonOkuyucu.readArticles(ARTICLES_JSON_PATH);

            // 2) Bellekte tam grafı kur
            this.graph = MakaleGrafı.buildFromArticles(articles);

            // 3) İstatistikleri hesapla
            int nodeCount = graph.getNodeCount();
            int edgeCount = graph.getEdgeCount();
            MakaleGrafı.ArticleIdAndCount mostCited = graph.getMostCitedArticle();
            MakaleGrafı.ArticleIdAndCount mostCiting = graph.getMostCitingArticle();

            // 4) Sonuçları göster
            StringBuilder sb = new StringBuilder();
            sb.append("=== Genel Graf İstatistikleri ===\n");
            sb.append("Toplam Makale: ").append(nodeCount).append("\n");
            sb.append("Toplam Referans: ").append(edgeCount).append("\n");
            sb.append("En Çok Atıf Alan: ").append(mostCited.articleId())
                    .append(" (").append(mostCited.count()).append(")\n");
            sb.append("En Çok Atıf Veren: ").append(mostCiting.articleId())
                    .append(" (").append(mostCiting.count()).append(")\n");
            sb.append("\nİşlem yapmak için ID veya K değeri giriniz.\n");

            outputArea.setText(sb.toString());
            graphPane.getChildren().clear();
            nodePositions.clear(); // Eski pozisyonları temizle
            System.out.println(sb);

        } catch (IOException e) {
            showError("JSON okuma hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Graf oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    protected void onKCoreButtonClick() {
        clearError();
        if (graph == null) {
            showError("Lütfen önce JSON dosyasını yükleyin.");
            return;
        }

        try {
            if (kInput == null) {
                showError("Arayüz hatası: kInput alanı bulunamadı.");
                return;
            }

            String kText = kInput.getText();
            if (kText == null || kText.isBlank()) {
                showError("Lütfen bir k değeri girin.");
                return;
            }

            int k = Integer.parseInt(kText.trim());

            // Hesapla
            List<MakaleDugumu> kCoreNodes = KCoreHesaplama.computeKCore(graph, k);

            outputArea.setText("=== K-Core Sonuçları ===\n");
            outputArea.appendText("k = " + k + " için bulunan düğüm sayısı: " + kCoreNodes.size() + "\n");
            outputArea.appendText("(K-Core görünümü şu an statiktir, tıklama özelliği H-Index modunda aktiftir.)\n");

            // Çizdir (K-Core için özel renkli çizim)
            GrafRenderlayici.drawKCoreGraph(graphPane, kCoreNodes);

        } catch (NumberFormatException e) {
            showError("Lütfen k değeri için geçerli bir tamsayı girin.");
        } catch (Exception e) {
            showError("K-Core hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    protected void onComputeHIndexClick() {
        clearError();
        if (graph == null) {
            showError("Lütfen önce JSON'u yükleyin.");
            return;
        }

        String id = articleIdField.getText();
        if (id == null || id.isBlank()) {
            showError("Lütfen geçerli bir makale ID girin.");
            return;
        }

        id = id.trim();
        MakaleDugumu centerNode = graph.getNode(id);

        if (centerNode == null) {
            showError("'" + id + "' ID'li makale bulunamadı.");
            return;
        }

        try {
            HIndexSonuc result = HIndexHesaplama.computeForArticle(graph, id);

            StringBuilder sb = new StringBuilder();
            sb.append("=== H-index Sonuçları ===\n");
            sb.append("Makale: ").append(centerNode.getArticle().getTitle()).append("\n");
            sb.append("h-index: ").append(result.getHIndex()).append("\n");
            sb.append("h-median: ").append(result.getHMedian()).append("\n");
            sb.append("h-core sayısı: ").append(result.getHCoreNodes().size()).append("\n");
            sb.append("\n[BİLGİ] Düğümlere tıklayarak referanslarını (mavi oklar) görebilirsiniz.");

            outputArea.setText(sb.toString());

            nodePositions.clear();

            double paneW = graphPane.getWidth() > 0 ? graphPane.getWidth() : 800;
            double paneH = graphPane.getHeight() > 0 ? graphPane.getHeight() : 600;

            nodePositions.put(centerNode, new Point2D(paneW / 2, paneH / 2));

            List<MakaleDugumu> hCore = result.getHCoreNodes();
            double radius = 150;
            for (int i = 0; i < hCore.size(); i++) {
                double angle = 2 * Math.PI * i / hCore.size();
                double x = (paneW / 2) + radius * Math.cos(angle);
                double y = (paneH / 2) + radius * Math.sin(angle);
                nodePositions.put(hCore.get(i), new Point2D(x, y));
            }

            redrawInteractive();

        } catch (Exception ex) {
            showError("Hata: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void handleNodeClick(MakaleDugumu clickedNode) {
        // Bu makalenin referans verdiği makaleler (Outgoing)
        List<MakaleDugumu> references = clickedNode.getOutgoing();

        if (references.isEmpty()) {
            outputArea.appendText("\n-> Bilgi: Bu makale veri setindeki başka bir makaleye atıf vermiyor.");
            return;
        }

        boolean anyNewAdded = false;
        Point2D parentPos = nodePositions.get(clickedNode);


        for (MakaleDugumu ref : references) {
            if (!nodePositions.containsKey(ref)) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double dist = 100 + random.nextDouble() * 50;

                double newX = parentPos.getX() + dist * Math.cos(angle);
                double newY = parentPos.getY() + dist * Math.sin(angle);

                double paneW = graphPane.getWidth();
                double paneH = graphPane.getHeight();
                newX = Math.max(40, Math.min(newX, paneW - 40));
                newY = Math.max(40, Math.min(newY, paneH - 40));

                nodePositions.put(ref, new Point2D(newX, newY));
                anyNewAdded = true;
            }
        }

        if (anyNewAdded) {
            applySimpleForceLayout();
            redrawInteractive();
            outputArea.appendText("\n-> " + clickedNode.getArticle().getId() + " genişletildi (" + references.size() + " yeni bağlantı).");
        } else {
            outputArea.appendText("\n-> " + clickedNode.getArticle().getId() + ": Tüm referansları zaten ekranda.");
        }
    }


    private void redrawInteractive() {
        // GraphRenderer.drawInteractiveGraph metodunu kullanıyoruz
        GrafRenderlayici.drawInteractiveGraph(graphPane, nodePositions, this::handleNodeClick);
    }


    private void applySimpleForceLayout() {
        int iterations = 10; // Yerleşim döngüsü sayısı
        double minDistance = 80.0; // İdeal minimum mesafe (Düğüm çapı + boşluk)

        for (int i = 0; i < iterations; i++) {
            for (Map.Entry<MakaleDugumu, Point2D> e1 : nodePositions.entrySet()) {
                for (Map.Entry<MakaleDugumu, Point2D> e2 : nodePositions.entrySet()) {
                    if (e1.getKey() == e2.getKey()) continue;

                    Point2D p1 = e1.getValue();
                    Point2D p2 = e2.getValue();

                    double dist = p1.distance(p2);

                    // Çok yakınlarsa it
                    if (dist < minDistance && dist > 1.0) {
                        double push = (minDistance - dist) / 2.0; // Ne kadar itilecek
                        double dx = p1.getX() - p2.getX();
                        double dy = p1.getY() - p2.getY();

                        // Vektörü normalize et
                        double len = Math.sqrt(dx * dx + dy * dy);
                        dx /= len;
                        dy /= len;

                        // p1'i it
                        e1.setValue(new Point2D(p1.getX() + dx * push, p1.getY() + dy * push));
                        // p2'yi aksi yöne it
                        e2.setValue(new Point2D(p2.getX() - dx * push, p2.getY() - dy * push));
                    }
                }
            }
        }
    }

    @FXML
    protected void onFindShortestPathClick() {
        clearError();
        if (graph == null) {
            showError("Lütfen önce JSON dosyasını yükleyin.");
            return;
        }

        String startId = pathStartField.getText();
        String endId = pathEndField.getText();

        if (startId == null || startId.isBlank() || endId == null || endId.isBlank()) {
            showError("Lütfen her iki ID alanını da doldurun.");
            return;
        }

        startId = startId.trim();
        endId = endId.trim();

        try {
            List<MakaleDugumu> path = KisaYolBulma.findShortestPath(graph, startId, endId);

            if (path.isEmpty()) {
                outputArea.setText("Sonuç: Bu iki makale arasında bir atıf zinciri (yol) BULUNAMADI.");
                graphPane.getChildren().clear();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("=== En Kısa Yol Sonuçları ===\n");
                sb.append("Adım Sayısı (Hop Count): ").append(path.size() - 1).append("\n");
                sb.append("Yol:\n");
                for (int i = 0; i < path.size(); i++) {
                    sb.append(i + 1).append(". ").append(path.get(i).getArticle().getId()).append("\n");
                }
                outputArea.setText(sb.toString());

                GrafRenderlayici.drawPathGraph(graphPane, path);
            }

        } catch (Exception e) {
            showError("Yol bulma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onShowGreenChainClick() {
        clearError();
        if (graph == null) {
            showError("Lütfen önce JSON dosyasını yükleyin.");
            return;
        }

        try {
            List<MakaleDugumu> allNodes = new java.util.ArrayList<>(graph.getAllNodes());

            allNodes.sort((n1, n2) -> n1.getArticle().getId().compareTo(n2.getArticle().getId()));

            int limit = Math.min(allNodes.size(), 100);
            List<MakaleDugumu> sortedList = allNodes.subList(0, limit);

            outputArea.setText("=== ID Sıralı Zincir (Yeşil Hat) ===\n");
            outputArea.appendText("Görüntülenen: " + sortedList.size() + " makale.\n");
            outputArea.appendText("İPUCU: Zincirdeki bir makaleye tıklayarak H-Index analizine gidebilirsiniz.");

            GrafRenderlayici.drawGreenLineChain(graphPane, sortedList, this::handleChainNodeClick);

        } catch (Exception e) {
            showError("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleChainNodeClick(MakaleDugumu node) {
        articleIdField.setText(node.getArticle().getId());

        onComputeHIndexClick();
    }


    @FXML
    protected void onZoomInClick() {
        double currentScale = graphPane.getScaleX();

        if (currentScale < 5.0) {
            double newScale = currentScale * 1.1;
            graphPane.setScaleX(newScale);
            graphPane.setScaleY(newScale);
        }
    }

    @FXML
    protected void onZoomOutClick() {
        double currentScale = graphPane.getScaleX();

        if (currentScale > 0.1) {
            double newScale = currentScale / 1.1;
            graphPane.setScaleX(newScale);
            graphPane.setScaleY(newScale);
        }
    }

    @FXML
    protected void onResetZoomClick() {
        graphPane.setScaleX(1.0);
        graphPane.setScaleY(1.0);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setText("");
    }
}