package com.example.lab3.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MakaleDugumu {

    private final MakaleModeli makale;
    private final List<MakaleDugumu> gidenler = new ArrayList<>();
    private final List<MakaleDugumu> gelenler = new ArrayList<>();

    public MakaleDugumu(MakaleModeli makale) {
        this.makale = makale;
    }

    public MakaleModeli getMakale() {
        return makale;
    }

    public MakaleModeli getArticle() { return makale; }

    public List<MakaleDugumu> getGidenler() {
        return Collections.unmodifiableList(gidenler);
    }

    public List<MakaleDugumu> getOutgoing() { return getGidenler(); }

    public List<MakaleDugumu> getGelenler() {
        return Collections.unmodifiableList(gelenler);
    }

    public List<MakaleDugumu> getIncoming() { return getGelenler(); }

    void gidenEkle(MakaleDugumu hedef) {
        if (!gidenler.contains(hedef)) {
            gidenler.add(hedef);
        }
    }

    void addOutgoing(MakaleDugumu target) { gidenEkle(target); }

    void gelenEkle(MakaleDugumu kaynak) {
        if (!gelenler.contains(kaynak)) {
            gelenler.add(kaynak);
        }
    }

    void addIncoming(MakaleDugumu source) { gelenEkle(source); }

    public int getCikisDerecesi() {
        return gidenler.size();
    }
    public int getOutDegree() { return getCikisDerecesi(); }

    public int getGirisDerecesi() {
        return gelenler.size();
    }
    public int getInDegree() { return getGirisDerecesi(); }
}