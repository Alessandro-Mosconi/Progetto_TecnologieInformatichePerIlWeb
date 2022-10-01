package it.polimi.tiw.beans;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class Meeting {
	private Integer id;
	private int ownerId;
	private String title;
	private int maxGuests;
	private LocalDateTime startDate;
	private int duration;
	private String owner;

	
	
	public Integer getId() {
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

	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMaxGuests() {
		return maxGuests;
	}

	public void setMaxGuests(int maxGuests) {
		this.maxGuests = maxGuests;
	}

	public LocalDate getDate() {
		return startDate.toLocalDate();
	}
	
	public LocalTime getTime() {
		return startDate.toLocalTime();
	}
	
	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(String dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		this.startDate =LocalDateTime.parse(dateTime, formatter);
	}
	
	public String getStringDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return startDate.format(formatter);
	}
	
	public void setStartDate(LocalDateTime dateTime) {
		this.startDate = dateTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
