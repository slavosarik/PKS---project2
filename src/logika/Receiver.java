package logika;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Receiver implements Runnable {	
	
	final int LIMIT = 1472;
	int TIMEOUT;
	
	public int NAHODNOST_CHYBY;
	private volatile boolean running = true;
	
	JTextArea textArea = null;
	public JLabel label = null;
	
	DatagramSocket sockPrijatie = null;
	DatagramSocket sockPotvrdenie = null;
	DatagramPacket dgpPotvrdenie = null;
	DatagramPacket dgpPrijatie = null;
	
	public int ack;
	public int ckc;
	public int last;
	public int repeated;
	
	public int poradieVzorky = 0;
	public int poradieFragmentu = 0;
	
	byte[] header = null;	
	public String prijatyHeader;	
	public int dataParity;
	
	List<String> vypis = null;
	List<String> vzorka = null;
	List<Vysledok> vysledky = null;
	Vysledok vysledok = null;	
	
	public Receiver(JTextArea textArea, JTextField field, JLabel label, String timeout) {
		this.textArea = textArea;
		NAHODNOST_CHYBY = Integer.parseInt(field.getText());
		this.label = label;
		this.TIMEOUT = Integer.parseInt(timeout);
	}
	
	public void terminate() {
		running = false;
	}

	public void run() {
		
		vysledky = new ArrayList<Vysledok>();		
		try {
			sockPrijatie = new DatagramSocket(27001);
			sockPrijatie.setSoTimeout(TIMEOUT);
			dgpPrijatie = new DatagramPacket(new byte[LIMIT], LIMIT);
			sockPotvrdenie = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}

		while (running) {
			//inicializacia
			vysledok = new Vysledok();
			vzorka = new ArrayList<String>();
			poradieFragmentu = 0;
			vypis = new ArrayList<String>();
			vypis.add("____________VZORKA cislo "+(poradieVzorky+1)+"_____________________\n");
			
			while (true && running) {
				try {
					sockPrijatie.receive(dgpPrijatie);
				} catch (IOException e) {
					// Neprijaty fragment kvoli timeoutu
					vypis.add("***************************\nTimout vyprsal........prijimanie fragmentov ukoncene\n**************************\n");
					for (String s : vypis)
						textArea.append(s);
					running = false;
					break;
				}
				vypis.add("____\n" + (poradieFragmentu + 1)
						+ " - Fragment bol prijaty, zdrojova adresa: "
						+ dgpPrijatie.getAddress().toString().replaceAll("/", "")
						+ "\n");
				
				//Bitovy String - hlavicka
				prijatyHeader = String.format(
						"%8s",
						Integer.toBinaryString(Integer.parseInt(
								FunctionsLogic.byteToHex(dgpPrijatie.getData()[0]),
								16))).replaceAll(" ", "0");
				
				vypis.add("Velkost: " + (dgpPrijatie.getLength() - 1) + 
						" ACK:"+ prijatyHeader.charAt(3) + 
						" Last fragment:"		+ prijatyHeader.charAt(2) + 
						" Detekcny kod:"	+ prijatyHeader.charAt(0) + 
						" Repeated:"+ prijatyHeader.charAt(1) + "\n");

				//pocitanie parity datovej casti
				ParityChecksum.getSingletonObject().update(dgpPrijatie.getData(), 1,
						dgpPrijatie.getLength()-1);
				dataParity = (int) ParityChecksum.getSingletonObject()
						.getValue();

				vypis.add("Vypocitany Detekcny kod: "
						+ (dataParity
								+ Integer.parseInt(Character
										.toString(prijatyHeader.charAt(1)))
								+ Integer.parseInt(Character
										.toString(prijatyHeader.charAt(2))) + Integer
									.parseInt(Character.toString(prijatyHeader
											.charAt(3)))) % 2 + "\n");
				
				
				//zistovanie ci detekcny kod celkoveho fragmentu sa zhoduje s detekcnym kodom v hlavicke
				if (Integer
						.parseInt(Character.toString(prijatyHeader.charAt(0))) != (dataParity
						+ Integer.parseInt(Character.toString(prijatyHeader
								.charAt(1)))
						+ Integer.parseInt(Character.toString(prijatyHeader
								.charAt(2))) + Integer.parseInt(Character
						.toString(prijatyHeader.charAt(3)))) % 2) {
					vypis.add("Detekcny kod nesedi\n");
					vysledok.pocetNespravnychFragmentov++;
					continue;
				}
				
				vysledok.pocetSpravnychFragmentov++;

				// /GENEROVANIE PRAVDEPODOBNOSTI VYSIELANIA POTVRDENIA
				Random random = new Random();
				int pick = random.nextInt(100);
				if (pick < NAHODNOST_CHYBY) {
					vypis.add("Potvrdenie nie je zaslane\n");
					continue;
				}
				
				vysledok.pocetACK++;
				poradieFragmentu++;
				
				//vytvaranie hlavicky potvrdenia, vratanie pocitania detekcneho kodu				
				ack = 1;
				last = 0;
				repeated = 0;
				ckc = 1;
				header = FunctionsLogic.createHeader(ckc, repeated, last, ack);

				//odosielanie potvrdenia
				try {
					dgpPotvrdenie = new DatagramPacket(header, 0, header.length);
					sockPotvrdenie.connect(
							InetAddress.getByName(dgpPrijatie.getAddress()
									.toString().replaceAll("/", "")), 27000);
					sockPotvrdenie.send(dgpPotvrdenie);

					vypis.add("Potvrdenie bolo odoslane na adresu: "
							+ dgpPrijatie.getAddress().toString()
									.replaceAll("/", "") + ", Detekcny kod: "+ack+"\n");

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				//pridanie obsahu fragmentu do aktualnej vytvaranej vzorky
				try {
					vzorka.add(new String(dgpPrijatie.getData(), "UTF-8").substring(
							1, dgpPrijatie.getLength() - 1));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					// vzorka je prazdna
				}
				
				//ak to je posledny fragment, tak ukoncujem prijimanie aktualnej vzorky
				if (Integer
						.parseInt(Character.toString(prijatyHeader.charAt(2))) == 1) {
					break;
				}

			}
			if (running == false)
				continue;
			
			vypis.add("______________KONIEC VZORKY CISLO "
					+ (poradieVzorky + 1)
					+ "__________________________________________\n");
			vypis.add("Pocet správnych fragmentov: "
					+ vysledok.pocetSpravnychFragmentov
					+ ", Pocet nesprávnych fragmentov: "
					+ vysledok.pocetNespravnychFragmentov
					+ ", Pocet odoslanych ACK: " + vysledok.pocetACK
					+ "\n");
			vypis.add("_____________________________________________________________________\n");
			poradieVzorky++;

			
			//thread na vypisanie priebehu prijimania vzorky
			final List<String> kopiaVypis = vypis;			
			new Thread() {
				public void run() {
					for (String s : kopiaVypis)
						textArea.append(s);					
				}
			}.start();		

		}
		sockPrijatie.close();
		sockPotvrdenie.close();		
		label.setText("Pripravene");
		
	}	
}
