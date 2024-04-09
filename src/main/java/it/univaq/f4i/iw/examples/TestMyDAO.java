/*
 * TestMyDAO.java
 *
 * Questo esempio mostra come creare totalmente "a mano" un data model e i suoi DAO
 * 
 * This example shows how to use manually-craft a data model and its DAOs
 *
 */
package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.ex.newspaper.data.dao.NewspaperDataLayer;
import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.result.HTMLResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class TestMyDAO extends HttpServlet {

    private DataSource ds;

    private void action_manipulate(HttpServletRequest request, HttpServletResponse response) throws IOException, DataException {

        NewspaperDataLayer dl = (NewspaperDataLayer) request.getAttribute("datalayer");

        HTMLResult result = new HTMLResult(getServletContext());

        result.setTitle("Data Model Manipulation");
        result.appendToBody("<h1>Data Model Manipulation</h1>");

        int latest_number = dl.getIssueDAO().getLatestIssue().getNumber();
        result.appendToBody("<p>Latest issue is #" + latest_number + "</p>");

        result.appendToBody("<p>Adding issue " + (latest_number + 1) + "...</p>");
        Issue issue = dl.getIssueDAO().createIssue();
        issue.setNumber((latest_number + 1));
        issue.setDate(LocalDate.now());
        dl.getIssueDAO().storeIssue(issue);

        result.appendToBody("<p>Adding article on new issue...</p>");
        Article article = dl.getArticleDAO().createArticle();
        Author author = dl.getAuthorDAO().getAuthor(1); //assumiamo che esista già

        if (author != null) {
            article.setAuthor(author);
            article.setTitle(SecurityHelpers.addSlashes("NEW ARTICLE FOR ISSUE" + (latest_number + 1)));
            article.setText(SecurityHelpers.addSlashes("article text"));
            article.setIssue(issue);
            dl.getArticleDAO().storeArticle(article);
            result.appendToBody("<p>Fetching new latest issue...</p>");
            issue = dl.getIssueDAO().getLatestIssue();
            result.appendToBody("<p>Number: " + issue.getNumber() + ", Date: " + issue.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "</p>");
            List<Article> articles = issue.getArticles();
            if (!articles.isEmpty()) {
                result.appendToBody("<ul>");
                for (Article a : articles) {
                    result.appendToBody("<li>" + HTMLResult.sanitizeHTMLOutput(a.getTitle()) + "</li>");
                }
                result.appendToBody("</ul>");
            }
            result.activate(request, response);
        } else {
            ServletHelpers.handleError("Cannot add article: undefined author", request, response, getServletContext());
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            try (DataLayer dl = new NewspaperDataLayer(ds)) {
                dl.init();
                request.setAttribute("datalayer", dl);
                action_manipulate(request, response);
            }
        } catch (Exception ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //init data source
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/" + config.getServletContext().getInitParameter("data.source"));
        } catch (NamingException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "A Manually-Crafted Data Model";
    }

}
