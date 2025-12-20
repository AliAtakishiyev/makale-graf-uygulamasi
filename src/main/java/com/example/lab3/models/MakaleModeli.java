package com.example.lab3.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MakaleModeli {

    private final String id;
    private final String doi;
    private final String title;
    private final int year;
    private final List<String> authors;
    private final List<String> referencedWorks;

    public MakaleModeli(
            String id,
            String doi,
            String title,
            int year,
            List<String> authors,
            List<String> referencedWorks
    ) {
        this.id = Objects.requireNonNull(id, "id null olamaz");
        this.doi = doi;
        this.title = Objects.requireNonNull(title, "title null olamaz");
        this.year = year;
        this.authors = new ArrayList<>(authors != null ? authors : List.of());
        this.referencedWorks = new ArrayList<>(referencedWorks != null ? referencedWorks : List.of());
    }

    public String getId() {
        return id;
    }

    public String getDoi() {
        return doi;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    public List<String> getReferencedWorks() {
        return Collections.unmodifiableList(referencedWorks);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", year=" + year +
                ", title='" + title + '\'' +
                '}';
    }
}


