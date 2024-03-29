package Controler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable; 
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import GUI.GUIView;
import Controler.typeOfChange;

/**
 * @author Val�rie Daras et Alexandre Demeyer
 */

public class NetworkInformation extends Observable {

	/************************************************* 
	 * 				ATTRIBUTS & FIELDS 
	 ************************************************/
	
	/** Singleton **/
	private static NetworkInformation InfoSingleton ;
	
	/** Local User **/
	private User localUser ;
	
	/** Liste de User **/
	private ArrayList<User> userList ;
	
	/** Correspondance entre User et adresse IP **/
	private HashMap<InetAddress, User> usersIPAddress ;
	
	/** Singleton **/
	private static GUIView guiView;
	
	/** Indique le dernier changement effectue sur les informations du reseau **/
	private typeOfChange lastChange;
	
	/** Contient les indices de positions des utilisateurs dans la liste visuelle de la view **/
	private ArrayList<Integer> arrayPositionsListModel;
	
	/** Contient l'historique de toutes les conversations **/
	private HashMap<TreeSet<Integer>, String> historicConversations;
	
	
	/************************************************* 
	 * 					CONSTRUCTOR
	 ************************************************/
	/** 
	 * Constructeur par defaut
	*/
	private NetworkInformation () { 
		usersIPAddress = new HashMap <InetAddress, User> () ;
		userList = new ArrayList<User> () ;
		lastChange = typeOfChange.DISCONNECTION;
		arrayPositionsListModel = new ArrayList<Integer>();
		historicConversations = new HashMap<TreeSet<Integer>,String>();
	}
	
	/** 
	 * Methode creant une instance de classe si necessaire et renvoie l'objet
	 * @return l'objet NetworkInformation
	 */
	public static NetworkInformation getInstance () {
		if (InfoSingleton == null)
			InfoSingleton = new NetworkInformation () ;
		return InfoSingleton ;
	}
	
	
	
	/************************************************* 
	 * 				GETTERS and SETTERS 
	 ************************************************/
	
	/**
	 * Getter du localUser
	 * @return l'objet localUser
	 */
	public User getLocalUser () {
		return localUser ;
	}
	
	/**
	 * Setter du localUser
	 * @param name : le nom de l'utilisateur local
	 */
	public void setLocalUser (String name) {
		if (name == null) 
			this.localUser = null ;
		else 
			this.localUser = new User(name) ;
	}
	
	/**
	 * Getter du UsersIPAddress
	 * @return la hashmap contenant les paires (AdresseIP,User)
	 */
	public HashMap<InetAddress,User> getUserList () {
		return usersIPAddress ;		
	}

	/** 
	 * Setter du GUIView
	 * @param guiview : l'objet GUIView a fixe
	 */
	public void setGuiView (GUIView guiview) {
		guiView = GUIView.getInstance();
		addObserver(guiView);
	}
	
	/** 
	 * Getter du champ lastChange
	 * @return le champ lastChange
	 */
	public typeOfChange getLastChange() {
		return lastChange;
	}
	
	/** 
	 * Getter du tableau des positions du composant JList
	 * @return le tableau contenant la position des utilisateurs dans la JList
	 */
	public ArrayList<Integer> getArrayPositionsListModel() {
		return arrayPositionsListModel;
	}
	
	/** 
	 * Getter de la hashmap contenant l'historique des conversations 
	 * @return la hashmap contenant les conversations
	 */
	public HashMap<TreeSet<Integer>,String> getHistoricConversations() {
		return historicConversations;
	}
	
	/************************************************* 
	 * 					METHODS 
	 ************************************************/
	
	/**
	 * Permet d'indiquer aux observateurs qu'il y a eut un changement et de quel type
	 * @param lastChange : dernier changement a indique a l'observateur
	 */
	protected void notifyLastChange(typeOfChange lastChange) {
		this.lastChange = lastChange;
		System.out.println("NETWORK INFORMATION - Observer is notified : " + lastChange);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Permet d'indiquer aux observateurs qu'il y a eut un changement ,de quel type et un objet
	 * @param lastChange : dernier changement a indique a l'observateur
	 * @param arg1 : un objet en plus permettant de preciser le changement
	 */
	protected void notifyLastChange(typeOfChange lastChange, Object arg1) {
		this.lastChange = lastChange;
		System.out.println("NETWORK INFORMATION - Observer is notified : " + lastChange);
		setChanged();
		notifyObservers(arg1);
	}
	
	/** 
	 * Methode qui cree un User et l'ajoute a la HashMap  
	 * @param nickname : nom de l'utilisateur
	 * @param ip : son adresse IP
	 * @param HelloAck : booleen indiquant si on ajoute un utilisateur a cause d'un HelloAck
	 * @return l'objet User contenant les informations passees en parametres
	 */
	protected User addUser (String nickname, InetAddress ip, boolean HelloAck) {
		User user = new User (nickname) ;
		System.out.println("NETINFO - ADD USER : USER ID :"+user.getIdUser()) ;
		this.usersIPAddress.put(ip, user) ;
		this.userList.add(user) ;
		if (HelloAck == true) {
			notifyLastChange(typeOfChange.ADDUSER_HELLO_ACK,user.getIdUser());	
		}
		else 
			notifyLastChange(typeOfChange.ADDUSER_HELLO,user.getIdUser());	
		return user; 
	}
	
	/** 
	 * Methode qui supprime un User grace a son adresse IP *
	 * @param ip : adresse ip de l'utilisateur a supprimes
	 */
	protected void removeUser (InetAddress ip) {
		int idUser = usersIPAddress.get(ip).getIdUser();
		notifyLastChange(typeOfChange.REMOVEUSER,idUser);
		this.usersIPAddress.remove(ip) ;
		// on ne supprime jamais les utilisateurs de la liste - inutile et couteux
		
	}
	
	/**
	 *  Methode qui recupere l'adresse IP d'un utilisateur 
	 * @param user : User dont on veut l'adresse ip
	 * @return l'adresse ip du User
	 */
	protected InetAddress getIPAddressOfUser(User user) {
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
	 * Permet d'obtenir l'adresse ip locale de l'utilisateur local en prenant garde au reseau utilise
	 * @return l'adresse ip locale du reseau
	 * @throws UnknownHostException : "Thrown to indicate that the IP address of a host could not be determined"
	 * @throws SocketException : "Thrown to indicate that there is an error creating or accessing a Socket."
	 */
	public String getLocalIPAddress () throws UnknownHostException, SocketException {
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		boolean found = false ;
		String ip = null ;
		while(e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration<InetAddress> ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress i = (InetAddress) ee.nextElement();
		        if (i.getHostAddress().startsWith("192.") | i.getHostAddress().startsWith("10.")) {
		        	found = true ;
		        	ip = i.getHostAddress() ;
		        }
		    }
		}
		if (found == false) {
			return InetAddress.getByName("192.168.1.23").getHostAddress() ;
		}
		else 
			return ip ;
	}
	

	/**
	 *  Methode qui ajoute le pattern @IP au nickname 
	 * @param user : le User contenant le nom
	 * @return "nom@IP"
	 */
	public String getNicknameWithIP (User user) {
		if (user.getIdUser() == InfoSingleton.getLocalUser().getIdUser()) {
			try {
				return (user.getNickname()+"@"+this.getLocalIPAddress()) ;
			} catch (UnknownHostException e) {
				System.out.println("Erreur lors de l'acquisition de l'@IP locale") ;
				return "0" ;
			} catch (SocketException e) {
				e.printStackTrace();
				return "0" ;
			}
		}
		else {
			return (user.getNickname()+"@"+(InfoSingleton.getIPAddressOfUser(user)).getHostAddress()) ;
		}
	}
	
	/** 
	 * Methode qui recupere l'adresse ip contenu dans le nom
	 * @param name : nom contenant l'adresse ip
	 * @return l'adresse ip sans le "nom@"
	 */
	protected String getIPOfPattern (String name){
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
	protected String getNicknameWithoutIP(String name){
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
 * Permet de recuperer un objet User grace � son id
 * @param id : id de l'utilisateur
 * @return l'objet User associe a l'id 
 */
	public User getUserWithId (int id) {
		User user = null ;
		// on commence a id-1 pour optimiser le parcours : c'est la premier position possible
		for (int i = id-1; i<this.userList.size(); i++) {
			if (userList.get(i).getIdUser() == id) {
				user = userList.get(i) ;
			break ;
			}
		}
		return user ;
	}
	
	
	/**
	 * Methodes permettant de reinitialise les variables du systeme en cas de plusieurs connexions/deconnexions
	 */
	protected void reinitializeVariables() {
		this.localUser = null;
		this.usersIPAddress = new HashMap <InetAddress, User> () ;
		this.userList = new ArrayList<User> () ;
		this.arrayPositionsListModel = new ArrayList<Integer>();
		this.historicConversations = new HashMap<TreeSet<Integer>,String>();
		User.reinitializeUsers() ;
	}
	
	
}
