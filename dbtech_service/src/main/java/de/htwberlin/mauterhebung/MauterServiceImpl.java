package de.htwberlin.mauterhebung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.exceptions.AlreadyCruisedException;
import de.htwberlin.exceptions.DataException;
import de.htwberlin.exceptions.InvalidVehicleDataException;
import de.htwberlin.exceptions.UnkownVehicleException;

/**
 * Die Klasse realisiert den AusleiheService.
 * 
 * @author Patrick Dohmeier
 */
public class MauterServiceImpl implements IMauterhebung {

	private static final Logger L = LoggerFactory.getLogger(MauterServiceImpl.class);
	private Connection connection;
	private boolean manuelVerfahren = false;

	private long fz_id;
	private int kategorie_id;
	
	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	private Connection getConnection() {
		if (connection == null) {
			throw new DataException("Connection not set");
		}
		return connection;
	}

	@Override
	public float berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
			throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {
		
		L.info("Start berechneMaut");
		
		manuelVerfahren = false;
		Fahrzeug f = new Fahrzeug();
		Buchung b = new Buchung();
		Mauterhebung m = new Mauterhebung();
		
		//first check if fahrzeug exists
		if(checkObFahrzeugExist(kennzeichen, b)) {
			//second check if Achsenanzahl is correct
			if(checkIfAchsenAnzahlIsCorrect(achszahl, kennzeichen, f)) {
				//third check if Verfahren is manuell or automatisch
				if(m.checkIfVerfahrenIsManuell(kennzeichen, getConnection())) {
					//Verfahren is manuell
					if(b.checkIfStatusIsOffen(kennzeichen, mautAbschnitt, getConnection())) {
						//status is offen
						b.changeStatusToClosed(getConnection(), kennzeichen);
						
					} else {
						//status is geschlossen
						throw new AlreadyCruisedException("Booking already cruised for verhicle: " + kennzeichen);
					}
				} else {
					//Verfahren is automatisch
					return m.calculateMaut(kennzeichen, achszahl, mautAbschnitt, f, getConnection(), kategorie_id, fz_id, f.getSSKL_ID(kennzeichen, getConnection()));
				}
			} else {
				
				throw new InvalidVehicleDataException("Invalid Achzahl: " + kennzeichen);
			}
		} else {
			 throw new UnkownVehicleException("vehicle doesn't exist: " + kennzeichen);	
		 }
		
		L.info("End berechneMaut");	
		
		
		return 0;
	}
	
	
	
	
	public boolean checkIfAchsenAnzahlIsCorrect(int achszahl, String kennzeichen, Fahrzeug f) {
		
		
		L.info("Start checkIfAchsenAnzahlIsCorrect");
		
		//will be checked antoher way in manuellem verfahren
		if(manuelVerfahren == false) return f.getAchszahlManuel(getConnection(), kennzeichen) == achszahl;
			else return f.getAchzahlAuto(getConnection(), kennzeichen, achszahl);
		
	}

	
	public boolean checkObFahrzeugExist(String kennzeichen, Buchung b) {
		
		L.info("Start checkObFahrzeugExist");
		
		String sqlCheckObFahrzeugExists = String.join(" ", 
				"select * from fahrzeug",
				"where kennzeichen = ?");
		L.info(sqlCheckObFahrzeugExists);
		
		try(PreparedStatement ps = getConnection().prepareStatement(sqlCheckObFahrzeugExists)) {
			ps.setString(1, kennzeichen);			
			try(ResultSet rs = ps.executeQuery()){
				 if(rs.next()) {
					 //return true if exists
					 fz_id = rs.getLong("fz_id");
					 return true;
				 } else {
					 //second chanche: check if vehicle exists with (nicht offene) Buchung -> return true
					 //false if not
					 if(b.checkIfThereAreClosedBuchungen(kennzeichen, getConnection())) {
						 manuelVerfahren = true;
						 return true;
					 } else return false;
					 
					 
				 }
			}
		} catch (SQLException e) {
			throw new DataException("checkObFahrzeugExist "+e);
		} 
		
	}
	



	
}
