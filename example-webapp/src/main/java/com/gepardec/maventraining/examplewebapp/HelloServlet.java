package com.gepardec.maventraining.examplewebapp;

import java.io.*;

import com.gepardec.maventraining.MessageService;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String title;
    private String description;

    private MessageService messageService;

    public void init() {
        messageService = new MessageService();
        title = messageService.getApplicationTitle();
        description = messageService.getApplicationDescription();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + title + "</h1>");
        out.println("<h3>" + description + "</h3>");
        out.println("</body></html>");
    }

    public void destroy() {
    }
}