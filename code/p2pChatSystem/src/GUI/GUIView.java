package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import Controler.GUIToControler;
import Controler.NetworkInformation;
import Controler.NetworkToControler;
import Controler.typeOfChange;
import GUI.GUIControler.Etats;
import NI.NIControler;

public class GUIView implements Observer{

	/************************************************* 
	 * 				ATTRIBUTS & FIELDS 
	 ************************************************/
	
	private static GUIView singleton;
	
	private ChatFenetre chatFenetre;
	
	private GUIControler guiControler ;
	
	private ArrayList<ConversationFenetre> listOfConversationFenetre ;
	
	/************************************************* 
	 * 				CONSTRUCTOR 
	 ************************************************/
	
	private GUIView() {
		chatFenetre = chatFenetre.getInstance();
		chatFenetre.setVisible(true);
		guiControler = guiControler.getInstance() ;
		listOfConversationFenetre = new ArrayList<ConversationFenetre>() ;
	}
	
	public static GUIView getInstance(){
		if (singleton == null){
			singleton = new GUIView();
		}
		return singleton;
	}
	
	/************************************************* 
	 * 				GETTERS & SETTERS
	 ************************************************/
	
	public ChatFenetre getChatFenetre () {
		return this.chatFenetre ;
	}
	
	public GUIControler getGUIControler(){
		return this.guiControler;
	}
	
	public ArrayList<ConversationFenetre> getConversationFenetre () {
		return this.listOfConversationFenetre ;
	}
	
	/************************************************* 
	 * 					METHODS 
	 * @throws UnknownHostException 
	 ************************************************/

	public static void initChatSystem (GUIView guiView) {
		/** Initialisation du Controler **/
		NetworkToControler netToCon = null ;
		netToCon = netToCon.getInstance() ;
		
		GUIToControler guiToCon = null ;
		guiToCon = guiToCon.getInstance() ;
		
		/** Initialisation du NI **/
		NIControler niCon = null ;
		niCon = niCon.getInstance() ;
		
		/** Creation des liens **/
		netToCon.getNetInfo().setGuiView(guiView);
		guiToCon.setNiCon(niCon);
		niCon.setNetToCon(netToCon);
		niCon.getUDPReceiver().setNiCon(niCon);
	}	
	
	protected void Connection (String name) throws UnknownHostException {
		if (name.length() == 0)
			JOptionPane.showMessageDialog(null, "Please choose a nickname.", "Error", JOptionPane.ERROR_MESSAGE);
		else {
			initChatSystem(this) ;
			guiControler.Connection(name);
		}
	}

	protected void Disconnection () {
		guiControler.Disconnection();
	}
	
	protected void TextMessage (String message, TreeSet <Integer> listOfId) throws UnknownHostException {
		guiControler.TextMessage(message, listOfId) ;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof NetworkInformation){
			NetworkInformation NI = NetworkInformation.getInstance();
			
			if (NI.getLastChange().equals(typeOfChange.CONNECTION)) {
				/** le bouton connect change d'aspect **/
				this.chatFenetre.getConnectDisconnectPanel().getButtonConnectOnOff().setText("Deconnexion");
				this.chatFenetre.getConnectDisconnectPanel().getNicknameField().setEditable(false);
				this.chatFenetre.getConnectDisconnectPanel().setImageStatus(new ImageIcon("online.png"));
				guiControler.setEtatConnect();
			}
			
			else if (NI.getLastChange().equals(typeOfChange.DISCONNECTION)) {
				/** le bouton connect change d'aspect **/
				this.chatFenetre.getConnectDisconnectPanel().getButtonConnectOnOff().setText("Connexion");
				this.chatFenetre.getConnectDisconnectPanel().getNicknameField().setEditable(true);
				this.chatFenetre.getConnectDisconnectPanel().setImageStatus(new ImageIcon("offline.png"));
				this.chatFenetre.getContactsListPanel().getDefaultListModel().clear() ;
				guiControler.setEtatDisconnect();
			}
			else if (NI.getLastChange().equals(typeOfChange.ADDUSER_HELLO)){	
				int idUser = (Integer)arg1;
				String nickname = guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname() ;
				this.chatFenetre.getContactsListPanel().getDefaultListModel().addElement(nickname);
				this.chatFenetre.pack();
				this.guiControler.getGUIToControler().addIDListModel(idUser);

				// on envoit un HelloAck au nouvel user
				this.guiControler.getGUIToControler().performSendHelloAck(idUser);
			}
			
			else if (NI.getLastChange().equals(typeOfChange.ADDUSER_HELLO_ACK)){	
				int idUser = (Integer)arg1;
				String nickname = guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname() ;
				this.chatFenetre.getContactsListPanel().getDefaultListModel().addElement(nickname);
				this.chatFenetre.pack();
				this.guiControler.getGUIToControler().addIDListModel(idUser);
			}
			
			else if (NI.getLastChange().equals(typeOfChange.REMOVEUSER)) {
				
				int idUser = (Integer)arg1;
				String nickname = guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname() ;
				// suppression du user de la liste de contact
				if (this.chatFenetre.getContactsListPanel().getDefaultListModel().removeElement(nickname)){
					System.out.println("JList : remove "+nickname+" success");
				}
				else{
					System.out.println("JList - ERROR : remove "+nickname+" failed !");
				}
				// remise en forme de la liste visuelle
				this.chatFenetre.pack();
				this.guiControler.getGUIToControler().removeIDListModel(idUser);
				
				//suppression du user des conversations
				for (int i = 0; i<this.listOfConversationFenetre.size(); i++) {
					// on supprime l'ID du user de toutes les conversations
					if (this.listOfConversationFenetre.get(i).getListOfIds().contains(idUser))
						this.listOfConversationFenetre.get(i).getListOfIds().remove(idUser) ;
					// on supprime son nickname de toutes les conversations
					if (this.guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser) == null)
						System.out.println("USER NULL") ;
					String name = this.guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname() ;
					System.out.println("BUG : "+name) ;
					if (this.listOfConversationFenetre.get(i).getListOfNicknames().contains(this.guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname()))
						this.listOfConversationFenetre.get(i).getListOfNicknames().remove(this.guiControler.getGUIToControler().getNetInfo().getUserWithId(idUser).getNickname()) ;
					// mise a jour de la fenetre
					this.listOfConversationFenetre.get(i).miseAJourFenetre() ;
					// si la conversation est alors vide, on la supprime de la liste
					if (this.listOfConversationFenetre.get(i).getListOfIds().isEmpty()) {
						this.listOfConversationFenetre.remove(i) ;
					}
				}
			}

			else if (NI.getLastChange().equals(typeOfChange.NEWINCOMINGTEXTMESSAGE)){
				// On recupere la conversation
				TreeSet<Integer> listOfIds = (TreeSet<Integer>)arg1 ;
				String conversation = NI.getHistoricConversations().get(listOfIds);
				
				boolean found = false ;
				int i = 0 ;
				while (!found && i < this.listOfConversationFenetre.size()) {
					if (this.listOfConversationFenetre.get(i).getListOfIds().equals(listOfIds)) {
						found = true ;
						this.listOfConversationFenetre.get(i).getHistoricArea().setText(conversation) ;
						if (!this.listOfConversationFenetre.get(i).isVisible()) {
							System.out.println("FENETRE NOT VISIBLE") ;
							String nicknames = null ;
							for (Integer aux : listOfIds) {
								if (nicknames ==null) 
									nicknames = this.getGUIControler().getGUIToControler().getNetInfo().getUserWithId(aux).getNickname()+", " ;
								else
									nicknames = nicknames + this.getGUIControler().getGUIToControler().getNetInfo().getUserWithId(aux).getNickname()+", " ;
							}
							JOptionPane.showMessageDialog(null, "There is a new message in your conversation with "+nicknames, "New Message Notification", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					i++ ;
				}
				
			}
			
			else if (NI.getLastChange().equals(typeOfChange.NEWINCOMINGFILEMESSAGE)){
				
				// On recupere la conversation
				TreeSet<Integer> listOfIds = (TreeSet<Integer>)arg1 ;
				String conversation = NI.getHistoricConversations().get(listOfIds);
				
				boolean found = false ;
				int i = 0 ;
				while (!found && i < this.listOfConversationFenetre.size()) {
					if (this.listOfConversationFenetre.get(i).getListOfIds().equals(listOfIds)) {
						found = true ;
						this.listOfConversationFenetre.get(i).getHistoricArea().setText(conversation) ;
						if (!this.listOfConversationFenetre.get(i).isVisible()) {
							System.out.println("FENETRE NOT VISIBLE") ;
							String nicknames = null ;
							for (Integer aux : listOfIds) {
								if (nicknames ==null) 
									nicknames = this.getGUIControler().getGUIToControler().getNetInfo().getUserWithId(aux).getNickname()+", " ;
								else
									nicknames = nicknames + this.getGUIControler().getGUIToControler().getNetInfo().getUserWithId(aux).getNickname()+", " ;
							}
							JOptionPane.showMessageDialog(null, "There is a new incoming file in your conversation with "+nicknames, "New File Notification", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					i++ ;
				}
				
				
			}
		}
		// Permet de placer correctement l'ensemble des composants
		this.getChatFenetre().pack();
			
	}

	
}
