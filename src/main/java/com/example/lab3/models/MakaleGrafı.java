package com.example.lab3.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakaleGrafı {

    // id -> dugum
    private final Map<String, MakaleDugumu> idyeGoreDugumler = new HashMap<>();

    private MakaleGrafı() {
    }

    public static MakaleGrafı buildFromArticles(List<MakaleModeli> makaleler) {
        MakaleGrafı graf = new MakaleGrafı();

        for (MakaleModeli makale : makaleler) {
            graf.idyeGoreDugumler.put(makale.getId(), new MakaleDugumu(makale));
        }

        for (MakaleDugumu dugum : graf.idyeGoreDugumler.values()) {
            MakaleModeli makale = dugum.getMakale();
            for (String refId : makale.getReferanslar()) {
                MakaleDugumu hedef = graf.idyeGoreDugumler.get(refId);
                if (hedef != null) {
                    dugum.gidenEkle(hedef);
                    hedef.gelenEkle(dugum);
                }
            }
        }
        return graf;
    }

    public MakaleDugumu getDugum(String id) {
        return idyeGoreDugumler.get(id);
    }
    public MakaleDugumu getNode(String id) { return getDugum(id); }

    public Collection<MakaleDugumu> tumDugumleriGetir() {
        return Collections.unmodifiableCollection(idyeGoreDugumler.values());
    }
    public Collection<MakaleDugumu> getAllNodes() { return tumDugumleriGetir(); }

    public int getDugumSayisi() {
        return idyeGoreDugumler.size();
    }
    public int getNodeCount() { return getDugumSayisi(); }

    public int getKenarSayisi() {
        int toplam = 0;
        for (MakaleDugumu dugum : idyeGoreDugumler.values()) {
            toplam += dugum.getCikisDerecesi();
        }
        return toplam;
    }
    public int getEdgeCount() { return getKenarSayisi(); }

    public MakaleIdVeSayisi enCokAtifAlanMakale() {
        MakaleDugumu enCokAtifAlan = null;
        int maksGirisDerecesi = -1;

        for (MakaleDugumu dugum : idyeGoreDugumler.values()) {
            int girisDerecesi = dugum.getGirisDerecesi();
            if (girisDerecesi > maksGirisDerecesi) {
                maksGirisDerecesi = girisDerecesi;
                enCokAtifAlan = dugum;
            }
        }

        if (enCokAtifAlan == null) {
            return new MakaleIdVeSayisi("", 0);
        }
        return new MakaleIdVeSayisi(enCokAtifAlan.getMakale().getId(), maksGirisDerecesi);
    }
    public MakaleIdVeSayisi getMostCitedArticle() { return enCokAtifAlanMakale(); }

    public MakaleIdVeSayisi enCokAtifVerenMakale() {
        MakaleDugumu enCokAtifVeren = null;
        int maksCikisDerecesi = -1;

        for (MakaleDugumu dugum : idyeGoreDugumler.values()) {
            int cikisDerecesi = dugum.getCikisDerecesi();
            if (cikisDerecesi > maksCikisDerecesi) {
                maksCikisDerecesi = cikisDerecesi;
                enCokAtifVeren = dugum;
            }
        }

        if (enCokAtifVeren == null) {
            return new MakaleIdVeSayisi("", 0);
        }
        return new MakaleIdVeSayisi(enCokAtifVeren.getMakale().getId(), maksCikisDerecesi);
    }
    public MakaleIdVeSayisi getMostCitingArticle() { return enCokAtifVerenMakale(); }

    public record MakaleIdVeSayisi(String makaleId, int sayi) {
        public String articleId() { return makaleId; }
        public int count() { return sayi; }
    }
}