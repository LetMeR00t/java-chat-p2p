package Controler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import GUI.ConnectDisconnectPanel;
import GUI.GUIView;
import Signals.AbstractMessage;
import Signals.Hello;
import Controler.typeOfChange;

public class NetworkInformation extends Observable {

	/************************************************* 
	 * 				ATTRIBUTS & FIELDS 
	 ************************************************/
	
	/** Singleton **/
	private static NetworkInformation InfoSingleton ;
	
	/** Local User **/
	private User localUser ;
	
	/** Correspondance entre User et adresse IP **/
	private HashMap<InetAddress, User> usersIPAddress ;
	
	/** Singleton **/
	private static GUIView guiView;
	
	/** Indique le dernier changement effectu� sur les informations du r�seau **/
	private typeOfChange lastChange;
	
	
	/************************************************* 
	 * 					CONSTRUCTOR
	 ************************************************/
	/** Constructeur **/
	private NetworkInformation () { 
		usersIPAddress = new HashMap <InetAddress, User> () ;
		lastChange = typeOfChange.DISCONNECTION;
	}
	
	/** Methode creant une instance de classe si necessaire et renvoie l'objet**/
	public static NetworkInformation getInstance () {
		if (InfoSingleton == null)
			InfoSingleton = new NetworkInformation () ;
		return InfoSingleton ;
	}
	
	
	
	/************************************************* 
	 * 				GETTERS & SETTERS 
	 ************************************************/
	
	/** Getter du localUser **/
	public User getLocalUser () {
		return localUser ;
	}
	
	/** Setter du localUser **/
	public void setLocalUser (String name) {
		if (name == null) 
			this.localUser = null ;
		else 
			this.localUser = new User(name) ;
	}
	
	/** Getter du UsersIPAddress **/
	public HashMap<InetAddress,User> getUserList () {
		return usersIPAddress ;		
	}

	/** Setter du GUIView**/
	public void setGuiView (GUIView guiview) {
		guiView = guiView.getInstance();
		addObserver(guiView);
	}
	
	public typeOfChange getLastChange(){
		return lastChange;
	}
	
	/************************************************* 
	 * 					METHODS 
	 ************************************************/
	
	// Permet d'indiquer aux observateurs qu'il y a eut un changement et de quel type
	public void notifyLastChange(typeOfChange lastChange){
		
		System.out.println("Observer is notified : " + lastChange);
		this.lastChange = lastChange;
		setChanged();
		notifyObservers();
		
	}
	
	/** Methode qui cree un User et l'ajoute a la HashMap  **/
	public User addUser (String nickname, InetAddress ip) {
		User user = new User (nickname) ;
		this.usersIPAddress.put(ip, user) ;
		notifyLastChange(typeOfChange.ADDUSER);		
		return user; 
	}
	
	/** Methode qui supprime un User grace a son adresse IP **/
	public void removeUser (InetAddress ip) {
		this.usersIPAddress.remove(ip) ;
		notifyLastChange(typeOfChange.REMOVEUSER);
	}
	
	/** Methode qui recupere l'adresse IP d'un utilisateur **/
	public InetAddress getIPAddressOfUser(User user){
		InetAddress ip = null;
		Iterator<Entry<InetAddress, User>> it = usersIPAddress.entrySet().iterator();
		while (it.hasNext() && ip == null){
			Entry<InetAddress,User> entry = it.next();
			if (entry.getValue().getIdUser() == user.getIdUser()){
				ip = entry.getKey();
			}
		}
		return ip;
	}
	
	/** Methode qui ajoute le pattern @IP au nickname 
	 * @throws UnknownHostException **/
	public String getNicknameWithIP (User user) throws UnknownHostException {
		NetworkInformation NI = null;
		NI = NI.getInstance();
		if (user.getIdUser() == NI.getLocalUser().getIdUser()) {
			try {
				return (user.getNickname()+"@"+(InetAddress.getLocalHost()).getHostAddress()) ;
			} catch (UnknownHostException e) {
				System.out.println("Erreur lors de l'acquisition de l'@IP locale") ;
				return "0" ;
			}
			
		}
		else 
			return (user.getNickname()+"@"+(NI.getIPAddressOfUser(user)).toString()) ;
	}
	
	/** Methode qui enleve le pattern @IP au nickname **/
	public String getIPOfPattern (String name){
		 Pattern pattern = Pattern.compile("^(.*)@(([0-9]{1,3}[.]){3}[0-9]{1,3})");
	     Matcher matcher = pattern.matcher(name);
	     if (matcher.find()){
	    	 return matcher.group(2);
	     }
	     else
	     {
	    	return name; 
	     }
	}
	
	public String getNicknameWithoutIP(String name){
		Pattern pattern = Pattern.compile("^(.*)@(([0-9]{1,3}[.]){3}[0-9]{1,3})");
	     Matcher matcher = pattern.matcher(name);
	     if (matcher.find()){
	    	 return matcher.group(1);
	     }
	     else
	     {
	    	return name; 
	     }
	}
	
	public void reinitializeVariables(){
		
		this.usersIPAddress.clear();
		this.localUser = null;
		
	}
	
	/** Methodes en relation avec la classe Observable **/
	
}
