package com.example.lab3.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tüm makaleleri ve aralarındaki referans ilişkilerini bellekte tutan graf yapısı.
 *
 * Bu sınıf "tam grafı" temsil eder; ekranda gösterilen alt-graf bu yapıdan türetilecektir.
 */
public class ArticleGraph {

    // id -> node
    private final Map<String, ArticleNode> nodesById = new HashMap<>();

    private ArticleGraph() {
    }

    /**
     * JSON'dan okunmuş makale listesinden yönlü graf oluşturur.
     *
     * @param articles Article listesi
     * @return Kurulmuş ArticleGraph
     */
    public static ArticleGraph buildFromArticles(List<Article> articles) {
        ArticleGraph graph = new ArticleGraph();

        // 1) Tüm makaleler için node oluştur
        for (Article article : articles) {
            graph.nodesById.put(article.getId(), new ArticleNode(article));
        }

        // 2) Referanslara göre kenarları kur
        for (ArticleNode node : graph.nodesById.values()) {
            Article article = node.getArticle();
            for (String refId : article.getReferencedWorks()) {
                ArticleNode target = graph.nodesById.get(refId);
                if (target != null) {
                    // yönlü kenar: node (source) -> target
                    node.addOutgoing(target);
                    target.addIncoming(node);
                }
            }
        }

        return graph;
    }

    public ArticleNode getNode(String id) {
        return nodesById.get(id);
    }

    public Collection<ArticleNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodesById.values());
    }

    public int getNodeCount() {
        return nodesById.size();
    }

    /**
     * Yönlü kenar sayısı (toplam referans ilişkisi).
     */
    public int getEdgeCount() {
        int sum = 0;
        for (ArticleNode node : nodesById.values()) {
            sum += node.getOutDegree();
        }
        return sum;
    }

    /**
     * Toplam verilen referans sayısı (tüm düğümlerin out-degree toplamı).
     * Bu aslında getEdgeCount() ile aynı sonucu verir.
     */
    public int getTotalOutgoingReferences() {
        return getEdgeCount();
    }

    /**
     * Toplam alınan referans sayısı (tüm düğümlerin in-degree toplamı).
     */
    public int getTotalIncomingReferences() {
        int sum = 0;
        for (ArticleNode node : nodesById.values()) {
            sum += node.getInDegree();
        }
        return sum;
    }

    /**
     * En çok referans alan makale bilgisi.
     * @return ArticleIdAndCount record'u (id ve aldığı referans sayısı)
     */
    public ArticleIdAndCount getMostCitedArticle() {
        ArticleNode mostCited = null;
        int maxInDegree = -1;

        for (ArticleNode node : nodesById.values()) {
            int inDegree = node.getInDegree();
            if (inDegree > maxInDegree) {
                maxInDegree = inDegree;
                mostCited = node;
            }
        }

        if (mostCited == null) {
            return new ArticleIdAndCount("", 0);
        }
        return new ArticleIdAndCount(mostCited.getArticle().getId(), maxInDegree);
    }

    /**
     * En çok referans veren makale bilgisi.
     * @return ArticleIdAndCount record'u (id ve verdiği referans sayısı)
     */
    public ArticleIdAndCount getMostCitingArticle() {
        ArticleNode mostCiting = null;
        int maxOutDegree = -1;

        for (ArticleNode node : nodesById.values()) {
            int outDegree = node.getOutDegree();
            if (outDegree > maxOutDegree) {
                maxOutDegree = outDegree;
                mostCiting = node;
            }
        }

        if (mostCiting == null) {
            return new ArticleIdAndCount("", 0);
        }
        return new ArticleIdAndCount(mostCiting.getArticle().getId(), maxOutDegree);
    }

    /**
     * Makale ID ve sayı bilgisini taşıyan basit record.
     */
    public record ArticleIdAndCount(String articleId, int count) {
    }
}


