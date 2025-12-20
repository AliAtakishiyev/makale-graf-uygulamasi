package com.example.lab3.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MakaleDugumu {

    private final MakaleModeli article;
    private final List<MakaleDugumu> outgoing = new ArrayList<>();
    private final List<MakaleDugumu> incoming = new ArrayList<>();

    public MakaleDugumu(MakaleModeli article) {
        this.article = article;
    }

    public MakaleModeli getArticle() {
        return article;
    }

    public List<MakaleDugumu> getOutgoing() {
        return Collections.unmodifiableList(outgoing);
    }

    public List<MakaleDugumu> getIncoming() {
        return Collections.unmodifiableList(incoming);
    }

    void addOutgoing(MakaleDugumu target) {
        if (!outgoing.contains(target)) {
            outgoing.add(target);
        }
    }

    void addIncoming(MakaleDugumu source) {
        if (!incoming.contains(source)) {
            incoming.add(source);
        }
    }


    public int getOutDegree() {
        return outgoing.size();
    }

    public int getInDegree() {
        return incoming.size();
    }
}


