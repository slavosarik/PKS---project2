package logika;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Sender implements Runnable {

	int TIMEOUT;
	final int LIMIT_NA_POCET_OPAKOVANI = 5;
	final static int MAXDATA = 1471;
	final int ACK_FRAGMENT_SIZE = 1;

	public int NAHODNOST_CHYBY;

	int velkostVzorky = 0;
	int pocetVzoriek = 0;

	String cielovaAdresa = null;
	DatagramPacket dgpPosielanie = null;
	DatagramPacket dtgram;
	DatagramSocket sockPosielanie = null;
	DatagramSocket sockPrijimanie = null;

	byte[] fragment;
	byte[] data = null;
	byte[] header = null;

	JTextArea textArea = null;
	public JLabel label;

	public boolean finished;
	public boolean failed = false;
	public boolean canceled = false;

	String vzorka;
	List<String> listFragmentov;
	int actualFragment;

	public int ack;
	public int ckc;
	public int last;
	public int repeated;
	int ckcHeader;
	public int dataParity;
	String prijatyHeader;
	int pocetOpakovani = 0;

	StopWatch watch;
	public long globalTime = 0;

	Vysledok vysledok;
	public List<Vysledok> vysledky;

	public Sender(String adresa, String velkost, String pocet,
			JTextArea textArea, JLabel label, JTextField field, String timeout) {
		this.cielovaAdresa = adresa;
		this.velkostVzorky = Integer.parseInt(velkost);
		this.pocetVzoriek = Integer.parseInt(pocet);
		this.textArea = textArea;
		this.label = label;
		NAHODNOST_CHYBY = Integer.parseInt(field.getText());
		this.TIMEOUT = Integer.parseInt(timeout);
	}

	public void run() {
		
		textArea.setText("");		
		vysledky = new ArrayList<Vysledok>();

		watch = new StopWatch();
		new Thread(watch).start();

		vzorka = null;

		dtgram = new DatagramPacket(new byte[ACK_FRAGMENT_SIZE],
				ACK_FRAGMENT_SIZE);

		// pripajanie socketu
		try {
			sockPosielanie = new DatagramSocket();
			sockPrijimanie = new DatagramSocket(27000);
			sockPrijimanie.setSoTimeout(TIMEOUT);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		// zaciatok cyklu pre posielanie vzoriek
		for (int i = 0; i < pocetVzoriek; i++) {

			// vygenerovanie vzorky
			vzorka = FunctionsLogic.randomString(velkostVzorky);

			// fragmentacia na fragmenty
			listFragmentov = FunctionsLogic.fragment(vzorka);

			actualFragment = 0;
			finished = false;

			repeated = 0;
			last = 0;
			ack = 0;

			watch.stop();
			watch.reset();

			vysledok = new Vysledok();

			textArea.append("______________ZACIATOK VZORKY CISLO " + (i + 1)
					+ "__________________________\n");

			while (!finished) {

				if (canceled == true) {
					break;
				}
				if(repeated!=1)
					textArea.append("-------------zaciatok fragmentu "+(actualFragment + 1)+"-----------------\n");
				else
					textArea.append("-------------opakovany fragment "+(actualFragment + 1)+"-----------------\n");
				watch.start();

				// vlozenie fragmentu do bajtoveho pola
				try {
					data = listFragmentov.get(actualFragment).getBytes("UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				// zistujem ci je posledny fragment - ci posielany fragment je
				// posledny v zozname fragmentov
				if (actualFragment == listFragmentov.size() - 1)
					last = 1;

				// pocitanie paritneho kodu datovej casti
				ParityChecksum.getSingletonObject()
						.update(data, 0, data.length);
				dataParity = (int) ParityChecksum.getSingletonObject()
						.getValue();

				// spocitanie detekcneho kodu z hlavicky a datovej casti
				ckc = (dataParity + ack + last + repeated) % 2;

				// /generovanie chybneho detekcneho kodu
				Random random = new Random();
				int pick = random.nextInt(100);
				if (pick < NAHODNOST_CHYBY) {
					ckc = (ckc + 1) % 2;
					watch.stop();
					textArea.append("DETEKCNY KOD BOL VYTVORENY S CHYBOU\n");
					watch.start();
					vysledok.pocetNespravnychFragmentov++;
				} else
					vysledok.pocetSpravnychFragmentov++;

				// vlozenie flagov do hlavicky, vlozenie hlavicky a dat do
				// fragmentu
				header = FunctionsLogic.createHeader(ckc, repeated, last, ack);
				fragment = new byte[header.length + data.length];
				System.arraycopy(header, 0, fragment, 0, header.length);
				System.arraycopy(data, 0, fragment, 1, data.length);

				vysledok.pocetVyslanychB += fragment.length;
				vysledok.preneseneBvRezii += 46;

				// POSIELANIE FRAGMENTU
				try {
					dgpPosielanie = new DatagramPacket(fragment, 0,
							fragment.length);
					sockPosielanie.connect(
							InetAddress.getByName(cielovaAdresa), 27001);
					sockPosielanie.send(dgpPosielanie);

					watch.stop();
					textArea.append("Fragment bol odoslany na adresu: "
							+ cielovaAdresa + ", velkost: " + fragment.length
							+ "\n");
					textArea.append("Velkost: " + dgpPosielanie.getLength()
							+ " ACK:" + ack + " LAST fragment:" + last
							+ " Detekcny kod:" + ckc + " Repeated fragment:"
							+ repeated + "\n");
					watch.start();

				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// PRIJIMANIE POTVRDENIA
				while (true) {
					try {
						sockPrijimanie.receive(dtgram);
					} catch (IOException e) {

						// ak casovy limit je prekroceny, opakuje sa odosielanie
						// fragmentu
						repeated = 1;
						pocetOpakovani++;

						watch.stop();
						textArea.append("Timeout na prijatie odpovede vyprsal\n");
						watch.start();

						// Limit na pocet pokusov pri neodpovedani
						if (pocetOpakovani == LIMIT_NA_POCET_OPAKOVANI) {
							watch.stop();
							textArea.append("Posielanie niekolkych opakovanych vzoriek neuspesne, prijemca neodpoveda potvrdenim\n");
							watch.start();
							failed = true;
							finished = true;
							break;
						}
						break;
					}

					// ziskanie flagov z hlavicky potvrdenia
					int ackHeader = Integer.parseInt(
							FunctionsLogic.byteToHex(dtgram.getData()[0]), 16);
					prijatyHeader = String.format("%8s", Integer
							.toBinaryString(ackHeader).replaceAll(" ", "0"));

					watch.stop();
					textArea.append("Detekcny kod potvrdenia: "+Integer.parseInt(Character.toString(prijatyHeader.charAt(0)))+", Vypocitany detekcny kod potvrdenia: "+(Integer.parseInt(Character
							.toString(prijatyHeader.charAt(1)))
							+ Integer.parseInt(Character.toString(prijatyHeader
									.charAt(2))) + Integer.parseInt(Character
							.toString(prijatyHeader.charAt(3)))) % 2+"\n");
					watch.start();
					
					// pocitanie detekcneho kodu z hlavicky
					if (Integer.parseInt(Character.toString(prijatyHeader
							.charAt(0))) != (Integer.parseInt(Character
							.toString(prijatyHeader.charAt(1)))
							+ Integer.parseInt(Character.toString(prijatyHeader
									.charAt(2))) + Integer.parseInt(Character
							.toString(prijatyHeader.charAt(3)))) % 2) {
						watch.stop();
						textArea.append("Prijate poskodene potvrdenie");
						watch.start();
						repeated = 1;
						pocetOpakovani++;
					}

					// zistovanie ci ack ma hodnotu 1
					if ((ackHeader & 16) == 16) {
						watch.stop();
						textArea.append("Potvrdenie bolo prijate, zdrojova adresa: "
								+ dtgram.getAddress().toString()
										.replaceAll("/", "") + "\n");
						watch.start();
					} else {
						watch.stop();
						textArea.append("Potvrdenie nebolo prijate\n");
						watch.start();
						repeated = 1;
						pocetOpakovani++;
					}

					vysledok.pocetACK++;
					vysledok.preneseneBvRezii += 46;

					// Ak sa prijalo potvrdenie po opkaovanom fragmente, tak
					// vynulujem pocitadla
					if (repeated == 1) {
						pocetOpakovani = 0;
						repeated = 0;
					}
					textArea.append("-----------------koniec fragmentu "+(actualFragment + 1)+"----------------\n");
					actualFragment++;
					if (actualFragment == listFragmentov.size())
						finished = true;
					break;

				}
				
			}
			// KONIEC ITERACIE FOR CYKLU

			// vysledok.
			watch.stop();
			if (failed != true) {
				if (canceled != true) {

					vysledok.celkovy_cas = watch.getElapsedTime();

					textArea.append("______________KONIEC VZORKY CISLO "
							+ (i + 1) + "__________________________\n");
					textArea.append("Pocet správnych fragmentov:"
							+ vysledok.pocetSpravnychFragmentov
							+ ", Pocet nesprávnych fragmentov:"
							+ vysledok.pocetNespravnychFragmentov
							+ ", Pocet prijatych ACK:" + vysledok.pocetACK
							+ "\n");
					textArea.append("_____________________________________________________________________\n");

					vysledky.add(vysledok);
				} else {
					i = pocetVzoriek;
					textArea.append("Prenos ukonèený používate¾om na strane vysielaèa\n");
				}

			} else {
				i = pocetVzoriek;
				textArea.append("Prijimac neodpoveda, posielanie ukoncene\n");
			}
		}
		if (failed != true && canceled != true) {
			int pocetAck = 0;
			long pocetodoslanychB = 0;
			long celkovaRezia = 0;

			for (Vysledok v : vysledky) {
				globalTime += v.celkovy_cas;
				pocetAck += v.pocetACK;
				pocetodoslanychB += v.pocetVyslanychB;
				celkovaRezia += v.preneseneBvRezii;
			}
			textArea.append("============================================================================\n");
			textArea.append("Celkovy cas odosielania vzoriek - "
					+ (float) ((globalTime) / 1000000L) + " milisekund\n");
			textArea.append("Velkost odosielanej vzorky - " + velkostVzorky
					+ " B\n");
			textArea.append("Priemerný èas potrebný na prenesenie vzorky (aplikaèné dáta) - "
					+ ((float) ((globalTime / ((long) pocetVzoriek))) / 1000000)
					+ " milisekund\n");

			textArea.append("Priemerný èas potrebný na prenesenie 1 B vzorky - "
					+ ((float) (globalTime / ((long) velkostVzorky * (long) pocetVzoriek)) / 1000)
					+ " \u00B5sekund\n");
			textArea.append("Priemerný èas potrebný, vo všeobecnosti, na prenesenie 1 B po médiu - "
					+ (globalTime
							/ ((float) (((long) pocetodoslanychB
									+ (long) celkovaRezia + (long) pocetAck))) / 1000)
					+ " \u00B5sekund\n");

			textArea.append("============================================================================\n");
			System.out.println(globalTime);
		}

		sockPrijimanie.close();
		sockPosielanie.close();
		label.setText("Pripravené");
	}
}
