/**
 * 
 */
package com.lars_albrecht.moviedb.database;

import java.awt.Image;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lars_albrecht.general.utilities.Debug;
import com.lars_albrecht.general.utilities.Helper;

/**
 * @author lalbrecht
 * 
 */
public class DB {

	private static Connection connection = null;

	private final static String dbUsername = "moviedb";
	private final static String dbPassword = "mypw";
	private final static String dbName = "moviedb";
	private final static String dbUrl = "jdbc:h2:";
	private final static String Strdb = "org.h2.Driver";
	private final static String dboptions = "";

	/**
	 * Returns the DB connection.
	 * 
	 * @return the connection
	 * @throws SQLException
	 */
	public static synchronized final Connection getConnection() throws SQLException {
		if((DB.connection == null) || !DB.connection.isValid(500) || DB.connection.isClosed() || DB.connection.isReadOnly()) {
			DB.connection = DB.createConnection();
		}
		return DB.connection;
	}

	/**
	 * Create a DB connection.
	 * 
	 * @return Connection
	 */
	private static Connection createConnection() {
		Connection con = null;
		try {
			Class.forName(DB.Strdb);
			con = DriverManager.getConnection(DB.dbUrl + DB.dbName + DB.dboptions, DB.dbUsername, DB.dbPassword);

		} catch(final ClassNotFoundException e) {
			System.err.println("Driver not found \"" + e.getMessage() + "\"");
			return null;
		} catch(final SQLException e) {
			e.printStackTrace();
		}

		return con;
	}

	/**
	 * Close the DB connection.
	 * 
	 * @throws SQLException
	 */
	public static void closeConnection() throws SQLException {
		if(DB.connection != null) {
			DB.connection.close();
		}
	}

	public static void commit() throws SQLException {
		if(DB.connection != null) {
			Debug.log(Debug.LEVEL_DEBUG, "DB COMMIT");
			DB.connection.commit();
		}
	}

	/**
	 * use for SQL command SELECT
	 * 
	 * @param expression
	 * @throws SQLException
	 */
	public static synchronized ResultSet query(final String expression) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		st = DB.getConnection().createStatement(); // statement objects can be
		// reused with

		// repeated calls to execute but we
		// choose to make a new one each time
		// System.out.println(expression);
		rs = st.executeQuery(expression); // run the query

		// do something with the result set.

		// st.close(); // NOTE!! if you close a statement the associated
		// ResultSet
		// is
		return rs;
		// closed too
		// so you should copy the contents to some other object.
		// the result set is invalidated also if you recycle an Statement
		// and try to execute some other query before the result set has been
		// completely examined.
	}

	/**
	 * use for SQL command SELECT
	 * 
	 * @param expression
	 * @param values
	 * @return ResultSet
	 * @throws SQLException
	 */
	public static synchronized ResultSet queryPS(final String expression, final ConcurrentHashMap<Integer, Object> values)
			throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		st = DB.getConnection().prepareStatement(expression);
		for(final Map.Entry<Integer, Object> entry : values.entrySet()) {
			// st = DB.addDynamicValue(st, entry.getKey(), entry.getValue()); // TODO check why it will not save lists with this -> see logs with image org.h2.jdbc.JdbcSQLException: Serialisierung
			// fehlgeschlagen, Grund: "java.io.NotSerializableException: sun.awt.image.ToolkitImage" - Serialization failed, cause: "java.io.NotSerializableException: sun.awt.image.ToolkitImage"
			// [90026-162]
			st.setObject(entry.getKey(), entry.getValue());
			// st = DB.addDynamicValue(st, entry.getKey(), entry.getValue());
		}

		rs = st.executeQuery();

		return rs;
	}

	/**
	 * use for SQL commands CREATE, DROP, INSERT, UPDATE and ALTER
	 * 
	 * @param expression
	 * @throws SQLException
	 */
	public static synchronized void update(final String expression) throws SQLException {
		Statement st = null;
		st = DB.getConnection().createStatement(); // statements
		final int i = st.executeUpdate(expression); // run the query
		System.out.println(expression);
		if(i == -1) {
			Debug.log(Debug.LEVEL_ERROR, "db error : " + expression);
		}

		st.close();
	}

	/**
	 * use for SQL commands CREATE, DROP, INSERT and UPDATE
	 * 
	 * @param expression
	 * @param values
	 * @throws SQLException
	 */
	public static synchronized void updatePS(final String expression, final ConcurrentHashMap<Integer, Object> values)
			throws SQLException {
		PreparedStatement st = null;
		st = DB.getConnection().prepareStatement(expression); // statements
		for(final Map.Entry<Integer, Object> entry : values.entrySet()) {
			st = DB.addDynamicValue(st, entry.getKey(), entry.getValue());
		}

		// System.out.println(expression);
		// System.out.println(values);
		final int result = st.executeUpdate(); // run the query
		if(result == -1) {
			Debug.log(Debug.LEVEL_ERROR, "db error : " + expression);
		}

		st.close();
	}

	/**
	 * Sets a dynamic object in a PreparedStatement.
	 * 
	 * @param pst
	 *            PreparedStatement
	 * @param index
	 *            int
	 * @param objectToSet
	 *            Object
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	private static PreparedStatement addDynamicValue(final PreparedStatement pst, final int index, final Object objectToSet)
			throws SQLException {
		if(objectToSet.getClass() == Integer.class) {
			pst.setInt(index, ((Integer) objectToSet).intValue());
		} else if(objectToSet.getClass() == String.class) {
			pst.setString(index, ((String) objectToSet).substring(1, ((String) objectToSet).length() - 1));
		} else if(objectToSet instanceof Image) {
			pst.setBytes(index, Helper.getBytesFromImage((Image) objectToSet));
		} else if(objectToSet.getClass() == Boolean.class) {
			pst.setBoolean(index, ((Boolean) objectToSet));
		} else if(objectToSet.getClass() == Float.class) {
			pst.setFloat(index, ((Float) objectToSet).floatValue());
		} else if(objectToSet.getClass() == Long.class) {
			pst.setLong(index, ((Long) objectToSet).longValue());
		} else if(objectToSet.getClass() == Byte.class) {
			pst.setByte(index, ((Byte) objectToSet).byteValue());
		} else if(objectToSet.getClass() == Short.class) {
			pst.setShort(index, ((Short) objectToSet).shortValue());
		} else if(objectToSet.getClass() == BigDecimal.class) {
			pst.setBigDecimal(index, ((BigDecimal) objectToSet));
		} else if(objectToSet.getClass() == Double.class) {
			pst.setDouble(index, ((Double) objectToSet).shortValue());
		} else if(objectToSet.getClass() == Time.class) {
			pst.setTime(index, ((Time) objectToSet));
		} else if(objectToSet.getClass() == Date.class) {
			pst.setDate(index, ((Date) objectToSet));
		} else if(objectToSet.getClass() == Timestamp.class) {
			pst.setTimestamp(index, ((Timestamp) objectToSet));
		} else if(objectToSet.getClass() == byte[].class) {
			pst.setBytes(index, ((byte[]) objectToSet));
		} else if(objectToSet.getClass() == Object[].class) {
			pst.setArray(index, (Array) Arrays.asList(((Object[]) objectToSet)));
		}
		return pst;
	}

	/**
	 * 
	 * @param rs
	 *            ResultSet
	 * @throws SQLException
	 */
	public static void dump(final ResultSet rs) throws SQLException {

		// the order of the rows in a cursor
		// are implementation dependent unless you use the SQL ORDER statement
		final ResultSetMetaData meta = rs.getMetaData();
		final int colmax = meta.getColumnCount();
		int i;
		Object o = null;

		// the result set is a cursor into the data. You can only
		// point to one row at a time
		// assume we are pointing to BEFORE the first row
		// rs.next() points to next row and returns true
		// or false if there is no next row, which breaks the loop
		for(; rs.next();) {
			for(i = 0; i < colmax; ++i) {
				o = rs.getObject(i + 1); // Is SQL the first column is indexed

				// with 1 not 0
				Debug.log(Debug.LEVEL_DEBUG, o.toString() + " ");
			}

			Debug.log(Debug.LEVEL_DEBUG, " ");
		}
	}

	/**
	 * 
	 * @throws SQLException
	 */
	public static void shutdown() throws SQLException {

		final Statement st = DB.getConnection().createStatement();

		// db writes out to files and performs clean shuts down
		// otherwise there will be an unclean shutdown
		// when program ends
		st.execute("SHUTDOWN");
		DB.connection.close(); // if there are no other open connection
	}

	/**
	 * 
	 * @return ArrayList<String>
	 * @throws SQLException
	 */
	public static ArrayList<String> getTables() throws SQLException {
		final ResultSet rs = DB.query("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC';");
		final ArrayList<String> temp = new ArrayList<String>();
		for(; rs.next();) {
			temp.add(rs.getString("TABLE_NAME"));
		}
		DB.closeConnection();

		return temp;
	}

	/**
	 * 
	 * @param sql
	 * @param fields
	 * @return ArrayList<ConcurrentHashMap<String, Object>>
	 * @throws SQLException
	 */
	public static ArrayList<ConcurrentHashMap<String, Object>> returnResultFromItems(final String sql,
			final ArrayList<String> fields) throws SQLException {
		final ArrayList<ConcurrentHashMap<String, Object>> resultSet = new ArrayList<ConcurrentHashMap<String, Object>>();
		final ResultSet rs = DB.query(sql);
		for(; rs.next();) {
			final ConcurrentHashMap<String, Object> temp = new ConcurrentHashMap<String, Object>();
			for(final String string : fields) {
				temp.put(string, rs.getObject(string));
			}
			resultSet.add(temp);
		}
		return resultSet;
	}

	/**
	 * 
	 * @param tableName
	 * @return ArrayList<String>
	 * @throws SQLException
	 */
	public static ArrayList<String> getColumnsFromTable(final String tableName) throws SQLException {
		final ResultSet rs = DB.query("SHOW COLUMNS FROM " + tableName);
		final ArrayList<String> tempList = new ArrayList<String>();
		while(rs.next()) {
			tempList.add(rs.getString("COLUMN_NAME"));
		}

		return tempList;
	}

}