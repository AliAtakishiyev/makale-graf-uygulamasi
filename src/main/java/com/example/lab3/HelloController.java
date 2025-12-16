package com.example.lab3;

import com.example.lab3.analysis.HIndexCalculator;
import com.example.lab3.analysis.HIndexResult;
import com.example.lab3.jsonreader.JsonArticleReader;
import com.example.lab3.models.Article;
import com.example.lab3.models.ArticleGraph;
import com.example.lab3.models.ArticleNode;
import com.example.lab3.ui.GraphRenderer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class HelloController {

    // Örnek test için JSON dosyasının tam yolu
    private static final Path ARTICLES_JSON_PATH = Path.of(
            "/Users/aliatakishiyev/Documents/KOU/lab/Lab3/src/main/java/com/example/lab3/utlis/makale.json"
    );

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField articleIdField;

    @FXML
    private Label errorLabel;

    @FXML
    private Pane graphPane;

    // Bellekteki tam graf; JSON yüklendikten sonra doldurulacak
    private ArticleGraph graph;

    @FXML
    protected void onLoadJsonClick() {
        clearError();
        try {
            // 1) JSON'dan makaleleri oku
            List<Article> articles = JsonArticleReader.readArticles(ARTICLES_JSON_PATH);

            // 2) Bellekte tam grafı kur
            this.graph = ArticleGraph.buildFromArticles(articles);

            // 3) İstatistikleri hesapla
            int nodeCount = graph.getNodeCount();
            int edgeCount = graph.getEdgeCount();
            int totalOutgoing = graph.getTotalOutgoingReferences();
            int totalIncoming = graph.getTotalIncomingReferences();
            ArticleGraph.ArticleIdAndCount mostCited = graph.getMostCitedArticle();
            ArticleGraph.ArticleIdAndCount mostCiting = graph.getMostCitingArticle();

            // 4) Sonuçları text alanında ve konsolda göster
            StringBuilder sb = new StringBuilder();
            sb.append("=== Genel Graf İstatistikleri ===\n");
            sb.append("Toplam Makale (Düğüm): ").append(nodeCount).append("\n");
            sb.append("Toplam Referans (Kenar): ").append(edgeCount).append("\n");
            sb.append("Toplam Verilen Referans: ").append(totalOutgoing).append("\n");
            sb.append("Toplam Alınan Referans: ").append(totalIncoming).append("\n");
            sb.append("En Çok Referans Alan: ").append(mostCited.articleId())
                    .append(" (").append(mostCited.count()).append(")\n");
            sb.append("En Çok Referans Veren: ").append(mostCiting.articleId())
                    .append(" (").append(mostCiting.count()).append(")\n");
            sb.append("\nBir makale için H-index hesaplamak üzere yukarıdaki alana makale id girin.\n");

            String message = sb.toString();
            outputArea.setText(message);
            graphPane.getChildren().clear(); // JSON yeniden yüklendiğinde grafı temizle
            System.out.println(message);
        } catch (IOException e) {
            showError("JSON okuma hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Graf oluşturma hatası: " + e.getMessage());
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

        // ID'nin graf içinde olup olmadığını kontrol et
        ArticleNode node = graph.getNode(id);
        if (node == null) {
            showError("'" + id + "' ID'li makale sistemde bulunamadı.");
            return;
        }

        try {
            HIndexResult result = HIndexCalculator.computeForArticle(graph, id);

            StringBuilder sb = new StringBuilder();
            sb.append("\n=== H-index Sonuçları ===\n");
            sb.append("Makale ID: ").append(id).append("\n");
            sb.append("h-index: ").append(result.getHIndex()).append("\n");
            sb.append("h-median: ").append(result.getHMedian()).append("\n");
            sb.append("h-core makaleleri (id / aldığı atıf sayısı):\n");

            for (ArticleNode hCoreNode : result.getHCoreNodes()) {
                sb.append("  - ")
                        .append(hCoreNode.getArticle().getId())
                        .append(" / in-degree = ")
                        .append(hCoreNode.getInDegree())
                        .append("\n");
            }

            outputArea.appendText(sb.toString());
            // Grafiği güncelle
            GraphRenderer.drawHCoreGraph(graphPane, node, result.getHCoreNodes());
            System.out.println(sb);
        } catch (Exception ex) {
            showError("Beklenmeyen hata: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setText("");
    }
}
