package org.teitheapp.classes;

import java.util.Date;

public class Announcement {
	private String body, category, author, title, attachmentUrl;
	private Date date;
	
	
	
	public Announcement(String body, String category, String author, String title,
			String attachmentUrl, Date date) {
		super();
		this.category = category;
		this.author = author;
		this.title = title;
		this.attachmentUrl = attachmentUrl;
		this.date = date;
		this.body = body;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getAuthor() {
		return author;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getAttachmentUrl() {
		return attachmentUrl;
	}


	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}


	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public Boolean hasAttachment() {
		return !this.attachmentUrl.equals("");
	}


}
