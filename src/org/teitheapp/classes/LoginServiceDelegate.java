package org.teitheapp.classes;

public interface LoginServiceDelegate {
	public void loginSuccess(String cookie, String am, String surname, String name, int loginMode);
	public void loginFailed(int status, int loginMode);
}