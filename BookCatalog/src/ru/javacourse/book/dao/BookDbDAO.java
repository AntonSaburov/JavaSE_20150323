package ru.javacourse.book.dao;

import ru.javacourse.book.domain.Book;
import ru.javacourse.book.exception.BookDaoException;
import ru.javacourse.book.filter.BookFilter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class BookDbDAO implements BookDAO
{
    private static final String SELECT_BOOK = "SELECT * FROM BOOK WHERE book_id = ?";
    private static final String SELECT_BOOKS = "SELECT * FROM BOOK ORDER BY title";
    private static final String INSERT_BOOK = "INSERT INTO BOOK (title, price, isbn) VALUES (?,?,?)";
    private static final String UPDATE_BOOK = "UPDATE BOOK SET title = ?, price = ?, isbn = ? WHERE book_id = ?";
    private static final String DELETE_BOOK = "DELETE FROM BOOK WHERE book_id = ?";

//    static {
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch(Exception ex) {
//        }
//    }

    private Connection getConnection() throws SQLException {
//        String url = "jdbc:postgresql://localhost:5432/javacourse";
//        String login = "postgres";
//        String password = "postgres";
//        Connection con = DriverManager.getConnection(url, login, password);
//        return con;
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/bookDS");
            Connection con = dataSource.getConnection();
            return con;
        } catch (NamingException e) {
            throw new SQLException(e);
        }

    }

    @Override
    public Long addBook(Book book) throws BookDaoException {
        Long bookId = 0L;
        try {
            Connection con = getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(INSERT_BOOK, new String[] {"book_id"});
                stmt.setString(1, book.getTitle());
                stmt.setDouble(2, book.getPrice());
                stmt.setString(3, book.getIsbn());
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                if(rs.next()) {
                    bookId = rs.getLong("book_id");
                }
            } finally {
                closeRsSt(rs, stmt);
                con.close();
            }
        } catch(SQLException ex) {
            throw new BookDaoException(ex);
        }
        return bookId;
    }

    @Override
    public void updateBook(Book book) throws BookDaoException {
        try {
            Connection con = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(UPDATE_BOOK);
                stmt.setString(1, book.getTitle());
                stmt.setDouble(2, book.getPrice());
                stmt.setString(3, book.getIsbn());
                stmt.setLong(4, book.getBookId());
                stmt.executeUpdate();
            } finally {
                closeRsSt(null, stmt);
                con.close();
            }
        } catch(SQLException ex) {
            throw new BookDaoException(ex);
        }
    }

    @Override
    public void deleteBook(Long bookId) throws BookDaoException {
        try {
            Connection con = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(DELETE_BOOK);
                stmt.setLong(1, bookId);
                stmt.executeUpdate();
            } finally {
                closeRsSt(null, stmt);
                con.close();
            }
        } catch(SQLException ex) {
            throw new BookDaoException(ex);
        }
    }

    @Override
    public Book getBook(Long bookId) throws BookDaoException {
        Book book = null;
        try {
            Connection con = getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(SELECT_BOOK);
                stmt.setLong(1, bookId);
                rs = stmt.executeQuery();
                if(rs.next()) {
                    book = new Book(rs.getLong("book_id"), rs.getString("title"), rs.getDouble("price"), rs.getString("isbn"));
                }
            } finally {
                closeRsSt(rs, stmt);
                con.close();
            }
        } catch(SQLException ex) {
            throw new BookDaoException(ex);
        }
        return book;
    }

    @Override
    public List<Book> findBooks(BookFilter filter) throws BookDaoException {
        List<Book> books = new LinkedList<Book>();
        try {
            Connection con = getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(SELECT_BOOKS);
                rs = stmt.executeQuery();
                while(rs.next()) {
                    Book book = new Book(rs.getLong("book_id"), rs.getString("title"), rs.getDouble("price"), rs.getString("isbn"));
                    books.add(book);
                }
            } finally {
                closeRsSt(rs, stmt);
                con.close();
            }
        } catch(SQLException ex) {
            throw new BookDaoException(ex);
        }
        return books;
    }

    private void closeRsSt(ResultSet rs, Statement st) throws SQLException {
        if(rs != null) {
            rs.close();
        }
        if(st != null) {
            st.close();
        }
    }
}
