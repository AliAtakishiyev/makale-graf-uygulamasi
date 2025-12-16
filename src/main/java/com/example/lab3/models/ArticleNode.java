package com.example.lab3.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graf içindeki tek bir düğümü temsil eder.
 * Article + gelen ve giden kenar listeleri tutar.
 */
public class ArticleNode {

    private final Article article;
    private final List<ArticleNode> outgoing = new ArrayList<>(); // this -> referenced
    private final List<ArticleNode> incoming = new ArrayList<>(); // citer -> this

    public ArticleNode(Article article) {
        this.article = article;
    }

    public Article getArticle() {
        return article;
    }

    public List<ArticleNode> getOutgoing() {
        return Collections.unmodifiableList(outgoing);
    }

    public List<ArticleNode> getIncoming() {
        return Collections.unmodifiableList(incoming);
    }

    void addOutgoing(ArticleNode target) {
        if (!outgoing.contains(target)) {
            outgoing.add(target);
        }
    }

    void addIncoming(ArticleNode source) {
        if (!incoming.contains(source)) {
            incoming.add(source);
        }
    }

    /**
     * Bu makalenin verdiği referans sayısı (out-degree).
     */
    public int getOutDegree() {
        return outgoing.size();
    }

    /**
     * Bu makalenin aldığı referans sayısı (in-degree).
     */
    public int getInDegree() {
        return incoming.size();
    }
}


