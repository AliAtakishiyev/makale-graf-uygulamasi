package com.example.lab3.analysis;

import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HIndexHesaplama {

    public static HIndexSonuc computeForArticle(MakaleGrafı graf, String makaleId) {
        MakaleDugumu merkez = graf.getDugum(makaleId);

        if (merkez == null) {
            throw new IllegalArgumentException("Verilen id ile makale bulunamadı: " + makaleId);
        }

        List<MakaleDugumu> atifYapanlar = new ArrayList<>(merkez.getGelenler()); // Atıflar listelenir.

        if (atifYapanlar.isEmpty()) {
            return new HIndexSonuc(0, 0.0, List.of()); // Atıf yok
        }

        atifYapanlar.sort(Comparator.comparingInt(MakaleDugumu::getGirisDerecesi).reversed()); // Atıfları sıralıyoruz.

        List<Integer> atifSayilari = new ArrayList<>(); // Sayıları listeliyoruz.
        for (MakaleDugumu dugum : atifYapanlar) {
            atifSayilari.add(dugum.getGirisDerecesi());
        }

        int h = 0;
        for (int i = 0; i < atifSayilari.size(); i++) { // Hindex bulma algoritması
            int atifSayisi = atifSayilari.get(i);
            int siraNumarasi = i + 1;

            if (atifSayisi >= siraNumarasi) {
                h = siraNumarasi;
            } else {
                break;
            }
        }

        if (h == 0) {
            return new HIndexSonuc(0, 0.0, List.of());
        }

        List<MakaleDugumu> hCekirdegi = new ArrayList<>(atifYapanlar.subList(0, h));
        List<Integer> hCekirdekAtiflari = atifSayilari.subList(0, h);

        double hMedyan;

        if (h % 2 == 1) {
            hMedyan = hCekirdekAtiflari.get(h / 2);
        } else {
            int orta1 = hCekirdekAtiflari.get(h / 2 - 1);
            int orta2 = hCekirdekAtiflari.get(h / 2);
            hMedyan = (orta1 + orta2) / 2.0;
        }

        return new HIndexSonuc(h, hMedyan, hCekirdegi);
    }
}