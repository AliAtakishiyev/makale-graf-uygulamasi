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
public class MakaleGrafı {

    // id -> node
    private final Map<String, MakaleDugumu> nodesById = new HashMap<>();

    private MakaleGrafı() {
    }

    /**
     * JSON'dan okunmuş makale listesinden yönlü graf oluşturur.
     *
     * @param articles Article listesi
     * @return Kurulmuş ArticleGraph
     */
    public static MakaleGrafı buildFromArticles(List<MakaleModeli> articles) {
        MakaleGrafı graph = new MakaleGrafı();

        // 1) Tüm makaleler için node oluştur
        for (MakaleModeli article : articles) {
            graph.nodesById.put(article.getId(), new MakaleDugumu(article));
        }

        // 2) Referanslara göre kenarları kur
        for (MakaleDugumu node : graph.nodesById.values()) {
            MakaleModeli article = node.getArticle();
            for (String refId : article.getReferencedWorks()) {
                MakaleDugumu target = graph.nodesById.get(refId);
                if (target != null) {
                    // yönlü kenar: node (source) -> target
                    node.addOutgoing(target);
                    target.addIncoming(node);
                }
            }
        }

        return graph;
    }

    public MakaleDugumu getNode(String id) {
        return nodesById.get(id);
    }

    public Collection<MakaleDugumu> getAllNodes() {
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
        for (MakaleDugumu node : nodesById.values()) {
            sum += node.getOutDegree();
        }
        return sum;
    }

    public int getTotalOutgoingReferences() {
        return getEdgeCount();
    }

    public int getTotalIncomingReferences() {
        int sum = 0;
        for (MakaleDugumu node : nodesById.values()) {
            sum += node.getInDegree();
        }
        return sum;
    }

    public ArticleIdAndCount getMostCitedArticle() {
        MakaleDugumu mostCited = null;
        int maxInDegree = -1;

        for (MakaleDugumu node : nodesById.values()) {
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

    public ArticleIdAndCount getMostCitingArticle() {
        MakaleDugumu mostCiting = null;
        int maxOutDegree = -1;

        for (MakaleDugumu node : nodesById.values()) {
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

    public record ArticleIdAndCount(String articleId, int count) {
    }
}


