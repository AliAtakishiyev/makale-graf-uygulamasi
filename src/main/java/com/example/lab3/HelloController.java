package com.example.lab3;

import com.example.lab3.analysis.*;
import com.example.lab3.jsonreader.JsonOkuyucu;
import com.example.lab3.models.MakaleModeli;
import com.example.lab3.models.MakaleGrafı;
import com.example.lab3.models.MakaleDugumu;
import com.example.lab3.ui.GrafRenderlayici;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HelloController {

    private static final Path MAKALELER_JSON_YOLU = Path.of(
            "C:\\Users/dgknb/OneDrive/Desktop/ProLab3/makale-graf-uygulamasi/src/main/java/com/example/lab3/utlis/makale.json"
    );

    @FXML private TextArea ciktiAlani;
    @FXML private TextField makaleIdAlani;
    @FXML private TextField kGiris;
    @FXML private Label hataEtiketi;
    @FXML private Pane grafPaneli;

    private MakaleGrafı graf;
    private final Map<MakaleDugumu, Point2D> dugumPozisyonlari = new HashMap<>();

    private final Random rastgele = new Random();

    @FXML
    protected void onLoadJsonClick() {
        hataTemizle();
        try {
            List<MakaleModeli> makaleler = JsonOkuyucu.readArticles(MAKALELER_JSON_YOLU);
            this.graf = MakaleGrafı.buildFromArticles(makaleler);

            int dugumSayisi = graf.getDugumSayisi();
            int kenarSayisi = graf.getKenarSayisi();

            MakaleGrafı.MakaleIdVeSayisi enCokAtifAlan = graf.enCokAtifAlanMakale();
            MakaleGrafı.MakaleIdVeSayisi enCokAtifVeren = graf.enCokAtifVerenMakale();

            StringBuilder sb = new StringBuilder();
            sb.append("Genel Graf İstatistikleri\n");
            sb.append("Toplam Makale: ").append(dugumSayisi).append("\n");
            sb.append("Toplam Referans: ").append(kenarSayisi).append("\n");

            if (enCokAtifAlan != null) {
                sb.append("En Çok Atıf Alan: ").append(enCokAtifAlan.makaleId())
                        .append(" (").append(enCokAtifAlan.sayi()).append(")\n");
            }
            if (enCokAtifVeren != null) {
                sb.append("En Çok Atıf Veren: ").append(enCokAtifVeren.makaleId())
                        .append(" (").append(enCokAtifVeren.sayi()).append(")\n");
            }
            sb.append("\nİşlem yapmak için ID veya K değeri giriniz.\n");

            ciktiAlani.setText(sb.toString());
            grafPaneli.getChildren().clear();
            dugumPozisyonlari.clear();

        } catch (IOException e) {
            hataGoster("JSON okuma hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            hataGoster("Graf oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    protected void onLocalKCoreClick() {
        ciktiAlani.setText("");

        if (dugumPozisyonlari.isEmpty()) {
            ciktiAlani.setText("Hata: Ekranda bir graf yok.");
            return;
        }

        try {
            if (kGiris == null || kGiris.getText().isBlank()) {
                ciktiAlani.setText("Hata: Lütfen bir k değeri girin.");
                return;
            }
            int k = Integer.parseInt(kGiris.getText().trim());

            List<MakaleDugumu> gorunurDugumler = new ArrayList<>(dugumPozisyonlari.keySet());
            List<MakaleDugumu> kalanlar = KCoreHesaplama.computeLocalKCore(gorunurDugumler, k);

            ciktiAlani.setText("Yerel K-Core Analizi\n");
            ciktiAlani.appendText("Analiz edilen düğüm: " + gorunurDugumler.size() + "\n");
            ciktiAlani.appendText("K=" + k + " çekirdeğindeki düğüm: " + kalanlar.size() + "\n");

            if (kalanlar.isEmpty()) {
                ciktiAlani.appendText("Sonuç: Bu kriteri sağlayan hiçbir düğüm yok.\n");
            } else {
                ciktiAlani.appendText("Sonuç: K-Core üyeleri KIRMIZI, elenenler GRİ renkle gösterildi.\n");
            }

            GrafRenderlayici.drawInteractiveGraphWithHighlight(
                    grafPaneli,
                    dugumPozisyonlari,
                    kalanlar,
                    this::dugumTiklamaIslemi
            );

        } catch (NumberFormatException e) {
            ciktiAlani.setText("Hata: Geçersiz sayı.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onComputeHIndexClick() {
        hataTemizle();
        if (graf == null) {
            hataGoster("Lütfen önce JSON'u yükleyin.");
            return;
        }

        String id = makaleIdAlani.getText();
        if (id == null || id.isBlank()) {
            hataGoster("Lütfen geçerli bir makale ID girin.");
            return;
        }

        id = idNormalizeEt(id.trim());
        MakaleDugumu merkezDugum = graf.getDugum(id);

        if (merkezDugum == null) {
            hataGoster("'" + id + "' ID'li makale bulunamadı.");
            return;
        }

        try {
            HIndexSonuc sonuc = HIndexHesaplama.computeForArticle(graf, id);

            StringBuilder sb = new StringBuilder();
            sb.append("H-index Sonuçları\n");
            sb.append("Makale: ").append(merkezDugum.getMakale().getBaslik()).append("\n");
            sb.append("h-index: ").append(sonuc.getHIndex()).append("\n");
            sb.append("h-median: ").append(sonuc.getHMedian()).append("\n");
            sb.append("h-core sayısı: ").append(sonuc.getHCoreNodes().size()).append("\n");

            ciktiAlani.setText(sb.toString());

            dugumPozisyonlari.clear();

            double panelG = grafPaneli.getWidth() > 0 ? grafPaneli.getWidth() : 800;
            double panelY = grafPaneli.getHeight() > 0 ? grafPaneli.getHeight() : 600;

            dugumPozisyonlari.put(merkezDugum, new Point2D(panelG / 2, panelY / 2));

            List<MakaleDugumu> hCekirdegi = sonuc.getHCoreNodes();
            double yaricap = 150;
            for (int i = 0; i < hCekirdegi.size(); i++) {
                double aci = 2 * Math.PI * i / hCekirdegi.size();
                double x = (panelG / 2) + yaricap * Math.cos(aci);
                double y = (panelY / 2) + yaricap * Math.sin(aci);
                dugumPozisyonlari.put(hCekirdegi.get(i), new Point2D(x, y));
            }

            grafigiYenidenCiz();

        } catch (Exception ex) {
            hataGoster("Hata: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void dugumTiklamaIslemi(MakaleDugumu tiklananDugum) {
        String yeniId = tiklananDugum.getMakale().getId();
        makaleIdAlani.setText(yeniId);

        HIndexSonuc sonuc = HIndexHesaplama.computeForArticle(graf, yeniId);

        ciktiAlani.appendText("\n\n----------------------------------\n");
        ciktiAlani.appendText("GENİŞLETİLEN: " + tiklananDugum.getMakale().getBaslik() + "\n");
        ciktiAlani.appendText("h-index: " + sonuc.getHIndex() + "\n");
        ciktiAlani.appendText("Eklenen Bağlantı: " + sonuc.getHCoreNodes().size() + "\n");

        Point2D merkezPoz = dugumPozisyonlari.get(tiklananDugum);
        if (merkezPoz == null) merkezPoz = new Point2D(400, 300);

        double mesafe = 100.0;
        boolean yeniEklendiMi = false;

        for (MakaleDugumu komsu : sonuc.getHCoreNodes()) {
            if (!dugumPozisyonlari.containsKey(komsu)) {
                double aci = rastgele.nextDouble() * 2 * Math.PI;
                double yeniX = merkezPoz.getX() + mesafe * Math.cos(aci);
                double yeniY = merkezPoz.getY() + mesafe * Math.sin(aci);

                dugumPozisyonlari.put(komsu, new Point2D(yeniX, yeniY));
                yeniEklendiMi = true;
            }
        }

        if (yeniEklendiMi) {
            kuvvetDuzeniniUygula();
            grafigiYenidenCiz();
        } else {
            ciktiAlani.appendText("Bağlantılar zaten ekranda.");
        }
    }

    private void grafigiYenidenCiz() {
        GrafRenderlayici.drawInteractiveGraph(grafPaneli, dugumPozisyonlari, this::dugumTiklamaIslemi);
    }

    @FXML
    protected void onShowGreenChainClick() {
        hataTemizle();
        if (graf == null) {
            hataGoster("Lütfen önce JSON dosyasını yükleyin.");
            return;
        }

        try {
            List<MakaleDugumu> tumDugumler = new ArrayList<>(graf.tumDugumleriGetir());
            tumDugumler.sort((n1, n2) -> n1.getMakale().getId().compareTo(n2.getMakale().getId()));

            int limit = Math.min(tumDugumler.size(), 100);
            List<MakaleDugumu> siraliListe = tumDugumler.subList(0, limit);

            ciktiAlani.setText("ID Sıralı Zincir (Yeşil Hat)\n");
            ciktiAlani.appendText("Görüntülenen: " + siraliListe.size() + " makale.\n");

            GrafRenderlayici.drawGreenLineChain(grafPaneli, siraliListe, this::zincirDugumTiklama);

        } catch (Exception e) {
            hataGoster("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void zincirDugumTiklama(MakaleDugumu dugum) {
        makaleIdAlani.setText(dugum.getMakale().getId());
        onComputeHIndexClick();
    }

    @FXML
    protected void onCalculateCentralityClick() {
        ciktiAlani.setText("");

        String hedefId = makaleIdAlani.getText();
        if (hedefId == null || hedefId.isBlank()) {
            ciktiAlani.setText("Hata: Lütfen bir makale ID girin veya graf üzerinden bir düğüme tıklayın.");
            return;
        }
        hedefId = idNormalizeEt(hedefId.trim());

        MakaleDugumu hedefDugum = graf.getDugum(hedefId);
        if (hedefDugum == null) {
            ciktiAlani.setText("Hata: Bu ID veritabanında bulunamadı.");
            return;
        }

        if (dugumPozisyonlari.isEmpty()) {
            ciktiAlani.setText("Hata: Önce ekrana bir graf çizdirin (H-Index veya Zincir ile).");
            return;
        }

        if (!dugumPozisyonlari.containsKey(hedefDugum)) {
            ciktiAlani.setText("Seçilen düğüm şu an ekranda görünmüyor.\nLütfen ekrandaki düğümlerden birini seçin.");
            return;
        }

        try {
            ciktiAlani.appendText("Betweenness Centrality\n");
            ciktiAlani.appendText("Hesaplanıyor... (Ekrandaki tüm ikililer taranıyor)\n");

            List<MakaleDugumu> gorunurDugumler = new ArrayList<>(dugumPozisyonlari.keySet());

            int skor = BetweenHesaplayici.computeCentrality(gorunurDugumler, hedefDugum);

            ciktiAlani.appendText("Hedef Makale: " + hedefDugum.getMakale().getBaslik() + "\n");
            ciktiAlani.appendText("Analiz Edilen Düğüm Sayısı: " + gorunurDugumler.size() + "\n");
            ciktiAlani.appendText("----------------------------------\n");
            ciktiAlani.appendText("BETWEENNESS SKORU: " + skor + "\n");
            ciktiAlani.appendText("----------------------------------\n");
            ciktiAlani.appendText(skor + " tanesi bu makalenin üzerinden geçiyor.\n");

        } catch (Exception e) {
            ciktiAlani.setText("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onZoomInClick() {
        double mevcutOlcek = grafPaneli.getScaleX();
        if (mevcutOlcek < 5.0) {
            double yeniOlcek = mevcutOlcek * 1.1;
            grafPaneli.setScaleX(yeniOlcek);
            grafPaneli.setScaleY(yeniOlcek);
        }
    }

    @FXML
    protected void onZoomOutClick() {
        double mevcutOlcek = grafPaneli.getScaleX();
        if (mevcutOlcek > 0.1) {
            double yeniOlcek = mevcutOlcek / 1.1;
            grafPaneli.setScaleX(yeniOlcek);
            grafPaneli.setScaleY(yeniOlcek);
        }
    }

    @FXML
    protected void onResetZoomClick() {
        grafPaneli.setScaleX(1.0);
        grafPaneli.setScaleY(1.0);
    }

    private void kuvvetDuzeniniUygula() {
        int iterasyonlar = 50;
        double minMesafe = 70.0;
        double genislik = grafPaneli.getWidth() > 0 ? grafPaneli.getWidth() : 800;
        double yukseklik = grafPaneli.getHeight() > 0 ? grafPaneli.getHeight() : 600;
        double dolgu = 40.0;

        for (int i = 0; i < iterasyonlar; i++) {
            List<MakaleDugumu> dugumler = new ArrayList<>(dugumPozisyonlari.keySet());
            for (int a = 0; a < dugumler.size(); a++) {
                for (int b = a + 1; b < dugumler.size(); b++) {
                    MakaleDugumu n1 = dugumler.get(a);
                    MakaleDugumu n2 = dugumler.get(b);

                    Point2D p1 = dugumPozisyonlari.get(n1);
                    Point2D p2 = dugumPozisyonlari.get(n2);

                    double mesafe = p1.distance(p2);

                    if (mesafe < minMesafe) {
                        if (mesafe < 0.1) mesafe = 0.1;

                        double itmeKuvveti = (minMesafe - mesafe) / 2.0;

                        double dx = (p1.getX() - p2.getX()) / mesafe;
                        double dy = (p1.getY() - p2.getY()) / mesafe;

                        double yeniX1 = p1.getX() + dx * itmeKuvveti;
                        double yeniY1 = p1.getY() + dy * itmeKuvveti;

                        double yeniX2 = p2.getX() - dx * itmeKuvveti;
                        double yeniY2 = p2.getY() - dy * itmeKuvveti;

                        dugumPozisyonlari.put(n1, new Point2D(yeniX1, yeniY1));
                        dugumPozisyonlari.put(n2, new Point2D(yeniX2, yeniY2));
                    }
                }
            }

            for (MakaleDugumu n : dugumler) {
                Point2D p = dugumPozisyonlari.get(n);
                double x = Math.max(dolgu, Math.min(genislik - dolgu, p.getX()));
                double y = Math.max(dolgu, Math.min(yukseklik - dolgu, p.getY()));
                dugumPozisyonlari.put(n, new Point2D(x, y));
            }
        }
    }

    private void hataGoster(String mesaj) {
        hataEtiketi.setText(mesaj);
    }

    private void hataTemizle() {
        hataEtiketi.setText("");
    }

    private static String idNormalizeEt(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }
        id = id.trim();
        if (id.contains("/")) {
            return id.substring(id.lastIndexOf("/") + 1);
        }
        return id;
    }
}