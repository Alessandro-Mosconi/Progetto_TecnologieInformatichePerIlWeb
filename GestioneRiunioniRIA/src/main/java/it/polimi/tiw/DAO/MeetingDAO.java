package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.List;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Meeting;

public class MeetingDAO {
	private Connection con;

	public MeetingDAO(Connection connection) {
		this.con = connection;
	}
	
	public List<Meeting> findMeetingByCreator(int creatorId) throws SQLException {
		List<Meeting> meetings = new ArrayList<Meeting>();
		
		String query = "SELECT * from meeting where idowner = ? ORDER BY date DESC";
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, creatorId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Meeting meeting = new Meeting();
					meeting.setStartDate(result.getString("date"));
					if (!meeting.getStartDate().isBefore(java.time.LocalDateTime.now())) {
						meeting.setId(result.getInt("idmeeting"));
						meeting.setTitle(result.getString("title"));
						meeting.setMaxGuests(result.getInt("max_partecipant"));
						meeting.setDuration(result.getInt("duration"));
						meeting.setOwnerId(creatorId);
						meetings.add(meeting);
					}
				}
			}
		}
		return meetings;
	}
		
		public Meeting findLastMeetingByCreator(int creatorId) throws SQLException {
			
			String query = "SELECT * from meeting where idowner = ? ORDER BY idmeeting DESC LIMIT 1";

			Meeting meeting = new Meeting();
			try (PreparedStatement pstatement = con.prepareStatement(query);) {
				pstatement.setInt(1, creatorId);
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next()) {
						meeting.setStartDate(result.getString("date"));
						if (!meeting.getStartDate().isBefore(java.time.LocalDateTime.now())) {
							meeting.setId(result.getInt("idmeeting"));
							meeting.setTitle(result.getString("title"));
							meeting.setMaxGuests(result.getInt("max_partecipant"));
							meeting.setDuration(result.getInt("duration"));
							meeting.setOwnerId(creatorId);
						}
						else meeting = null;
					}
				}
			}
		return meeting;
	}
	
	public List<Meeting> findMeetingByGuest(int id) throws SQLException {
		List<Meeting> meetings = new ArrayList<Meeting>();
			
			String query = "SELECT * from invitation NATURAL JOIN meeting where idpartecipant = ? ORDER BY date DESC";
			
			try (PreparedStatement pstatement = con.prepareStatement(query);) {
				pstatement.setInt(1, id);
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next()) {
						Meeting meeting = new Meeting();
						meeting.setStartDate(result.getString("date"));
						if (!meeting.getStartDate().isBefore(java.time.LocalDateTime.now())) {
						meeting.setId(result.getInt("idmeeting"));
						meeting.setTitle(result.getString("title"));
						meeting.setMaxGuests(result.getInt("max_partecipant"));
						meeting.setDuration(result.getInt("duration"));
						meeting.setOwnerId(result.getInt("idowner"));
						UserDAO userDao = new UserDAO(con);
						User creator = new User();
						creator = userDao.getUser(result.getInt("idowner"));
						meeting.setOwner(creator.getMail());
						meetings.add(meeting);
						}
					}
				}
			}
			return meetings;
		}

	public void createMeeting(String title, LocalDateTime startDate, Integer duration, Integer maxGuests, Integer creator)
			throws SQLException {

		String query = "INSERT into meeting (title, date, duration, max_partecipant, idowner) VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			
			pstatement.setString(1, title);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			Timestamp timestamp = Timestamp.valueOf(startDate.format(formatter));
			pstatement.setTimestamp(2, timestamp);
			pstatement.setInt(3,  duration);
			pstatement.setInt(4, maxGuests);
			pstatement.setInt(5, creator);
			pstatement.executeUpdate();
			
		}
		
	}
	
}
