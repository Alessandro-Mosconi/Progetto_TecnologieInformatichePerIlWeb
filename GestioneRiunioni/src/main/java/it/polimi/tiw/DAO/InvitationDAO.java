package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InvitationDAO {
	private Connection con;

	public InvitationDAO(Connection connection) {
		this.con = connection;
	}


	public void createInvitation(Integer idMeeting, Integer idUser)
			throws SQLException {

		String query = "INSERT into invitation (idmeeting, idpartecipant) VALUES(?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, idMeeting);
			pstatement.setInt(2, idUser);
			pstatement.executeUpdate();
			
		}
	}

}
