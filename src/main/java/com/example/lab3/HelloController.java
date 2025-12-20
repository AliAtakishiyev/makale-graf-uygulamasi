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
import java.util.*;

public class HelloController {

    // JSON DOSYA YOLUNU KENDİ BİLGİSAYARINA GÖRE GÜNCELLEMEYİ UNUTMA
    private static final Path ARTICLES_JSON_PATH = Path.of(
            "C:/Users/dgknb/OneDrive/Desktop/Prolab3/makale-graf-uygulamasi/src/main/java/com/example/lab3/utlis/makale.json"
    );

    @FXML private TextArea outputArea;
    @FXML private TextField pathStartField;
    @FXML private TextField pathEndField;
    @FXML private TextField articleIdField;
    @FXML private TextField kInput;
    @FXML private Label errorLabel;
    @FXML private Pane graphPane;

    private MakaleGrafı graph;
    private final Map<MakaleDugumu, Point2D> nodePositions = new HashMap<>();

    // Rastgelelik için (Genişletme sırasında kullanılır)
    private final Random random = new Random();

    @FXML
    protected void onLoadJsonClick() {
        clearError();
        try {
            List<MakaleModeli> articles = JsonOkuyucu.readArticles(ARTICLES_JSON_PATH);
            this.graph = MakaleGrafı.buildFromArticles(articles);

            int nodeCount = graph.getNodeCount();
            int edgeCount = graph.getEdgeCount();
            MakaleGrafı.ArticleIdAndCount mostCited = graph.getMostCitedArticle();
            MakaleGrafı.ArticleIdAndCount mostCiting = graph.getMostCitingArticle();

            StringBuilder sb = new StringBuilder();
            sb.append("=== Genel Graf İstatistikleri ===\n");
            sb.append("Toplam Makale: ").append(nodeCount).append("\n");
            sb.append("Toplam Referans: ").append(edgeCount).append("\n");

            if (mostCited != null) {
                sb.append("En Çok Atıf Alan: ").append(mostCited.articleId())
                        .append(" (").append(mostCited.count()).append(")\n");
            }
            if (mostCiting != null) {
                sb.append("En Çok Atıf Veren: ").append(mostCiting.articleId())
                        .append(" (").append(mostCiting.count()).append(")\n");
            }
            sb.append("\nİşlem yapmak için ID veya K değeri giriniz.\n");

            outputArea.setText(sb.toString());
            graphPane.getChildren().clear();
            nodePositions.clear();

        } catch (IOException e) {
            showError("JSON okuma hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Graf oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onLocalKCoreClick() {
        outputArea.setText("");

        if (nodePositions.isEmpty()) {
            outputArea.setText("Hata: Ekranda bir graf yok.");
            return;
        }

        try {
            if (kInput == null || kInput.getText().isBlank()) {
                outputArea.setText("Hata: Lütfen bir k değeri girin.");
                return;
            }
            int k = Integer.parseInt(kInput.getText().trim());

            List<MakaleDugumu> visibleNodes = new ArrayList<>(nodePositions.keySet());
            List<MakaleDugumu> survivors = KCoreHesaplama.computeLocalKCore(visibleNodes, k);

            outputArea.setText("=== Yerel K-Core Analizi ===\n");
            outputArea.appendText("Analiz edilen düğüm: " + visibleNodes.size() + "\n");
            outputArea.appendText("K=" + k + " çekirdeğindeki düğüm: " + survivors.size() + "\n");

            if (survivors.isEmpty()) {
                outputArea.appendText("Sonuç: Bu kriteri sağlayan hiçbir düğüm yok.\n");
            } else {
                outputArea.appendText("Sonuç: K-Core üyeleri KIRMIZI, elenenler GRİ renkle gösterildi.\n");
            }

            GrafRenderlayici.drawInteractiveGraphWithHighlight(
                    graphPane,
                    nodePositions,
                    survivors,
                    this::handleNodeClick
            );

        } catch (NumberFormatException e) {
            outputArea.setText("Hata: Geçersiz sayı.");
        } catch (Exception e) {
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
            // 1. Hesapla
            HIndexSonuc result = HIndexHesaplama.computeForArticle(graph, id);

            // 2. Bilgi Ver
            StringBuilder sb = new StringBuilder();
            sb.append("=== H-index Sonuçları ===\n");
            sb.append("Makale: ").append(centerNode.getArticle().getTitle()).append("\n");
            sb.append("h-index: ").append(result.getHIndex()).append("\n");
            sb.append("h-median: ").append(result.getHMedian()).append("\n");
            sb.append("h-core sayısı: ").append(result.getHCoreNodes().size()).append("\n");
            sb.append("\n[BİLGİ] Düğümlere tıklayarak grafiği genişletebilirsiniz (H-Core eklenir).");

            outputArea.setText(sb.toString());

            // 3. Pozisyonları Sıfırla ve Çizim
            nodePositions.clear();

            double paneW = graphPane.getWidth() > 0 ? graphPane.getWidth() : 800;
            double paneH = graphPane.getHeight() > 0 ? graphPane.getHeight() : 600;

            // Merkezi yerleştir
            nodePositions.put(centerNode, new Point2D(paneW / 2, paneH / 2));

            // H-Core elemanlarını etrafa diz
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

    // --- GÜNCELLENEN KISIM BURASI ---
    // Tıklanan düğümün H-Index'ini hesaplar ve grafiği H-Core ile genişletir.
    private void handleNodeClick(MakaleDugumu clickedNode) {
        String newId = clickedNode.getArticle().getId();

        // 1. ID Kutusunu güncelle
        articleIdField.setText(newId);

        // 2. Tıklanan düğüm için H-Index Hesapla
        HIndexSonuc result = HIndexHesaplama.computeForArticle(graph, newId);

        // 3. Sonuçları Ekrana Bas (Eskileri silmeden altına ekle)
        outputArea.appendText("\n\n----------------------------------\n");
        outputArea.appendText("GENİŞLETİLEN MAKALE: " + clickedNode.getArticle().getTitle() + "\n");
        outputArea.appendText("ID: " + newId + "\n");
        outputArea.appendText("h-index: " + result.getHIndex() + "\n");
        outputArea.appendText("h-median: " + result.getHMedian() + "\n");
        outputArea.appendText("Yeni h-core bağlantıları eklendi (" + result.getHCoreNodes().size() + " adet).");

        // 4. Yeni Düğümleri Haritaya Ekle
        // Tıklanan düğümün etrafına, mevcut pozisyonu merkez alarak ekliyoruz.
        Point2D centerPos = nodePositions.get(clickedNode);

        // Eğer düğüm bir şekilde haritada yoksa (hata durumu), ortaya al.
        if (centerPos == null) {
            centerPos = new Point2D(graphPane.getWidth() / 2, graphPane.getHeight() / 2);
        }

        double dist = 120.0; // Merkezden uzaklık
        boolean anyNewAdded = false;

        for (MakaleDugumu neighbor : result.getHCoreNodes()) {
            // Eğer bu düğüm zaten ekranda varsa yerine dokunma
            if (!nodePositions.containsKey(neighbor)) {
                // Rastgele bir açı ile etrafa dağıt
                double angle = random.nextDouble() * 2 * Math.PI;

                double newX = centerPos.getX() + dist * Math.cos(angle);
                double newY = centerPos.getY() + dist * Math.sin(angle);

                // Ekran sınırlarına çarpmasın diye basit kontrol
                double paneW = graphPane.getWidth() > 0 ? graphPane.getWidth() : 800;
                double paneH = graphPane.getHeight() > 0 ? graphPane.getHeight() : 600;
                newX = Math.max(40, Math.min(newX, paneW - 40));
                newY = Math.max(40, Math.min(newY, paneH - 40));

                nodePositions.put(neighbor, new Point2D(newX, newY));
                anyNewAdded = true;
            }
        }

        // 5. Grafiği Yeniden Çiz
        if (anyNewAdded) {
            // İsterseniz burada force layout çağırabilirsiniz ama karışıklık olmaması için
            // şimdilik sadece çizimi yeniliyoruz.
            redrawInteractive();
        } else {
            outputArea.appendText("\n-> (Bu makalenin tüm H-Core bağlantıları zaten ekranda.)");
        }
    }

    private void redrawInteractive() {
        GrafRenderlayici.drawInteractiveGraph(graphPane, nodePositions, this::handleNodeClick);
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