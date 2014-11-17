package NI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import Signals.*;
import userModel.*;

public class UDPSender extends AbstractSender {

	// On a comme champ le singleton de la classe
	private static UDPSender singleton = null;
	
	
	/*
	 * On définit les constructeurs
	 */
	private UDPSender(){
		this.setPortEnvoi(5000);
	}
	
	/*
	 * Méthode pour récupérer l'instance 
	 */
	public static UDPSender getInstanceUDPSender(){
		if (singleton == null){
			singleton = new UDPSender();
		}
		return singleton;
	}
	
	/*
	 * Methode permettant d'envoyer un message en broadcast
	 */
	public void sendBroadcast(AbstractMessage message){
		/** creation d'un socket UDP**/
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket (this.getPortEnvoi()) ;
			System.out.println("Creation du socket UDPSender") ;
			
			if (message.getTypeContenu() == typeContenu.HELLO) {
				/** Creation du Datagram Packet **/
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput out = null;
				try {
					out = new ObjectOutputStream(bos);
				  	out.writeObject(message);
				  	byte[] buf = bos.toByteArray();
				  	DatagramPacket packet = new DatagramPacket (buf, buf.length, InetAddress.getByName("255.255.255.255"), this.getPortEcoute()) ;
				  	System.out.println("Paquet concu ! ") ;

				  	/** Envoi du paquet **/
				  	socket.send(packet);
				  	System.out.println("Paquet envoy�!") ;
				} catch (IOException e) {
					System.out.println("Erreur lors de l'�criture dans le UDP Sender") ;
				}
			}

			if (message.getTypeContenu() == typeContenu.GOODBYE) {
				
			}
		} catch (BindException e1) {
			System.out.println("Port for UDP SocketSender already used.") ;
		} catch (SocketException e2) {
			System.out.println("Creation of UDP SocketSender failed.") ;
		} catch (IOException e3) {
			System.out.println("IOException during Sending.") ;
		} finally {
			if (socket != null)
				socket.close() ;
		}
	}
	
	/*
	 * Methode permettant d'envoyer un message à une liste d'utilisateurs
	 */
	public void send(AbstractMessage message, ArrayList<User> listOfUsers){
		
	}
	
}
