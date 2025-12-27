package com.example.lab3.jsonreader;

import com.example.lab3.models.MakaleModeli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonOkuyucu {

    private final String text;
    private int index;

    private JsonOkuyucu(String text) {
        this.text = text;
        this.index = 0;
    }

    public static List<MakaleModeli> readArticles(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        JsonOkuyucu reader = new JsonOkuyucu(content);
        return reader.parseArticleArray();
    }

    private List<MakaleModeli> parseArticleArray() {
        skipWhitespace();
        expect('[');
        skipWhitespace();

        List<MakaleModeli> result = new ArrayList<>();

        while (true) {
            skipWhitespace();
            if (peek() == ']') {
                consume(); // ]
                break;
            }

            MakaleModeli a = parseArticleObject();
            result.add(a);

            skipWhitespace();
            char c = peek();
            if (c == ',') {
                consume();
                continue;
            } else if (c == ']') {
                consume();
                break;
            } else {
                throw error("Makale listesi parse edilirken beklenmeyen karakter: " + c);
            }
        }

        return result;
    }

    private MakaleModeli parseArticleObject() {
        skipWhitespace();
        expect('{');

        String id = null;
        String doi = null;
        String title = null;
        int year = 0;
        List<String> authors = new ArrayList<>();
        List<String> referencedWorks = new ArrayList<>();

        while (true) {
            skipWhitespace();
            char c = peek();
            if (c == '}') {
                consume();
                break;
            }

            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();

            switch (key) {
                case "id" -> id = normalizeId(parseString());
                case "doi" -> {
                    char valueStart = peek();
                    if (valueStart == 'n') { // null
                        skipValue();
                    } else {
                        doi = parseString();
                    }
                }
                case "title" -> title = parseString();
                case "year" -> year = parseIntNumber();
                case "authors" -> authors = parseStringArray();
                case "referenced_works" -> {
                    List<String> refs = parseStringArray();
                    for (int i = 0; i < refs.size(); i++) {
                        refs.set(i, normalizeId(refs.get(i)));
                    }
                    referencedWorks = refs;
                }
                default -> skipValue();
            }

            skipWhitespace();
            c = peek();
            if (c == ',') {
                consume();
                continue;
            } else if (c == '}') {
                consume();
                break;
            } else {
                throw error("Nesne içinde beklenmeyen karakter: " + c);
            }
        }

        if (id == null || title == null) {
            throw error("Makale nesnesinde zorunlu alan eksik: id veya title null.");
        }

        return new MakaleModeli(id, doi, title, year, authors, referencedWorks);
    }


    private List<String> parseStringArray() {
        skipWhitespace();
        expect('[');
        skipWhitespace();

        List<String> list = new ArrayList<>();

        while (true) {
            skipWhitespace();
            char c = peek();
            if (c == ']') {
                consume();
                break;
            }

            String value = parseString();
            list.add(value);

            skipWhitespace();
            c = peek();
            if (c == ',') {
                consume();
            } else if (c == ']') {
                consume();
                break;
            } else {
                throw error("Dizi içinde beklenmeyen karakter: " + c);
            }
        }

        return list;
    }

    private String parseString() {
        skipWhitespace();
        expect('"');
        StringBuilder sb = new StringBuilder();

        while (true) {
            if (isAtEnd()) {
                throw error("String beklenirken dosya sonu geldi.");
            }
            char c = consume();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                if (isAtEnd()) {
                    throw error("Eksik kaçış dizisi.");
                }
                char esc = consume();
                switch (esc) {
                    case '"', '\\', '/' -> sb.append(esc);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        consume();
                        consume();
                        consume();
                        consume();
                    }
                    default -> throw error("Bilinmeyen kaçış dizisi: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private int parseIntNumber() {
        skipWhitespace();
        StringBuilder sb = new StringBuilder();

        char c = peek();
        if (c == '-') {
            sb.append(consume());
            c = peek();
        }

        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(consume());
        }

        if (sb.isEmpty()) {
            throw error("Sayı bekleniyordu ancak bulunamadı.");
        }

        return Integer.parseInt(sb.toString());
    }

    private void skipValue() {
        skipWhitespace();
        char c = peek();
        switch (c) {
            case '{' -> skipObject();
            case '[' -> skipArray();
            case '"' -> {
                parseString();
            }
            default -> {
                while (!isAtEnd()) {
                    c = peek();
                    if (c == ',' || c == '}' || c == ']') {
                        break;
                    }
                    consume();
                }
            }
        }
    }

    private void skipObject() {
        int depth = 0;
        do {
            char c = consume();
            if (c == '{') depth++;
            else if (c == '}') depth--;
        } while (depth > 0 && !isAtEnd());
    }

    private void skipArray() {
        int depth = 0;
        do {
            char c = consume();
            if (c == '[') depth++;
            else if (c == ']') depth--;
        } while (depth > 0 && !isAtEnd());
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                consume();
            } else {
                break;
            }
        }
    }

    private void expect(char expected) {
        char c = peek();
        if (c != expected) {
            throw error("Beklenen karakter '" + expected + "', fakat '" + c + "' bulundu.");
        }
        consume();
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return text.charAt(index);
    }

    private char consume() {
        if (isAtEnd()) {
            return '\0';
        }
        return text.charAt(index++);
    }

    private boolean isAtEnd() {
        return index >= text.length();
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException(message + " (index=" + index + ")");
    }

    /**
     * OpenAlex ID'sini normalize eder.
     * Tam link (https://openalex.org/W123456) formatından sadece ID kısmını (W123456) çıkarır.
     * Zaten normalize edilmiş ID ise olduğu gibi döndürür.
     */
    private static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }
        id = id.trim();
        // Eğer https://openalex.org/ ile başlıyorsa, son kısmını al
        if (id.contains("/")) {
            return id.substring(id.lastIndexOf("/") + 1);
        }
        return id;
    }
}


