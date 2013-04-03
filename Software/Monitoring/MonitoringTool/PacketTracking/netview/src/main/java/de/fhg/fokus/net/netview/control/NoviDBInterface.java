/**
*
* Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
* Copyright according to BSD License
* For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt
*
* @author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
* @author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
* @author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
* @author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
*
*/

package de.fhg.fokus.net.netview.control;

import de.fhg.fokus.net.ptapi.PtProbeStats;
import java.math.BigInteger;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoviDBInterface
{

private long uid		= 0;
private long hdid		= 0;
private long pdid		= 0;
private String host		= null;
private String port		= null;
private String db		= null;
private String user		= null;
private String password	= null;
private	Connection connection = null;
private final Logger logger = LoggerFactory.getLogger (getClass ());


public NoviDBInterface (String host, String port, String db, 
						String user, String password)
	throws SQLException, ClassNotFoundException
{
	this.host 	= host;
	this.port 	= port;
	this.db 	= db;
	this.user 	= user;
	this.password = password;
	
	try
	{
		Class.forName ("org.postgresql.Driver");
	} catch (ClassNotFoundException e) {
		logger.debug ("PostgreSQL JDBC Driver not found!");
		throw e;
	}

	this.connection = DriverManager.getConnection
	("jdbc:postgresql://" + host + ":" + port + "/" + db, user, password);

	// Creating the SQL tables if they doesn't exist already.
	createHopDelaysTable ();
	createPathDelaysTable ();
	createNodeStatsTable ();
}


protected void finalize() throws Throwable
{
	if (connection != null)
		connection.close();
}



private long getMaxIDfromDB (String tableName, String idColumn)
throws java.sql.SQLException
{
	if (this.connection == null) {
		throw new SQLException("not connected!");
	}

	// fetch the biggest id number (key) from the table
	String query = "SELECT max(" + idColumn + ") FROM " + tableName + ";";
	ResultSet res = this.connection.createStatement().executeQuery(query);

	if (res.next ()) {
		return res.getLong (1);
	} else {
		return 0;
	}
}


private void createHopDelaysTable () 
throws SQLException
{
	try
	{
		hdid = getMaxIDfromDB ("hop_delays", "hdid") + 1;
		logger.debug ("hop_delays-table exists.");
		logger.debug ("hop_delays-table: using as next hdid = " + (hdid));

	} catch (SQLException ex) {
		logger.debug (ex.toString());
		logger.debug ("Creating hop_delays-table ...");

		try
		{
			String update = "CREATE TABLE hop_delays (hdid bigint NOT NULL, "
				+ "id bigint, ts bigint, src bigint, dst bigint, "
				+ "num bigint, hits double precision, "
				+ "sumdelay bigint, sumbytes bigint, "
				+ "mindelay bigint, maxdelay bigint, "
				+ "CONSTRAINT hop_delays_pkey PRIMARY KEY (hdid) );";

			connection.createStatement().executeUpdate (update);

		} catch (SQLException ex1) {
			logger.debug
			("createHopDelaysTable-Error: Wasn't able to create hop_delays-table");
			throw ex1;
		}
	}
}


private void createPathDelaysTable ()
throws SQLException
{
	try
	{
		pdid = getMaxIDfromDB ("path_delays", "pdid") + 1;
		logger.debug ("path_delays-table exists.");
		logger.debug ("path_delays-table: using as next pdid = " + (pdid));

	} catch (SQLException ex)	{
		logger.debug (ex.toString());
		logger.debug ("Creating path_delays-table ...");

		try
		{
			String update = "CREATE TABLE path_delays (pdid bigint NOT NULL, "
				+ "id bigint, ts bigint, src bigint, dst bigint, "
				+ "num bigint, path text, "
				+ "sumdelay bigint, sumbytes bigint, "
				+ "mindelay bigint, maxdelay bigint, "
				+ "CONSTRAINT path_delays_pkey PRIMARY KEY (pdid) );";

			connection.createStatement().executeUpdate (update);
		} catch (SQLException ex1) {
			logger.debug
			("createPathDelaysTable-Error: Wasn't able to create path_delays-table");
			throw ex1;
		}
	}
}


private void createNodeStatsTable ()
throws SQLException
{
	try
	{
		uid = getMaxIDfromDB ("node_stats", "uid") + 1;
		logger.debug ("node_stats-table exists.");
		logger.debug ("node_stats-table: using as next pdid = " + (uid));

	} catch (SQLException ex) {
		logger.debug (ex.toString());
		logger.debug ("Creating node_stats-table ...");

		try
		{
			String update = "CREATE TABLE node_stats ( "
				+ "uid bigint NOT NULL, "
				+ "oid bigint, "
				+ "exporttime bigint,"
				+ "observationtimemilliseconds bigint, "
				+ "systemcpuidle double precision,"
				+ "systemmemfree bigint, "
				+ "processcpuuser double precision, "
				+ "processcpusys double precision, "
				+ "processmemvzs bigint,"
				+ "processmemrss bigint, "
				+ "CONSTRAINT node_stats_pkex PRIMARY KEY (uid) );";

			connection.createStatement().executeUpdate (update);
		}
		catch (SQLException ex1)
		{
			logger.debug
			("createNodeStatsTable-Error: Wasn't able to create node_stats-table");
			throw ex1;
		}
	}
}


public ResultSet getContent (String selection, String options, String table)
throws SQLException
{
	if (!(table.equals ("hop_delays")) && !(table.equals ("path_delays")))
	{
		logger.debug
		("getContent-Error: parameter table has unexpected content");
		return null;
	}

	try
	{
		String query   = "SELECT " + selection + 
						 " FROM " + table + " " + options + " ;";
		//logger.debug("getContent-query: "+query);
		return connection.createStatement().executeQuery (query);

	} catch (SQLException ex) {
		logger.debug (ex.toString());
		logger.debug
		("getContent-Error: Wasn't able to fetch content from database");
		throw ex;
	}
}


public void updateRow (long did, long sumdelay, long num, float hits,
			long sumbytes, long mindelay, long maxdelay,
			String table)
throws SQLException
{
	if (!(table.equals ("hop_delays")) && !(table.equals ("path_delays")))
	{
		logger.debug
		("updateRow-Error: request for table '"+table+"' not supported");
		return;
	}
	
	String update = "";
	if (table.equals ("hop_delays"))
	{
		update = "UPDATE hop_delays "
		+ "SET num = " + num
		+ ", hits = " + hits
		+ ", sumdelay = " + sumdelay
		+ ", sumbytes = " + sumbytes
		+ ", mindelay = " + mindelay
		+ ", maxdelay = " + maxdelay + " WHERE hdid = " + did + " ;";
	}
	else if (table.equals ("path_delays"))
	{
		update = "UPDATE path_delays "
		+ "SET num = " + num
		+ ", sumdelay = " + sumdelay
		+ ", sumbytes = " + sumbytes
		+ ", mindelay = " + mindelay
		+ ", maxdelay = " + maxdelay + " WHERE pdid = " + did + " ;";
	}

	logger.debug ("updateRow-Update: " + update);
    connection.createStatement().executeUpdate (update);
}


public void writeRow (long id, long timestamp, long src, long dst,
			long numberOfDelays, float hitcounter, String path,
			long sumDelays, long sumBytes, long mindelay,
			long maxdelay, String table)
throws SQLException
{
	if (table.equals ("hop_delays"))
	{
		hdid++;
	}
	else if (table.equals ("path_delays"))
	{
		pdid++;
	}
	else
	{
		logger.debug
		("writeRow-Error: request for table '"+table+"' not supported");
		return;
	}

	String update = "";
	if (table.equals ("hop_delays"))
	{
		update = "INSERT INTO hop_delays "
		+ "VALUES ( "
		+ hdid + ", "
		+ id + ", "
		+ timestamp + ", "
		+ src + ", "
		+ dst + ", "
		+ numberOfDelays + ", "
		+ hitcounter + ", "
		+ sumDelays + ", "
		+ sumBytes + ", " + mindelay + ", " + maxdelay + " " + ");";
	}
	else if (table.equals ("path_delays"))
	{
		update = "INSERT INTO path_delays "
		+ "VALUES ( "
		+ pdid + ", "
		+ id + ", "
		+ timestamp + ", "
		+ src + ", "
		+ dst + ", "
		+ numberOfDelays + ", "
		+ path + ", "
		+ sumDelays + ", "
		+ sumBytes + ", " + mindelay + ", " + maxdelay + " " + ");";
	}

	logger.debug ("writeRow-Update: " + update);
    connection.createStatement().executeUpdate (update);
}


public void exportNodeStats (PtProbeStats probeStats)
throws SQLException
{
	this.uid++;

	try
	{
		String update = "INSERT INTO node_stats "
			+ "VALUES ("
			+ uid + ", "
			+ probeStats.getOid() + ", "
			+ probeStats.getExportTime () + ", "
			+ probeStats.getObservationTimeMilliseconds() + ", "
			+ probeStats.getSystemCpuIdle() + ", "
			+ probeStats.getSystemMemFree() + ", "
			+ probeStats.getProcessCpuUser() + ", "
			+ probeStats.getProcessCpuSys() + ", "
			+ probeStats.getProcessMemVzs () + ", "
			+ probeStats.getProcessMemRss() + ");";

		logger.debug ("exportNodeStats-Update: " + update);
		connection.createStatement ().executeUpdate (update);
	}
	catch (SQLException ex)
	{
		logger.debug
		("exportNodeStats-Error: update of node_stats failed");
		throw ex;
	}
}

}
