package com.example.lab3;

import com.example.lab3.jsonreader.JsonArticleReader;
import com.example.lab3.models.Article;
import com.example.lab3.models.ArticleGraph;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

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
    protected void onLoadJsonClick() {
        try {
            // 1) JSON'dan makaleleri oku
            List<Article> articles = JsonArticleReader.readArticles(ARTICLES_JSON_PATH);

            // 2) Bellekte tam grafı kur
            ArticleGraph graph = ArticleGraph.buildFromArticles(articles);

            // 3) İstatistikleri hesapla
            int nodeCount = graph.getNodeCount();
            int edgeCount = graph.getEdgeCount();
            int totalOutgoing = graph.getTotalOutgoingReferences();
            int totalIncoming = graph.getTotalIncomingReferences();
            ArticleGraph.ArticleIdAndCount mostCited = graph.getMostCitedArticle();
            ArticleGraph.ArticleIdAndCount mostCiting = graph.getMostCitingArticle();

            // 4) Sonuçları text alanında ve konsolda göster
            StringBuilder sb = new StringBuilder();
            sb.append("Toplam Makale (Düğüm): ").append(nodeCount).append("\n");
            sb.append("Toplam Referans (Kenar): ").append(edgeCount).append("\n");
            sb.append("Toplam Verilen Referans: ").append(totalOutgoing).append("\n");
            sb.append("Toplam Alınan Referans: ").append(totalIncoming).append("\n");
            sb.append("En Çok Referans Alan: ").append(mostCited.articleId())
                    .append(" (").append(mostCited.count()).append(")\n");
            sb.append("En Çok Referans Veren: ").append(mostCiting.articleId())
                    .append(" (").append(mostCiting.count()).append(")\n");

            String message = sb.toString();
            outputArea.setText(message);
            System.out.println(message);
        } catch (IOException e) {
            String errorMsg = "JSON okuma hatası: " + e.getMessage();
            outputArea.setText(errorMsg);
            e.printStackTrace();
        } catch (Exception e) {
            String errorMsg = "Graf oluşturma hatası: " + e.getMessage();
            outputArea.setText(errorMsg);
            e.printStackTrace();
        }
    }
}
