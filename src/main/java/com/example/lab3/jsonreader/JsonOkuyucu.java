package com.example.lab3.jsonreader;

import com.example.lab3.models.MakaleModeli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonOkuyucu {

    private final String metin;
    private int indeks;

    private JsonOkuyucu(String metin) {
        this.metin = metin;
        this.indeks = 0;
    }

    public static List<MakaleModeli> readArticles(Path yol) throws IOException {
        String icerik = Files.readString(yol, StandardCharsets.UTF_8);
        JsonOkuyucu okuyucu = new JsonOkuyucu(icerik);
        return okuyucu.makaleDizisiniAyristir();
    }

    private List<MakaleModeli> makaleDizisiniAyristir() {
        bosluklariAtla();
        bekle('[');
        bosluklariAtla();

        List<MakaleModeli> sonuc = new ArrayList<>();

        while (true) {
            bosluklariAtla();
            if (gozat() == ']') {
                tuket(); // ]
                break;
            }

            MakaleModeli a = makaleNesnesiniAyristir();
            sonuc.add(a);

            bosluklariAtla();
            char c = gozat();
            if (c == ',') {
                tuket();
                continue;
            } else if (c == ']') {
                tuket();
                break;
            } else {
                throw hata("Makale listesi parse edilirken beklenmeyen karakter: " + c);
            }
        }

        return sonuc;
    }

    private MakaleModeli makaleNesnesiniAyristir() {
        bosluklariAtla();
        bekle('{');

        String id = null;
        String doi = null;
        String baslik = null;
        int yil = 0;
        List<String> yazarlar = new ArrayList<>();
        List<String> referanslar = new ArrayList<>();

        while (true) {
            bosluklariAtla();
            char c = gozat();
            if (c == '}') {
                tuket();
                break;
            }

            String anahtar = stringAyristir();
            bosluklariAtla();
            bekle(':');
            bosluklariAtla();

            switch (anahtar) {
                case "id" -> id = idNormalizeEt(stringAyristir());
                case "doi" -> {
                    char degerBaslangici = gozat();
                    if (degerBaslangici == 'n') { // null
                        degeriAtla();
                    } else {
                        doi = stringAyristir();
                    }
                }
                case "title" -> baslik = stringAyristir();
                case "year" -> yil = tamSayiAyristir();
                case "authors" -> yazarlar = stringDizisiAyristir();
                case "referenced_works" -> {
                    List<String> refs = stringDizisiAyristir();
                    for (int i = 0; i < refs.size(); i++) {
                        refs.set(i, idNormalizeEt(refs.get(i)));
                    }
                    referanslar = refs;
                }
                default -> degeriAtla();
            }

            bosluklariAtla();
            c = gozat();
            if (c == ',') {
                tuket();
                continue;
            } else if (c == '}') {
                tuket();
                break;
            } else {
                throw hata("Nesne içinde beklenmeyen karakter: " + c);
            }
        }

        if (id == null || baslik == null) {
            throw hata("Makale nesnesinde zorunlu alan eksik: id veya title null.");
        }

        return new MakaleModeli(id, doi, baslik, yil, yazarlar, referanslar);
    }

    private List<String> stringDizisiAyristir() {
        bosluklariAtla();
        bekle('[');
        bosluklariAtla();

        List<String> liste = new ArrayList<>();

        while (true) {
            bosluklariAtla();
            char c = gozat();
            if (c == ']') {
                tuket();
                break;
            }

            String deger = stringAyristir();
            liste.add(deger);

            bosluklariAtla();
            c = gozat();
            if (c == ',') {
                tuket();
            } else if (c == ']') {
                tuket();
                break;
            } else {
                throw hata("Dizi içinde beklenmeyen karakter: " + c);
            }
        }

        return liste;
    }

    private String stringAyristir() {
        bosluklariAtla();
        bekle('"');
        StringBuilder sb = new StringBuilder();

        while (true) {
            if (sonaGelindiMi()) {
                throw hata("String beklenirken dosya sonu geldi.");
            }
            char c = tuket();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                if (sonaGelindiMi()) {
                    throw hata("Eksik kaçış dizisi.");
                }
                char kacis = tuket();
                switch (kacis) {
                    case '"', '\\', '/' -> sb.append(kacis);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        tuket(); tuket(); tuket(); tuket();
                    }
                    default -> throw hata("Bilinmeyen kaçış dizisi: \\" + kacis);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private int tamSayiAyristir() {
        bosluklariAtla();
        StringBuilder sb = new StringBuilder();

        char c = gozat();
        if (c == '-') {
            sb.append(tuket());
            c = gozat();
        }

        while (!sonaGelindiMi() && Character.isDigit(gozat())) {
            sb.append(tuket());
        }

        if (sb.isEmpty()) {
            throw hata("Sayı bekleniyordu ancak bulunamadı.");
        }

        return Integer.parseInt(sb.toString());
    }

    private void degeriAtla() {
        bosluklariAtla();
        char c = gozat();
        switch (c) {
            case '{' -> nesneyiAtla();
            case '[' -> diziyiAtla();
            case '"' -> stringAyristir();
            default -> {
                while (!sonaGelindiMi()) {
                    c = gozat();
                    if (c == ',' || c == '}' || c == ']') {
                        break;
                    }
                    tuket();
                }
            }
        }
    }

    private void nesneyiAtla() {
        int derinlik = 0;
        do {
            char c = tuket();
            if (c == '{') derinlik++;
            else if (c == '}') derinlik--;
        } while (derinlik > 0 && !sonaGelindiMi());
    }

    private void diziyiAtla() {
        int derinlik = 0;
        do {
            char c = tuket();
            if (c == '[') derinlik++;
            else if (c == ']') derinlik--;
        } while (derinlik > 0 && !sonaGelindiMi());
    }

    private void bosluklariAtla() {
        while (!sonaGelindiMi()) {
            char c = gozat();
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                tuket();
            } else {
                break;
            }
        }
    }

    private void bekle(char beklenen) {
        char c = gozat();
        if (c != beklenen) {
            throw hata("Beklenen karakter '" + beklenen + "', fakat '" + c + "' bulundu.");
        }
        tuket();
    }

    private char gozat() {
        if (sonaGelindiMi()) return '\0';
        return metin.charAt(indeks);
    }

    private char tuket() {
        if (sonaGelindiMi()) return '\0';
        return metin.charAt(indeks++);
    }

    private boolean sonaGelindiMi() {
        return indeks >= metin.length();
    }

    private IllegalStateException hata(String mesaj) {
        return new IllegalStateException(mesaj + " (index=" + indeks + ")");
    }

    private static String idNormalizeEt(String id) {
        if (id == null || id.isBlank()) return id;
        id = id.trim();
        if (id.contains("/")) {
            return id.substring(id.lastIndexOf("/") + 1);
        }
        return id;
    }
}