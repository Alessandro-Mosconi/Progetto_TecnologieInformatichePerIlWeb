package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.DAO.MeetingDAO;
import it.polimi.tiw.DAO.InvitationDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckGuests")
@MultipartConfig
public class CheckGuests extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// obtaining user and counter from session
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		Integer counter = (Integer) session.getAttribute("counter");

		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		List<User> usersChosed = new ArrayList<User>();

		// obtaining all user
		try {
			users = userDAO.findAllUsers();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover user list");
			return;
		}

		// adding to userChosed only the user selected in the form
		try {
			for (User u : users) {
				if (request.getParameter(u.getId().toString()) != null) {
					u.setFlag(true);
					usersChosed.add(u);
				}
			}
			if (usersChosed.size() == 0 || usersChosed == null)
				throw new Exception("No user selected");

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("no user selected");
			return;
		}

		// obtaining meeting data by the form and checking validity
		boolean isBadRequest = false;
		LocalDateTime startDate = null;
		String title = null;
		Integer duration = null;
		Integer maxGuests = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			startDate = LocalDateTime.parse((String) request.getParameter("date"), formatter);
			System.out.println("\n\n" + startDate + "\n\n");
			maxGuests = Integer.parseInt(request.getParameter("maxGuests"));
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			duration = Integer.parseInt(request.getParameter("duration"));
			isBadRequest = maxGuests <= 0 || title.isEmpty() || duration <= 0
					|| startDate.isBefore(java.time.LocalDateTime.now());

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("incorrect or missing meeting param values");
			return;
		}

		// checking starting date validity
		if (startDate.isBefore(java.time.LocalDateTime.now())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("invalid starting date");
			return;
		}

		Meeting meeting = new Meeting();
		meeting.setDuration(duration);
		meeting.setMaxGuests(maxGuests);
		meeting.setStartDate(startDate);
		meeting.setTitle(title);

		// if number of user selected is more than the maximum counter--
		// if the number of attempts is 0 send message
		// else update counter

		if (usersChosed.size() > maxGuests) {
			counter = counter - 1;
			if (counter == 0) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("Attempts ended");
				return;
			} else {
				request.getSession().setAttribute("counter", counter);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("Too much user selected");
				return;
			}
		}

		// if user selection is valid create meeting

		MeetingDAO meetingDAO = new MeetingDAO(connection);
		try {
			meetingDAO.createMeeting(title, meeting.getStartDate(), duration, maxGuests, user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create meeting");
			return;
		}

		// find meeting id
		try {
			meeting = meetingDAO.findLastMeetingByCreator(user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to find meeting");
			return;
		}

		// create invitation

		InvitationDAO invitationDAO = new InvitationDAO(connection);

		for (User u : usersChosed) {
			try {
				invitationDAO.createInvitation(meeting.getId(), u.getId());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create invitation");
				return;
			}
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Success");
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}