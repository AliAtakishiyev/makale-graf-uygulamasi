package com.example.lab3.analysis;

import com.example.lab3.models.MakaleDugumu;
import java.util.*;

public class BetweenHesaplayici {

    public static int computeCentrality(List<MakaleDugumu> gorunurDugumler, MakaleDugumu hedefDugum) {
        int skor = 0;
        int n = gorunurDugumler.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                MakaleDugumu baslangicDugumu = gorunurDugumler.get(i);
                MakaleDugumu bitisDugumu = gorunurDugumler.get(j);


                if (baslangicDugumu.equals(hedefDugum) || bitisDugumu.equals(hedefDugum)) {
                    continue;
                }

                List<MakaleDugumu> yol = findUndirectedShortestPath(gorunurDugumler, baslangicDugumu, bitisDugumu);

                if (!yol.isEmpty() && yol.contains(hedefDugum)) {
                    skor++;
                }
            }
        }
        return skor;
    }

    private static List<MakaleDugumu> findUndirectedShortestPath(List<MakaleDugumu> kapsam, MakaleDugumu baslangic, MakaleDugumu bitis) {
        if (baslangic.equals(bitis)) return List.of(baslangic);

        Queue<MakaleDugumu> kuyruk = new LinkedList<>();
        Map<MakaleDugumu, MakaleDugumu> ebeveynHaritasi = new HashMap<>();
        Set<MakaleDugumu> ziyaretEdilenler = new HashSet<>();

        Set<MakaleDugumu> gecerliDugumler = new HashSet<>(kapsam);

        kuyruk.add(baslangic);
        ziyaretEdilenler.add(baslangic);
        ebeveynHaritasi.put(baslangic, null);

        while (!kuyruk.isEmpty()) {
            MakaleDugumu mevcut = kuyruk.poll();

            if (mevcut.equals(bitis)) {
                return yoluOlustur(ebeveynHaritasi, bitis);
            }

            Set<MakaleDugumu> tumKomsular = new HashSet<>();
            if (mevcut.getGidenler() != null) tumKomsular.addAll(mevcut.getGidenler());
            if (mevcut.getGelenler() != null) tumKomsular.addAll(mevcut.getGelenler());

            for (MakaleDugumu komsu : tumKomsular) {
                if (gecerliDugumler.contains(komsu) && !ziyaretEdilenler.contains(komsu)) {
                    ziyaretEdilenler.add(komsu);
                    ebeveynHaritasi.put(komsu, mevcut);
                    kuyruk.add(komsu);
                }
            }
        }
        return Collections.emptyList(); // Yol yok
    }

    private static List<MakaleDugumu> yoluOlustur(Map<MakaleDugumu, MakaleDugumu> ebeveynHaritasi, MakaleDugumu bitisDugumu) {
        List<MakaleDugumu> yol = new LinkedList<>();
        MakaleDugumu mevcut = bitisDugumu;
        while (mevcut != null) {
            yol.add(0, mevcut);
            mevcut = ebeveynHaritasi.get(mevcut);
        }
        return yol;
    }
}