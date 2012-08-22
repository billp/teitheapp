package org.teitheapp.classes;

public class MimeType {
	private int id;
	private String mimeType, ext;
	
	public MimeType(String mimeType, String ext) {
		super();
		this.mimeType = mimeType;
		this.ext = ext;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}
	
	
}
