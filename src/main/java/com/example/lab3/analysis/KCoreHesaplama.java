package com.example.lab3.analysis;

import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KCoreHesaplama {

    public static List<MakaleDugumu> computeKCore(MakaleGrafı graf, int k) { // Ana Metod
        List<MakaleDugumu> aktifDugumler = new ArrayList<>(graf.tumDugumleriGetir()); // Kopyalama

        Set<MakaleDugumu> aktifKume = new HashSet<>(aktifDugumler);

        boolean herhangiSilindiMi;
        do {
            herhangiSilindiMi = false;
            List<MakaleDugumu> silinecekler = new ArrayList<>();

            for (MakaleDugumu dugum : aktifDugumler) {
                int mevcutDerece = kumedeDereceHesapla(dugum, aktifKume);

                if (mevcutDerece < k) {
                    silinecekler.add(dugum);
                }
            }

            if (!silinecekler.isEmpty()) {
                aktifDugumler.removeAll(silinecekler);
                aktifKume.removeAll(silinecekler);
                herhangiSilindiMi = true;
            }

        } while (herhangiSilindiMi);

        return aktifDugumler; // K şartı sağlanan düğümleri dönderiyoruz
    }

    private static int kumedeDereceHesapla(MakaleDugumu dugum, Set<MakaleDugumu> aktifKume) {
        int derece = 0;

        for (MakaleDugumu kaynak : dugum.getGelenler()) {
            if (aktifKume.contains(kaynak)) {
                derece++;
            }
        }

        for (MakaleDugumu hedef : dugum.getGidenler()) {
            if (aktifKume.contains(hedef)) {
                derece++;
            }
        }

        return derece;
    }

    public static List<MakaleDugumu> computeLocalKCore(List<MakaleDugumu> gorunurDugumler, int k) {
        List<MakaleDugumu> aktifDugumler = new ArrayList<>(gorunurDugumler);
        Set<MakaleDugumu> aktifKume = new HashSet<>(aktifDugumler);

        boolean herhangiSilindiMi;
        do {
            herhangiSilindiMi = false;
            List<MakaleDugumu> silinecekler = new ArrayList<>();

            for (MakaleDugumu dugum : aktifDugumler) {
                int mevcutDerece = kumedeDereceHesapla(dugum, aktifKume);

                if (mevcutDerece < k) {
                    silinecekler.add(dugum);
                }
            }

            if (!silinecekler.isEmpty()) {
                aktifDugumler.removeAll(silinecekler);
                aktifKume.removeAll(silinecekler);
                herhangiSilindiMi = true;
            }

        } while (herhangiSilindiMi);

        return aktifDugumler;
    }
}