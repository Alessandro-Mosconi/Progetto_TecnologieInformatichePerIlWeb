package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckSignup")
@MultipartConfig
public class CheckSignup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// obtaining and check login parameters
		String mail = null;
		String psw = null;
		String repeatpsw = null;
		String username = null;
		String name = null;
		String surname = null;
		String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

		mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
		psw = StringEscapeUtils.escapeJava(request.getParameter("psw"));
		repeatpsw = StringEscapeUtils.escapeJava(request.getParameter("repeatpsw"));
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		name = StringEscapeUtils.escapeJava(request.getParameter("name"));
		surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));

		if (mail == null || psw == null || repeatpsw == null || username == null || name == null || surname == null
				|| mail.isEmpty() || psw.isEmpty() || repeatpsw.isEmpty() || username.isEmpty() || name.isEmpty()
				|| surname.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null");
			return;
		}

		// checking if username already exists in db
		UserDAO userDao = new UserDAO(connection);
		boolean credentialCheck;
		try {
			credentialCheck = userDao.checkSignupCredentials(username);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}

		// checking if username email validity
		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(mail);

		// building error message
		String errorMsg = "";

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
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(errorMsg);
		} else {

			// user creation

			User user = new User();

			try {
				userDao.createUser(mail, psw, username, name, surname);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to create user");
				return;
			}

			// taking user from db to obtain the id
			try {
				user = userDao.getUserByUsername(username);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to find user");
				return;
			}

			// adding info to the session and go to home
			request.getSession().setAttribute("counter", 3);
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(user.getUsername());

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