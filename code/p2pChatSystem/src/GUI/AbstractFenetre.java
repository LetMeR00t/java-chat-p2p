package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;


public abstract class AbstractFenetre extends JFrame implements Observer, ActionListener{

	/************************************************* 
	 * 				ATTRIBUTS & FIELDS 
	 ************************************************/
	
	
	
	/************************************************* 
	 * 				CONSTRUCTOR 
	 ************************************************/
	
	
	/************************************************* 
	 * 				GETTERS & SETTERS
	 ************************************************/
	
	
	
	/************************************************* 
	 * 					METHODS 
	 ************************************************/

	/*
	 * Attention, les m�thodes suivantes :
	 * public void actionPerformed(ActionEvent arg0)
	 * public void update(Observable arg0, Object arg1)
	 * doivent �tre impl�menter dans les classes filles
	 */
	
	public abstract void initializeComponents();
}