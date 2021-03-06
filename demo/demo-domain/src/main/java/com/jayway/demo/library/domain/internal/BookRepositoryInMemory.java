/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.demo.library.domain.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.jayway.demo.library.domain.Book;
import com.jayway.demo.library.domain.BookRepository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookRepositoryInMemory implements BookRepository {

    private final Map<Integer, Book> allBooks = new HashMap<Integer, Book>();

    private final AtomicInteger nextId = new AtomicInteger();

    @Override
    public Book newBook(String author, String title) {
        int id = nextId.getAndIncrement();
        Book book = new Book(id, author, title);
        allBooks.put(id, book);
        return book;
    }

    @Override
    public Collection<Book> getLoansForCustomer(final Integer customerId) {
        return Collections2.filter(allBooks.values(), new Predicate<Book>() {
            @Override
            public boolean apply(Book book) {
                if (!book.isBorrowed()) {
                    return false;
                }
                return book.getBorrowedBy().getId().equals(customerId);
            }
        });
    }

    @Override
    public Collection<Book> getLoans() {
        return Collections2.filter(allBooks.values(), new Predicate<Book>() {
            @Override
            public boolean apply(Book book) {
                if (!book.isBorrowed()) {
                    return false;
                } else {
                    return true;
                }
            }
        });
    }

    @PostConstruct
    public void init() {
        newBook("J.R.R. Tolkien", "Lord of the Rings");
        newBook("Cormac McCarthy", "The Road");
        newBook("George R.R. Martin", "The Game of Thrones");
        newBook("George R.R. Martin", "A Clash of Kings");
    }

    @Override
    public List<Book> getAllBooks() {
        return new ArrayList<Book>(allBooks.values());
    }

    @Override
    public Book getBookById(Integer id) {
        return allBooks.get(id);
    }

}
