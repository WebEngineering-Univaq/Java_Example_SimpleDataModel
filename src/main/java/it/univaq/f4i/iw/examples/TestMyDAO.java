/*
 * TestMyDAO.java
 *
 * Questo esempio mostra come creare totalmente "a mano" un data model e i suoi DAO
 * 
 * This example shows how to use manually-craft a data model and its DAOs
 *
 */
package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.NewspaperDataLayer;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class TestMyDAO extends HttpServlet {

    private DataSource ds;

    private void action_manipulate(HttpServletRequest request, HttpServletResponse response) throws IOException, DataException {

        //preleviamo il data layer 
        //get the data layer
        NewspaperDataLayer dl = (NewspaperDataLayer) request.getAttribute("datalayer");

        //manipoliamo i dati usando le interfacce esposta dai DAO accessibili dal data layer
        //manipulate the data using the interfaces exposed by the DAOs accessible from the data layer
        int latest_number = dl.getIssueDAO().getLatestIssue().getNumber();
        //        
        Issue new_issue = dl.getIssueDAO().createIssue();
        new_issue.setNumber((latest_number + 1));
        new_issue.setDate(LocalDate.now());
        dl.getIssueDAO().storeIssue(new_issue);
        //        
        Article new_article = dl.getArticleDAO().createArticle();
        Author author = dl.getAuthorDAO().getAuthor(1); //assume it already exists
        if (author != null) {
            new_article.setAuthor(author);
            new_article.setTitle(SecurityHelpers.addSlashes("NEW ARTICLE FOR ISSUE" + (latest_number + 1)));
            new_article.setText(SecurityHelpers.addSlashes("article text"));
            new_article.setIssue(new_issue);
            dl.getArticleDAO().storeArticle(new_article);
            //
            Issue latest_issue = dl.getIssueDAO().getLatestIssue();
            List<Article> articles = latest_issue.getArticles();

            //mandiamo in output quanche informazione prelavata dal data model
            //output some information from the data model
            HTMLResult result = new HTMLResult(getServletContext());
            result.setTitle("Data Model Manipulation");
            result.appendToBody("<h1>Data Model Manipulation</h1>");
            result.appendToBody("<p>Original latest issue was #" + latest_number + "</p>");
            result.appendToBody("<p>Adding new issue #" + (latest_number + 1) + "</p>");
            result.appendToBody("<p>Adding article \""+HTMLResult.sanitizeHTMLOutput(new_article.getTitle())+"\" on the new issue</p>");
            result.appendToBody("<p>Fetching new latest issue:</p>");
            result.appendToBody("<p>Number: " + latest_issue.getNumber() + ", Date: " + latest_issue.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "</p>");
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
     * @throws jakarta.servlet.ServletException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            //creiamo LOCALMENTE il data layer (da cui si accede ai DAO) e ci assicuriamo che venga chiuso alla fine della richiesta
            //LOCALLY create the data layer (from which DAOs are accessed) and make sure it is closed at the end of the request
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
     * @throws jakarta.servlet.ServletException
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
        //init data source (thread safe)
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/" + config.getServletContext().getInitParameter("data.source"));
        } catch (NamingException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     * @return 
     */
    @Override
    public String getServletInfo() {
        return "A Manually-Crafted Data Model";
    }

}
