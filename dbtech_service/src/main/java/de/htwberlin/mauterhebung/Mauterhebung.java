package de.htwberlin.mauterhebung;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import de.htwberlin.exceptions.DataException;

public class Mauterhebung {
	
	private static final Logger L = LoggerFactory.getLogger(MauterServiceImpl.class);

	public void insert(int abschnitt_id, float kosten, Fahrzeug f, Connection connection, long fz_id, int kategorie_id) {
		
L.info("Start createNewDatenEintrag");
		
		String sql = String.join(" ", 
				"insert into mauterhebung",
				"(maut_id, abschnitts_id, fzg_id, kategorie_id, befahrungsdatum, kosten)",
				" values",
				"(?, ?, ?, ?, ?, ?)"
				);
		
		try(PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, 1018);
			ps.setInt(2, abschnitt_id);
			ps.setLong(3, f.getFzg_id(connection, fz_id));
			ps.setInt(4, kategorie_id);
			ps.setString(5, "18.02.15 17:25:00,000000000");
			ps.setFloat(6, kosten);
			try(ResultSet rsResultSet = ps.executeQuery()){
				
			} catch (SQLException e) {
				throw new DataException("getMautsatzJeKm "+e);
			}

			
		}catch(SQLException e) {
			throw new DataException(e.getCause());
		}
		
		
		
	}
	
	
	public float calculateMaut(String kennzeichen, int achsZahl, int mautAbschnitt, Fahrzeug f, Connection connection, int kategorie_id, long fz_id, int sskl_id) {
		
		L.info("Start calculateMaut");
		
		float mautsatzJeKm = f.getMautsatzProKM(sskl_id, achsZahl, connection, kategorie_id);
		
		String sql = String.join(" ", 
				"select laenge from mautabschnitt",
				"where abschnitts_id = ?"
				);

		
		try(PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setInt(1, mautAbschnitt);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					
				//format mausatz
				DecimalFormat df_3 = new DecimalFormat("#.###");
				String s = df_3.format(mautsatzJeKm);
				float finalFloat = Float.parseFloat(s.replaceAll(",", "."));
				
				//format laenge
				float laenge = (float)rs.getInt("laenge")/1000f;
				
				//round and format final Mautsatz
				float maut = laenge * finalFloat;
				DecimalFormat df_2 = new DecimalFormat("0.00");
				String s_2 = df_2.format(maut);
				float finalMautRechnung = Float.parseFloat(s_2.replaceAll(",", "."));
				
				insert(mautAbschnitt, finalMautRechnung, f, connection, fz_id, kategorie_id);
				return finalMautRechnung;
				}
				throw new DataException("");
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} 
			
	}
	
	
	public boolean checkIfVerfahrenIsManuell(String kennzeichen, Connection connection) {
		
		L.info("Start checkIfVerfahrenIsManuell");
		
		String sqlCheckIfVerfahrenIsManuell = String.join(" ", 
				"select * from buchung",
				"where kennzeichen = ?"
				
				);
		L.info(sqlCheckIfVerfahrenIsManuell);

		
		try(PreparedStatement ps = connection.prepareStatement(sqlCheckIfVerfahrenIsManuell)) {
			ps.setString(1, kennzeichen);			
			try(ResultSet rs = ps.executeQuery()){
				 if(rs.next()) {
					 //if Eintrag unter Tabelle Buchung exists -> Verfahren is manuell
					 return true;
				 } else {
					 return false;
				 }
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} 
		
	}
	
	
}
