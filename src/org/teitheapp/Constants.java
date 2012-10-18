package org.teitheapp;

public class Constants {
	public static final String URL_HYDRA = "https://hydra.it.teithe.gr";
	public static final String URL_PITHIA = "http://pithia.teithe.gr";
	
	public static final String URL_HYDRA_LOGIN = URL_HYDRA + "/s/index.php";
	public static final String URL_PITHIA_LOGIN = URL_PITHIA + "/unistudent/login.asp";
	
	public static final String URL_PITHIA_MYGRADES = URL_PITHIA + "/unistudent/stud_CResults.asp?studPg=1&mnuid=mnu3&";
	public static final String URL_PITHIA_MYINFO = URL_PITHIA + "/unistudent/studentMain.asp?mnuid=student&";
	public static final String URL_PITHIA_MY_DECLARATION = URL_PITHIA + "/unistudent/stud_NewClass.asp?studPg=1&mnuid=diloseis;newDil&";
	
	public static final String URL_HYDRA_TEACHER_INFO = URL_HYDRA + "/s/index.php?m=itdep-staffliststud&_lang=el";
	public static final String URL_HYDRA_ANNOUNCEMENTS = URL_HYDRA + "/s/index.php?m=itdep-bbstud&_lang=el";
	
	public static final String API_URL = "http://aetos.it.teithe.gr/~vpanag/teitheapp/json.php";
	
	
	public static final int TIMEOUT_CONNECTION = 5000;
	public static final int TIMEOUT_SOCKET_CONNECTION = 0;
	
	//Login timeouts in minutes
	public static final int HYDRA_LOGIN_TIMEOUT = 7;
	public static final int PITHIA_LOGIN_TIMEOUT = 7;
	
	
	//Hydra announcements fetch timeout
	public static final long HYDRA_ANNOUNCEMENTS_FETCH_TIMEOUT = 5 * 30 * 1000;
}
