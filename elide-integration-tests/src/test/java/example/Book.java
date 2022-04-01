/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.ComputedRelationship;
import com.yahoo.elide.annotation.FilterExpressionPath;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.Paginate;
import com.yahoo.elide.annotation.ReadPermission;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Model for books.
 */
@Entity
@Table(name = "book")
@Include
@Paginate
@Audit(action = Audit.Action.CREATE,
        operation = 10,
        logStatement = "{0}",
        logExpressions = {"${book.title}"})
public class Book extends BaseId {
    private String title;
    private String genre;
    private String language;
    private Date publishDate = new Date(0);
    private Collection<Author> authors = new ArrayList<>();
    private Collection<Chapter> chapters = new ArrayList<>();
    private String editorName;
    private Publisher publisher;
    private Collection<String> awards = new ArrayList<>();
    private Price price;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setPublishDate(final Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getPublishDate() {
        return this.publishDate;
    }

    @ElementCollection(targetClass = String.class)
    public Collection<String> getAwards() {
        return awards;
    }

    public void setAwards(Collection<String> awards) {
        this.awards = awards;
    }

    @Embedded
    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    /**
     * Demonstrates a more complex ranking use case.
     * @return The number of chapters in a book.
     */
    @Formula(value = "(SELECT COUNT(*) FROM book AS b JOIN book_chapter AS bc ON bc.book_id = b.id WHERE id=b.id)")
    public int getChapterCount() {
        return chapters.size();
    }

    public void setChapterCount(int unused) {
        //NOOP
        return;
    }

    @OneToMany
    @JoinTable(
            name = "book_chapter",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "chapters_id")
    )
    public Collection<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(Collection<Chapter> chapters) {
        this.chapters = chapters;
    }

    @ManyToMany
    public Collection<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Collection<Author> authors) {
        this.authors = authors;
    }

    // Case sensitive collation for H2
    @Column(columnDefinition = "VARCHAR_CASESENSITIVE(255) DEFAULT NULL")
    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    @ManyToOne
    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Transient
    @ComputedRelationship
    @OneToOne
    @FilterExpressionPath("publisher.editor")
    @ReadPermission(expression = "Field path editor check")
    public Editor getEditor() {
        if (publisher != null) {
            return getPublisher().getEditor();
        }

        return null;
    }
    @Override
    public String toString() {
        return "Book: " + id;
    }
}
