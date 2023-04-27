package it.polito.tdp.meteo.model;

import java.util.*;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private MeteoDAO meteoDAO;
	
	List<Citta> listaCitta;
	List<Citta> soluzione;
	
	public Model() {
		this.meteoDAO = new MeteoDAO();
		
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		String s = "";
		List<Rilevamento> ril = meteoDAO.getAllRilevamentiLocalitaMese(mese, null);
		for (Rilevamento r: ril) {
			s += r.getLocalita()+" "+r.getUmidita() + "\n";
		}
		return s;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		this.listaCitta = new ArrayList<Citta>();
		this.soluzione = new ArrayList<Citta>();
		List<Rilevamento> ril = this.meteoDAO.getAllRilevamentiMese(mese);
		for (Rilevamento r: ril) {
			boolean flag = true;
			for (Citta c: listaCitta) {
				if (r.getLocalita().equals(c.getNome()))
					flag=false;
			}
			if(flag == true)
				listaCitta.add(new Citta(r.getLocalita()));
		}
		List<Citta> parziale = new ArrayList<Citta>();
		cerca(parziale, mese);
		String sol = "";
		for(Citta c: soluzione) {
			sol += c.getNome() + "\n";
		}
		return sol;
	}
	
	public void cerca(List<Citta> parziale, int mese) {
		//CONDIZIONE DI TERMINAZIONE 
		if (parziale.size() == 15) {
			//calcoliamo il costo
			//se il costo è minore del costo che è gia in soluzione 
			double costoMigliore = this.calcolaCosto(soluzione, mese);
			double costoParziale = this.calcolaCosto(parziale, mese);
			if(costoParziale<costoMigliore) {
				soluzione.clear();
				soluzione.addAll(parziale);
			}
			return;
		}
		//ALTRIMENTI SI VA AVANTI A COSTRUIRE LA SOLUZIONE PARZIALE
	
		for(Citta c:listaCitta) {
			parziale.add(c);//inseriamo la prima citta
			if(controlloCondizioni(parziale)) {
				cerca(parziale, mese);
			}
			parziale.remove(parziale.size()-1);//tolgo l'ultima citta messa BACKTRAKING
		}
	}
	
	private boolean controlloCondizioni(List<Citta> parziale) {
		boolean corretto = true; //se una delle condizioni non è verificata diventa false
		for(Citta c: listaCitta) {
			int contatore = 0;
			for (Citta cit: parziale) {
				if (c.getNome().equals(cit.getNome())){
					contatore ++;
				}
			}
			if (contatore > 6) {
				corretto = false;
			}
		}
		
		String precedente = parziale.get(0).getNome();
		int cont = 0;
		for (Citta c: parziale) {
			if(precedente.compareTo(c.getNome())!=0) {
				if (cont<3) {
					corretto = false;
				}
				cont = 1;
						precedente = c.getNome();
			}
			else {
				cont++;
			}
		}
		return corretto;
	}

	public Double calcolaCosto(List<Citta> listaDaValutare, int mese) {
		//all'inizio della ricorsione soluzione è vuota: per riempirla dobbiamo considerare che il primo parziale che trovermo 
		//dovrà per forza essere salvato in soluzione e quindi diciamo che il costo di una lista vuota sia il massimo possibile
		if(listaDaValutare.size()==0) {
			return Double.MAX_VALUE; //double piu alto che posso avere
		}
		double costo = 0.0;
		Citta precedente = listaDaValutare.get(0);
		int i=0; //per salvare la posizione dell'elemento nella lista ( che coincide con il giorno)
		for(Citta c: listaDaValutare) {
			i++;
			if(c.getNome().equals(precedente.getNome())) {
				costo += meteoDAO.getUmidita(mese, i, c.getNome());
			}
			else {
				costo += meteoDAO.getUmidita(mese, i, c.getNome())+100;
				precedente=c;
			}
		}
		return costo;
	}
	
	

}
