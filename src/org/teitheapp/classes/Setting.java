package org.teitheapp.classes;

public class Setting {
	private int id;
	private String name;
	private String text;
	
	public Setting(int id, String name, String text) {
		super();
		this.id = id;
		this.name = name;
		this.text = text;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
