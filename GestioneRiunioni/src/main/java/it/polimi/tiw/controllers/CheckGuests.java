package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.DAO.MeetingDAO;
import it.polimi.tiw.DAO.InvitationDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckGuests")
public class CheckGuests extends HttpServlet {
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

		HttpSession session = request.getSession();
		Integer counter = (Integer) session.getAttribute("counter");

		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		List<User> usersChosed = new ArrayList<User>();

		try {
			users = userDAO.findAllUsers();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover user list");
			return;
		}

		try {
			for (User u : users) {
				if (request.getParameter(u.getId().toString()) != null) {
					u.setFlag(true);
					usersChosed.add(u);
				}
			}

			if (usersChosed.size() == 0 || usersChosed == null) {
				throw new Exception("No user selected");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No user selected");
			return;
		}

		Meeting meeting = (Meeting) session.getAttribute("meeting");
		User user = (User) session.getAttribute("user");

		String path;
		if (usersChosed.size() > meeting.getMaxGuests()) {
			counter = counter - 1;
			if (counter == 0) {
				// tentativi terminati
				path = "/WEB-INF/Cancellazione.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("users", users);
				templateEngine.process(path, ctx, response.getWriter());
			} else {

				// tentativi non terminati
				request.getSession().setAttribute("counter", counter);
				path = "/WEB-INF/Anagrafe.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("users", users);
				templateEngine.process(path, ctx, response.getWriter());
			}

		} else {

			// creo il meeting
			MeetingDAO meetingDAO = new MeetingDAO(connection);
			try {
				meetingDAO.createMeeting(meeting.getTitle(), meeting.getStartDate(), meeting.getDuration(),
						meeting.getMaxGuests(), user.getId());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create meeting");
				return;
			}

			try {
				meeting = meetingDAO.findLastMeetingByCreator(user.getId());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to find meeting");
				return;
			}

			// popolo la tabella degli inviti

			InvitationDAO invitationDAO = new InvitationDAO(connection);

			for (User u : usersChosed) {
				try {
					invitationDAO.createInvitation(meeting.getId(), u.getId());
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Not possible to create invitation");
					return;
				}
			}

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