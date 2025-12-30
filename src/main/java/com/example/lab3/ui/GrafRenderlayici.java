package com.example.lab3.ui;

import com.example.lab3.models.MakaleDugumu;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GrafRenderlayici {

    private static final double DUGUM_YARICAPI = 25.0;
    private static final double OK_BOYUTU = 6.0;

    public static void drawInteractiveGraph(Pane panel, Map<MakaleDugumu, Point2D> dugumPozisyonlari, Consumer<MakaleDugumu> dugumTiklamaOlayi) {
        panel.getChildren().clear();

        if (dugumPozisyonlari == null || dugumPozisyonlari.isEmpty()) {
            return;
        }

        Group grup = new Group();

        for (Map.Entry<MakaleDugumu, Point2D> kayit : dugumPozisyonlari.entrySet()) {
            MakaleDugumu kaynak = kayit.getKey();
            Point2D p1 = kayit.getValue();

            for (MakaleDugumu hedef : kaynak.getGidenler()) {
                if (dugumPozisyonlari.containsKey(hedef)) {
                    Point2D p2 = dugumPozisyonlari.get(hedef);
                    okCiz(grup, p1, p2, Color.GRAY, 1.0);
                }
            }
        }

        for (Map.Entry<MakaleDugumu, Point2D> kayit : dugumPozisyonlari.entrySet()) {
            MakaleDugumu dugum = kayit.getKey();
            Point2D poz = kayit.getValue();
            dugumCiz(grup, poz.getX(), poz.getY(), dugum, Color.CORNFLOWERBLUE, dugumTiklamaOlayi);
        }

        panel.getChildren().add(grup);
    }

    public static void drawInteractiveGraphWithHighlight(Pane panel, Map<MakaleDugumu, Point2D> dugumPozisyonlari,
                                                         List<MakaleDugumu> vurguListesi,
                                                         Consumer<MakaleDugumu> dugumTiklamaOlayi) {
        panel.getChildren().clear();
        if (dugumPozisyonlari == null || dugumPozisyonlari.isEmpty()) return;

        Group grup = new Group();

        for (Map.Entry<MakaleDugumu, Point2D> kayit : dugumPozisyonlari.entrySet()) {
            MakaleDugumu kaynak = kayit.getKey();
            Point2D p1 = kayit.getValue();

            for (MakaleDugumu hedef : kaynak.getGidenler()) {
                if (dugumPozisyonlari.containsKey(hedef)) {
                    Point2D p2 = dugumPozisyonlari.get(hedef);

                    boolean vurgulu = vurguListesi.contains(kaynak) && vurguListesi.contains(hedef);
                    if (vurgulu) {
                        okCiz(grup, p1, p2, Color.RED, 3.0);
                    } else {
                        okCiz(grup, p1, p2, Color.LIGHTGRAY, 1.0);
                    }
                }
            }
        }

        for (Map.Entry<MakaleDugumu, Point2D> kayit : dugumPozisyonlari.entrySet()) {
            MakaleDugumu dugum = kayit.getKey();
            Point2D poz = kayit.getValue();
            Color renk = vurguListesi.contains(dugum) ? Color.SALMON : Color.LIGHTGRAY;
            dugumCiz(grup, poz.getX(), poz.getY(), dugum, renk, dugumTiklamaOlayi);
        }
        panel.getChildren().add(grup);
    }

    public static void drawGreenLineChain(Pane panel, List<MakaleDugumu> siraliDugumler, Consumer<MakaleDugumu> dugumTiklamaOlayi) {
        panel.getChildren().clear();
        if (siraliDugumler == null || siraliDugumler.isEmpty()) return;

        double genislik = panel.getWidth() > 0 ? panel.getWidth() : 800;
        Group grup = new Group();

        double baslangicX = 80, baslangicY = 80, boslukX = 120, boslukY = 150;
        int sutunlar = (int) ((genislik - 100) / boslukX);
        if (sutunlar < 2) sutunlar = 2;

        Point2D[] pozisyonlar = new Point2D[siraliDugumler.size()];
        for (int i = 0; i < siraliDugumler.size(); i++) {
            int satir = i / sutunlar;
            int sutun = i % sutunlar;
            if (satir % 2 == 1) sutun = (sutunlar - 1) - sutun;

            double x = baslangicX + sutun * boslukX;
            double y = baslangicY + satir * boslukY;
            pozisyonlar[i] = new Point2D(x, y);
        }

        for (int i = 0; i < siraliDugumler.size() - 1; i++) {
            Point2D p1 = pozisyonlar[i];
            Point2D p2 = pozisyonlar[i+1];

            Line cizgi = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            cizgi.setStroke(Color.GREEN);
            cizgi.setStrokeWidth(2.5);
            cizgi.getStrokeDashArray().addAll(10d, 5d);
            grup.getChildren().add(cizgi);
        }

        for (int i = 0; i < siraliDugumler.size(); i++) {
            dugumCiz(grup, pozisyonlar[i].getX(), pozisyonlar[i].getY(), siraliDugumler.get(i), Color.WHITE, dugumTiklamaOlayi);
            Text t = new Text(pozisyonlar[i].getX() - 5, pozisyonlar[i].getY() + DUGUM_YARICAPI + 15, "#" + (i+1));
            t.setFont(Font.font("Arial", 10));
            grup.getChildren().add(t);
        }

        panel.getChildren().add(grup);
        int toplamSatir = (int) Math.ceil((double) siraliDugumler.size() / sutunlar);
        panel.setPrefHeight(Math.max(600, baslangicY + toplamSatir * boslukY + 100));
    }

    private static void okCiz(Group grup, Point2D p1, Point2D p2, Color renk, double kalinlik) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double aci = Math.atan2(dy, dx);

        double hedefX = p2.getX() - DUGUM_YARICAPI * Math.cos(aci);
        double hedefY = p2.getY() - DUGUM_YARICAPI * Math.sin(aci);

        double baslangicX = p1.getX() + DUGUM_YARICAPI * Math.cos(aci);
        double baslangicY = p1.getY() + DUGUM_YARICAPI * Math.sin(aci);

        Line cizgi = new Line(baslangicX, baslangicY, hedefX, hedefY);
        cizgi.setStroke(renk);
        cizgi.setStrokeWidth(kalinlik);

        double okUzunlugu = 10.0 + kalinlik;
        double okKanadi = Math.PI / 6.0;

        double x1 = hedefX - okUzunlugu * Math.cos(aci - okKanadi);
        double y1 = hedefY - okUzunlugu * Math.sin(aci - okKanadi);
        double x2 = hedefX - okUzunlugu * Math.cos(aci + okKanadi);
        double y2 = hedefY - okUzunlugu * Math.sin(aci + okKanadi);

        Polygon okBasi = new Polygon();
        okBasi.getPoints().addAll(
                hedefX, hedefY,
                x1, y1,
                x2, y2
        );
        okBasi.setFill(renk);
        okBasi.setStroke(renk);
        okBasi.setStrokeWidth(1.0);

        grup.getChildren().addAll(cizgi, okBasi);
    }

    private static void dugumCiz(Group grup, double x, double y, MakaleDugumu dugum, Color dolgu, Consumer<MakaleDugumu> tiklama) {
        String atifSayisi = String.valueOf(dugum.getGirisDerecesi());
        String hamId = dugum.getMakale().getId();
        String gorunenId = hamId.contains("/") ? hamId.substring(hamId.lastIndexOf("/") + 1) : hamId;
        if (gorunenId.length() > 8) gorunenId = gorunenId.substring(0, 8) + "..";

        StringBuilder sb = new StringBuilder();
        List<String> yazarlar = dugum.getMakale().getYazarlar();
        if (yazarlar != null) {
            for (String yazar : yazarlar) {
                if (yazar != null && !yazar.isBlank()) {
                    sb.append(yazar.trim().substring(0, 1).toUpperCase());
                }
            }
        }
        String yazarBasHarfleri = sb.length() > 0 ? sb.toString() : "-";
        if (yazarBasHarfleri.length() > 5) yazarBasHarfleri = yazarBasHarfleri.substring(0, 5) + "..";

        String tumYazarlar = (yazarlar != null && !yazarlar.isEmpty()) ? String.join(", ", yazarlar) : "Bilinmiyor";

        StackPane yigin = new StackPane();
        yigin.setLayoutX(x - DUGUM_YARICAPI);
        yigin.setLayoutY(y - DUGUM_YARICAPI);

        Circle daire = new Circle(DUGUM_YARICAPI);
        daire.setFill(dolgu);
        daire.setStroke(Color.DARKSLATEGRAY);
        daire.setStrokeWidth(1.5);

        yigin.setOnMouseEntered(e -> daire.setStroke(Color.RED));
        yigin.setOnMouseExited(e -> daire.setStroke(Color.DARKSLATEGRAY));

        if (tiklama != null) {
            yigin.setOnMouseClicked(e -> {
                tiklama.accept(dugum);
                e.consume();
            });
        }

        VBox metinKutusu = new VBox(0);
        metinKutusu.setAlignment(Pos.CENTER);

        Text yaziSayi = new Text(atifSayisi);
        yaziSayi.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Text yaziId = new Text(gorunenId);
        yaziId.setFont(Font.font("Arial", 9));

        Text yaziYazar = new Text(yazarBasHarfleri);
        yaziYazar.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        metinKutusu.getChildren().addAll(yaziSayi, yaziId, yaziYazar);
        yigin.getChildren().addAll(daire, metinKutusu);

        Tooltip ipucu = new Tooltip(
                "Başlık: " + dugum.getMakale().getBaslik() + "\n" +
                        "Yazarlar: " + tumYazarlar + "\n" +
                        "ID: " + hamId + "\n" +
                        "Yıl: " + dugum.getMakale().getYil() + "\n" +
                        "Atıf (In): " + dugum.getGirisDerecesi() + "\n" +
                        "Ref (Out): " + dugum.getCikisDerecesi()
        );
        ipucu.setShowDelay(Duration.millis(100));
        Tooltip.install(yigin, ipucu);

        grup.getChildren().add(yigin);
    }
}