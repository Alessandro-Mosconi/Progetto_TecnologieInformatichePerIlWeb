package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// private TemplateEngine templateEngine;
	private Connection connection = null;

	public CreateMeeting() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Get and parse all parameters from request
		boolean isBadRequest = false;
		LocalDateTime startDate = null;
		String title = null;
		Integer duration = null;
		Integer maxGuests = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			startDate = LocalDateTime.parse((String) request.getParameter("date"), formatter);
			maxGuests = Integer.parseInt(request.getParameter("maxGuests"));
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			duration = Integer.parseInt(request.getParameter("duration"));
			isBadRequest = maxGuests <= 0 || title.isEmpty() || duration <= 0
					|| startDate.isBefore(java.time.LocalDateTime.now());

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
		}
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}

		// creating meeting object and adding it to session
		Meeting meeting = new Meeting();
		meeting.setDuration(duration);
		meeting.setMaxGuests(maxGuests);
		meeting.setStartDate(startDate);
		meeting.setTitle(title);

		request.getSession().setAttribute("meeting", meeting);

		// return the user to the right view
		String path = getServletContext().getContextPath() + "/Anagrafe";
		response.sendRedirect(path);

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
