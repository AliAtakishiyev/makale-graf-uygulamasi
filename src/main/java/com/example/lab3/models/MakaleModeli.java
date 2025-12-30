package com.example.lab3.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MakaleModeli {

    private final String id;
    private final String doi;
    private final String baslik;
    private final int yil;
    private final List<String> yazarlar;
    private final List<String> referanslar;

    public MakaleModeli(
            String id,
            String doi,
            String baslik,
            int yil,
            List<String> yazarlar,
            List<String> referanslar
    ) {
        this.id = Objects.requireNonNull(id, "id null olamaz");
        this.doi = doi;
        this.baslik = Objects.requireNonNull(baslik, "baslik null olamaz");
        this.yil = yil;
        this.yazarlar = new ArrayList<>(yazarlar != null ? yazarlar : List.of());
        this.referanslar = new ArrayList<>(referanslar != null ? referanslar : List.of());
    }

    public String getId() {
        return id;
    }

    public String getDoi() {
        return doi;
    }

    public String getBaslik() {
        return baslik;
    }
    public String getTitle() { return getBaslik(); }

    public int getYil() {
        return yil;
    }
    public int getYear() { return getYil(); }

    public List<String> getYazarlar() {
        return Collections.unmodifiableList(yazarlar);
    }
    public List<String> getAuthors() { return getYazarlar(); }

    public List<String> getReferanslar() {
        return Collections.unmodifiableList(referanslar);
    }
    public List<String> getReferencedWorks() { return getReferanslar(); }

    @Override
    public String toString() {
        return "Makale{" +
                "id='" + id + '\'' +
                ", yil=" + yil +
                ", baslik='" + baslik + '\'' +
                '}';
    }
}