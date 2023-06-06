package de.htwberlin.mauterhebung;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import de.htwberlin.exceptions.DataException;

public class Buchung {
	
	
	private static final Logger L = LoggerFactory.getLogger(MauterServiceImpl.class);

	public void getAchszahl() {

	}

	public void changeStatusToClosed(Connection connection, String kennzeichen) {
	
		L.info("Start closeBuchungAndChangeBefahrungszeitraum");
		
		String sql = String.join(" ", 
				"update buchung",
				"set b_id = 3",
				//Beispieldatum
				//TODO: echtes Datum
				", befahrungsdatum = ?",
				"where kennzeichen = ?"
				);
		
		try(PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, "18.02.15 17:25:00,000000000");
			ps.setString(2, kennzeichen);
			ps.executeQuery();
			
		}catch(SQLException e) {
			throw new DataException(e);
		}
	}

	public void update() {

	}

	
public boolean checkIfStatusIsOffen(String kennzeichen, int abschnitts_id, Connection connection) {
		
		boolean offeneBuchung = false;
		
		L.info("Start checkIfStatusIsOffen");
		
		String sqlCheckIfVerfahrenIsOffen = String.join(" ", 
				"select * from buchung",
				"where kennzeichen = ?",
				"and abschnitts_id = ?"
				
				);
		L.info(sqlCheckIfVerfahrenIsOffen);

		
		try(PreparedStatement ps = connection.prepareStatement(sqlCheckIfVerfahrenIsOffen)) {
			ps.setString(1, kennzeichen);		
			ps.setInt(2, abschnitts_id);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					int status = rs.getInt("b_id");
					
					if(status == 1) offeneBuchung = true;
				}
				return offeneBuchung;
				 
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} 
		
		
	}

public boolean checkIfThereAreClosedBuchungen(String kennzeichen, Connection connection) {
	
	L.info("Start checkIfThereAreClosedBuchungen");
	
	String sql = String.join(" ", 
			"select * from buchung",
			"where kennzeichen = ?"
			
			);
	
	try(PreparedStatement ps = connection.prepareStatement(sql)) {
		ps.setString(1, kennzeichen);			
		try(ResultSet rs = ps.executeQuery()){	
			while(rs.next()) {
				int status = rs.getInt("b_id");
			
				if ((status == 2) || (status == 3)) {
					return true;
				}
			} 
			return false;
		}
	} catch (SQLException e) {
		throw new DataException(e);
	} 
	
}
}
