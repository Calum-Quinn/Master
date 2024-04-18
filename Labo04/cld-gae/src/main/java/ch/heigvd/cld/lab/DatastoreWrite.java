package ch.heigvd.cld.lab;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Objects;

@WebServlet(name = "DatastoreWrite", value = "/datastorewrite")
public class DatastoreWrite extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        pw.println("Writing entity to datastore.");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        final String kindConst = "_kind";
        final String keyConst = "_key";
        String kind = req.getParameter(kindConst);
        String key = req.getParameter(keyConst);

        Entity element;

        if(key != null) {
            element = new Entity(kind,key);
        }
        else {
            element = new Entity(kind);
        }

        for (Enumeration parameterNames = req.getParameterNames(); parameterNames.hasMoreElements();) {
            String nextElement = parameterNames.nextElement().toString();
            if (Objects.equals(nextElement, "_kind") || Objects.equals(nextElement, "_key")) {
                continue;
            }
            element.setProperty(nextElement, req.getParameter(nextElement));
        }
        datastore.put(element);
    }
}