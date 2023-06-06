package de.htwberlin.mauterhebung;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import de.htwberlin.exceptions.DataException;

public class Fahrzeug {

	private static final Logger L = LoggerFactory.getLogger(MauterServiceImpl.class);

	public int getAchszahlManuel(Connection connection, String kennzeichen) {

		L.info("Start checkIfAchsenAnzahlIsCorrect im automatischen Verfahren");

		String sqlCheckIfAchsenAnzahlIsCorrect = String.join(" ", "select * from fahrzeug", "where kennzeichen = ?");
		L.info(sqlCheckIfAchsenAnzahlIsCorrect);

		try (PreparedStatement ps = connection.prepareStatement(sqlCheckIfAchsenAnzahlIsCorrect)) {
			ps.setString(1, kennzeichen);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int actualAchsenZahl = rs.getInt("achsen");
					return actualAchsenZahl;
				} else {
					throw new DataException();
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}

	}

	public float getMautsatzProKM(int sskl_id, int achszahl, Connection connection, int kategorie_id) {

		L.info("Start getMausatzJeKm");

		String sql = String.join(" ", "select * from mautkategorie", "where sskl_id = ?");

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, sskl_id);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {

					char systemAchszahl = rs.getString("achszahl").charAt(rs.getString("achszahl").length() - 1);
					int systemAchszahlInt = Character.getNumericValue(systemAchszahl);

					char operator = rs.getString("achszahl").charAt(0);
					String operatorString = String.valueOf(operator);

					if (operatorString.equals("=") && systemAchszahlInt == achszahl) {
						kategorie_id = rs.getInt("kategorie_id");
						return rs.getFloat("mautsatz_je_km") / 100;
					} else if (operatorString.equals(">") && systemAchszahlInt <= achszahl) {
						kategorie_id = rs.getInt("kategorie_id");
						return rs.getFloat("mautsatz_je_km") / 100;
					}
					// gibt es nicht aktuell, aber falls es dazu kommt
					else if (operatorString.equals("<") && systemAchszahlInt >= achszahl) {
						kategorie_id = rs.getInt("kategorie_id");
						return rs.getFloat("mautsatz_je_km") / 100;
					}

				}
				throw new DataException("Da stimmt was nicht");
			}
		} catch (SQLException e) {
			throw new DataException("getMautsatzJeKm " + e);
		}

	}

	public long getFzg_id(Connection connection, long fz_id) throws SQLException {
		String sql = String.join(" ", "select fzg_id from fahrzeuggerat", "where fz_id = ?"

		);

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, fz_id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong("fzg_id");
				}
				throw new DataException();
			} catch (SQLException e) {
				throw new DataException("getFzg_id " + e);

			}

		}

	}

	public int getSSKL_ID(String kennzeichen, Connection connection) {
		String sql = String.join(" ", "select sskl_id from fahrzeug", "where kennzeichen = ?"

		);

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, kennzeichen);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int sskl_id = rs.getInt("sskl_id");
					return sskl_id;
				} else {
					throw new DataException("Da stimmt was nicht");
				}
			}
		} catch (SQLException e) {
			throw new DataException("getSSKL_ID " + e);
		}

	}

	public boolean getAchzahlAuto(Connection connection, String kennzeichen, int achszahl) {

		L.info("Start checkIfAchsenAnzahlIsCorrect im manuellen Verfahren");

		String sql_1 = String.join(" ", "select kategorie_id, b_id from buchung", "where kennzeichen = ?");

		try (PreparedStatement ps = connection.prepareStatement(sql_1)) {
			ps.setString(1, kennzeichen);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {

					int k_id = rs.getInt("kategorie_id");
					int b_id = rs.getInt("b_id");

					String sql_2 = String.join(" ", "select achszahl from mautkategorie", "where kategorie_id = ?");

					try (PreparedStatement ps_2 = connection.prepareStatement(sql_2)) {
						ps_2.setInt(1, k_id);
						try (ResultSet rs_2 = ps_2.executeQuery()) {
							boolean validAchszahl = false;
							while (rs_2.next()) {
								char systemAchszahl = rs_2.getString("achszahl")
										.charAt(rs_2.getString("achszahl").length() - 1);
								int systemAchszahlInt = Character.getNumericValue(systemAchszahl);

								char operator = rs_2.getString("achszahl").charAt(0);
								String operatorString = String.valueOf(operator);

								if (operatorString.equals("=") && systemAchszahlInt == achszahl)
									validAchszahl = true;
								else if (operatorString.equals(">") && systemAchszahlInt <= achszahl)
									validAchszahl = true;
								// gibt es nicht aktuell, aber falls es dazu kommt
								else if (operatorString.equals("<") && systemAchszahlInt >= achszahl)
									validAchszahl = true;
							}

							return validAchszahl;
						}

					}
				}
				return false;

			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public boolean checkObFahrzeugExist(String kennzeichen, Buchung b, Connection connection,
			boolean manuel) {

		L.info("Start checkObFahrzeugExist");

		String sqlCheckObFahrzeugExists = String.join(" ", "select * from fahrzeug", "where kennzeichen = ?");
		L.info(sqlCheckObFahrzeugExists);

		try (PreparedStatement ps = connection.prepareStatement(sqlCheckObFahrzeugExists)) {
			ps.setString(1, kennzeichen);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					// return true if exists
					return true;
					// second chanche: check if vehicle exists with (nicht offene) Buchung -> return
					// true
					// false if not
				} else
					return b.checkIfThereAreClosedBuchungen(kennzeichen, connection);

			}
		} catch (SQLException e) {
			throw new DataException("checkObFahrzeugExist " + e);
		}

	}

	public long getFz_id(Connection connection, String kennzeichen) {
		L.info("Start checkObFahrzeugExist");

		String sqlCheckObFahrzeugExists = String.join(" ", "select * from fahrzeug", "where kennzeichen = ?");
		L.info(sqlCheckObFahrzeugExists);

		try (PreparedStatement ps = connection.prepareStatement(sqlCheckObFahrzeugExists)) {
			ps.setString(1, kennzeichen);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					// return true if exists
					long fz_id = rs.getLong("fz_id");
					return fz_id;
				}else {
					throw new DataException();
				}
			}
		} catch (SQLException e) {
			throw new DataException("checkObFahrzeugExist " + e);
		}
	}

	public void getFahrzeugGeraetId() {

	}

	public void getMautkategorieId() {

	}

}
