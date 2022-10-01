package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckSignup")
public class CheckSignup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// obtain and escape parameters
		String mail = null;
		String psw = null;
		String repeatpsw = null;
		String username = null;
		String name = null;
		String surname = null;
		String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

		try {
			mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
			psw = StringEscapeUtils.escapeJava(request.getParameter("psw"));
			repeatpsw = StringEscapeUtils.escapeJava(request.getParameter("repeatpsw"));
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));

			if (mail == null || psw == null || repeatpsw == null || username == null || name == null || surname == null
					|| mail.isEmpty() || psw.isEmpty() || repeatpsw.isEmpty() || username.isEmpty() || name.isEmpty()
					|| surname.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}

		// checking for username already taken
		UserDAO userDao = new UserDAO(connection);
		boolean credentialCheck;
		try {
			credentialCheck = userDao.checkSignupCredentials(username);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
			return;
		}

		// email validation

		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(mail);

		String errorMsg = "";
		String path;
		if (!matcher.find()) {
			errorMsg = errorMsg + " - invalid email address - ";
		}
		if (!credentialCheck) {
			errorMsg = errorMsg + " - username already taken - ";
		}
		if (!psw.equals(repeatpsw)) {
			errorMsg = errorMsg + "- password and repeaten password are different - ";
		}

		if (!errorMsg.equals("")) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorSignupMsg", errorMsg);
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
		} else {

			User user = new User();
			// creation user in db
			try {
				userDao.createUser(mail, psw, username, name, surname);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to create user");
				return;
			}

			try {
				user = userDao.getUserByUsername(username);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find user");
				return;
			}

			// adding user to session parameters
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/Home";
			response.sendRedirect(path);
		}

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}