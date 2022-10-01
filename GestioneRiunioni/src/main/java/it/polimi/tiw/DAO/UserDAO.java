package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkLoginCredentials(String username, String psw) throws SQLException {
		String query = "SELECT  iduser, mail, username, name, surname FROM user  WHERE username = ? AND password = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, psw);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("iduser"));
					user.setMail(result.getString("mail"));
					user.setUsername(username);
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}
	
	public boolean checkSignupCredentials(String username) throws SQLException {
		String query = "SELECT  iduser FROM user  WHERE username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				
				return !result.isBeforeFirst();//true see non ci sono risultati
			}
		}
	}
		
	public List<User> findAllUsers() throws SQLException {
		List<User> users = new ArrayList<User>();
			
			String query = "SELECT * from user";
			
			try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next()) {
						User user = new User();
						user.setId(result.getInt("iduser"));
						user.setMail(result.getString("mail"));
						user.setName(result.getString("name"));
						user.setSurname(result.getString("surname"));
						
						users.add(user);
					}
				}
			}
			return users;
		}
	
	public User getUser(int id) throws SQLException {
		
			String query = "SELECT  mail, username, name, surname FROM user  WHERE iduser = ?";
			try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				pstatement.setInt(1, id);
				try (ResultSet result = pstatement.executeQuery();) {
					if (!result.isBeforeFirst()) // no results, credential check failed
						return null;
					else {
						result.next();
						User user = new User();
						user.setId(id);
						user.setMail(result.getString("mail"));
						user.setName(result.getString("name"));
						user.setUsername(result.getString("username"));
						user.setSurname(result.getString("surname"));
						return user;
					}
				}
			}
		}
	

	public User getUserByUsername(String username) throws SQLException {
		
		String query = "SELECT  iduser, mail, name, surname FROM user  WHERE username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);

			System.out.println("ora provvo il result");
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("iduser"));
					user.setMail(result.getString("mail"));
					user.setUsername(username);
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}

	
	public void createUser(String mail, String psw, String username, String name, String surname)
			throws SQLException {

		String query = "INSERT into user (mail, password, username, name, surname) VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			
			pstatement.setString(1, mail);
			pstatement.setString(2, psw);
			pstatement.setString(3, username);
			pstatement.setString(4, name);
			pstatement.setString(5, surname);
			pstatement.executeUpdate();
			
		}
		
	}

}
