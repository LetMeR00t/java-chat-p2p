package Controler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable; 
import java.util.TreeSet;
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
	
	/** Contient les indices de positions des utilisateurs dans la liste visuelle de la view **/
	private ArrayList<Integer> arrayPositionsListModel;
	
	/** Contient l'historique de toutes les conversations **/
	private HashMap<TreeSet<Integer>, String> historicConversations;
	
	/************************************************* 
	 * 					CONSTRUCTOR
	 ************************************************/
	/** Constructeur **/
	private NetworkInformation () { 
		usersIPAddress = new HashMap <InetAddress, User> () ;
		lastChange = typeOfChange.DISCONNECTION;
		arrayPositionsListModel = new ArrayList<Integer>();
		historicConversations = new HashMap<TreeSet<Integer>,String>();
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
	
	/** Getter du champ lastChange **/
	public typeOfChange getLastChange(){
		return lastChange;
	}
	
	/** Getter du tableau des positions du composant JList **/
	public ArrayList<Integer> getArrayPositionsListModel(){
		return arrayPositionsListModel;
	}
	
	/** Getter de la hashmap contenant l'historique des conversations **/
	public HashMap<TreeSet<Integer>,String> getHistoricConversations(){
		return historicConversations;
	}
	
	/************************************************* 
	 * 					METHODS 
	 ************************************************/
	
	/**
	 * Permet d'indiquer aux observateurs qu'il y a eut un changement et de quel type
	 * @param lastChange : dernier changement a indique a l'observateur
	 */
	public void notifyLastChange(typeOfChange lastChange){
		
		this.lastChange = lastChange;
		System.out.println("Observer is notified : " + lastChange);
		setChanged();
		notifyObservers();
		
	}
	
	/**
	 * Permet d'indiquer aux observateurs qu'il y a eut un changement ,de quel type et un objet
	 * @param lastChange : dernier changement a indique a l'observateur
	 * @param arg1 : un objet en plus permettant de preciser le changement
	 */
	public void notifyLastChange(typeOfChange lastChange, Object arg1){
		
		this.lastChange = lastChange;
		System.out.println("Observer is notified : " + lastChange);
		setChanged();
		notifyObservers(arg1);
		
	}
	
	/** 
	 * Methode qui cree un User et l'ajoute a la HashMap  
	 * @param nickname : nom de l'utilisateur
	 * @param ip : son adresse IP
	 * @return l'objet User contenant les informations passees en parametres
	 */
	public User addUser (String nickname, InetAddress ip) {
		User user = new User (nickname) ;
		this.usersIPAddress.put(ip, user) ;
		notifyLastChange(typeOfChange.ADDUSER,user.getIdUser());		
		return user; 
	}
	
	/** 
	 * Methode qui supprime un User grace a son adresse IP *
	 * @param ip : adresse ip de l'utilisateur a supprimes
	 */
	public void removeUser (InetAddress ip) {
		int idUser = usersIPAddress.get(ip).getIdUser();
		notifyLastChange(typeOfChange.REMOVEUSER,idUser);
		this.usersIPAddress.remove(ip) ;
		
	}
	
	/**
	 *  Methode qui recupere l'adresse IP d'un utilisateur 
	 * @param user : User dont on veut l'adresse ip
	 * @return l'adresse ip du User
	 */
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
	
	/**
	 *  Methode qui ajoute le pattern @IP au nickname 
	 * @param user : le User contenant le nom
	 * @return "nom@IP"
	 * @throws UnknownHostException
	 */
	 
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
	
	/** 
	 * Methode qui recupere l'adresse ip contenu dans le nom
	 * @param name : nom contenant l'adresse ip
	 * @return l'adresse ip sans le "nom@"
	 */
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
	
	/**
	 * Methode qui enleve le pattern @IP au nickname 
	 * @param name : nom contenant l'adresse ip
	 * @return le nom sans l'adresse ip
	 */
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
	
	/**
	 * Methodes permettant de reinitialise les variables du systeme en cas de plusieurs connexions/deconnexions
	 */
	public void reinitializeVariables(){
		
		this.usersIPAddress.clear();
		this.localUser = null;
		this.arrayPositionsListModel.clear();
		this.historicConversations.clear();
		
	}
	
}
