package it.polimi.tiw.beans;

//import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

public class Invitation {
	private int id;
	private int ownerId;
	private String title;
	private int max_partecipant;
	private LocalDateTime dateTime;
	private Time duration;

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMax_partecipant() {
		return max_partecipant;
	}

	public void setMax_partecipant(int max_partecipant) {
		this.max_partecipant = max_partecipant;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Time getDuration() {
		return duration;
	}

	public void setDuration(Time duration) {
		this.duration = duration;
	}

}
