package Controler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * @author Val�rie Daras et Alexandre Demeyer
 */

public class NetworkToControler {

	/************************************************* 
	 * 				ATTRIBUTS and FIELDS 
	 ************************************************/
	
	// Reference a NetworkToControler
	private static NetworkToControler netToContSingleton ;
	
	// Reference a NetworkInformation
	private static NetworkInformation NI;
	
	
	/************************************************* 
	 * 				CONSTRUCTOR 
	 ************************************************/
	
	/**
	 * Constructeur par defaut
	 */
	private NetworkToControler() {
		NI = NetworkInformation.getInstance();
	}
	
	/** 
	 * Methode qui permet d'obtenir l'instance de la classe 
	 * @return l'objet NetworkToControler
	 */
	public static NetworkToControler getInstance() {
			if (netToContSingleton == null) {
				netToContSingleton = new NetworkToControler() ;
			}
			return netToContSingleton ;
	}

	/************************************************* 
	 * 				GETTERS & SETTERS
	 ************************************************/
	
	
	/**
	 * Getter recuperant NetworkInformation
	 * @return NetworkInformation
	 */
	public NetworkInformation getNetInfo () {
		return NI ;
	}
	
	/************************************************* 
	 * 					METHODS
	 ************************************************/
	
	/** 
	 * Methode permettant de traiter la reception d'un Hello sur le reseau
	 * @param name : nom de la personne envoyant le message (au format "nom@IP")
	 * @param ipAddress : l'adresse IP de la personne
	 */
	public void processHello(String name, InetAddress ipAddress) {
		// ajout du nouvel user
		String nameWithoutPattern = NI.getNicknameWithoutIP(name);
		NI.addUser(nameWithoutPattern, ipAddress, false) ;
	}
	
	/**
	 * Methode permettant de traiter la reception d'un HelloAck sur le reseau
	 * @param name : nom de la personne envoyant le message (au format "nom@IP")
	 * @param ipAddress : l'adresse IP de la personne
	 */
	public void processHelloAck(String name, InetAddress ipAddress) {
		String nameWithoutPattern = NI.getNicknameWithoutIP(name);
		NI.addUser(nameWithoutPattern, ipAddress, true) ;
	}
	
	/**
	 * Methode permettant de traiter la reception d'un Goodbye sur le reseau
	 * @param name : nom de la personne envoyant le message (au format "nom@IP")
	 * @param ipAddress : l'adresse IP de la personne
	 */
	public void processGoodbye(String name, InetAddress ipAddress) {
		if (NI.getUserList() != null) {
			if (NI.getUserList().containsKey(ipAddress))
				NI.removeUser(ipAddress);
		}
	}
	
	/**
	 * Methode permettant de traiter la reception d'un TextMessage sur le reseau
	 * @param message : le message envoye aux utilisateurs
	 * @param listOfUsernames : la liste des noms d'utilisateurs concernes
	 */
	public void processTextMessage(String message, ArrayList<String> listOfUsernames) {
		
		// On crée une liste d'ID Utilisateurs
		TreeSet<Integer> listOfIDs = new TreeSet<Integer>();
		
		// On déclare un utilisateur pointant sur null pour une utilisation par la suite
		User user = null;
		String ipString = null;
		InetAddress ip = null;
		
		// On récupère l'ensemble des id de chaque utilisateur concerné par le message
		for (int i = 0; i < listOfUsernames.size(); i++){
			ipString = NI.getIPOfPattern(listOfUsernames.get(i));
			try {
				ip = InetAddress.getByName(ipString);
			} catch (UnknownHostException e) {
				System.out.println("NETWORK TO CONTROLER - ProcessTextMessage : Impossible de recuperer l'adresse IP de la source du TextMessage") ;
			}
			if ((user = NI.getUserList().get(ip)) != null){
				listOfIDs.add(user.getIdUser());
			}
			else{
				System.out.println("ERREUR - ProcessTextMessage - User who has the ip address "+ip+" doesn't exist");
			}
		}

		// On definit le format d'affichage du message
		String finalMessage ;
		if (NI.getHistoricConversations().get(listOfIDs) == null) {
			finalMessage = user.getNickname()+" : "+message+"\n";
		}
		else {
			finalMessage = NI.getHistoricConversations().get(listOfIDs) + user.getNickname()+" : "+message+"\n";
		}
		
		// Ajout du message a l'historique 
		NI.getHistoricConversations().put(listOfIDs, finalMessage);

		// Notification envoyée à la vue
		NI.notifyLastChange(typeOfChange.NEWINCOMINGTEXTMESSAGE, listOfIDs);
	}
	
	/**
	 * Methode permettant de traiter la reception d'un FileMessage sur le reseau
	 * @param nameFile : nom du fichier
	 * @param listOfUsernames : la liste des noms d'utilisateurs concernes
	 */
	public void processFileMessage(String nameFile, ArrayList<String> listOfUsernames) {
			
		// On crée une liste d'ID Utilisateurs
		TreeSet<Integer> listOfIDs = new TreeSet<Integer>();
		
		// On déclare un utilisateur pointant sur null pour une utilisation par la suite
		User user = null;
		String ipString = null;
		InetAddress ip = null;
		
		// On récupère l'ensemble des id de chaque utilisateur concerné par le message
		for (int i = 0; i < listOfUsernames.size(); i++){
			ipString = NI.getIPOfPattern(listOfUsernames.get(i));
			try {
				ip = InetAddress.getByName(ipString);
			} catch (UnknownHostException e) {
				System.out.println("NETWORK TO CONTROLER - ProcessFileMessage : Impossible de recuperer l'adresse IP de la source du FileMessage") ;
			}
			if ((user = NI.getUserList().get(ip)) != null){
				listOfIDs.add(user.getIdUser());
			}
			else{
				System.out.println("NETWORK TO CONTROLER - ProcessFileMessage - User who has the ip address "+ip+" doesn't exist");
			}
		}
		
		// On definit le format d'affichage du message
		String finalMessage ;
		if (NI.getHistoricConversations().get(listOfIDs) == null) {
			finalMessage = user.getNickname()+" : "+nameFile+"\n";
		}
		else {
			finalMessage = NI.getHistoricConversations().get(listOfIDs) + user.getNickname()+" : "+nameFile+"\n";
		}
		
		// Ajout du message a l'historique 
		NI.getHistoricConversations().put(listOfIDs, finalMessage);

		// Notification envoyée à la vue
		NI.notifyLastChange(typeOfChange.NEWINCOMINGFILEMESSAGE, listOfIDs);				
	}
}
