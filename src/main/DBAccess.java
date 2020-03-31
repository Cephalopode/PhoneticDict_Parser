/*
 * First, create database as :
 * CREATE DATABASE dict CHARACTER SET utf16 COLLATE utf16_bin;
 * use dict;
 * create table french ( id INT NOT NULL AUTO_INCREMENT, word varchar(30) NOT NULL, phon varchar(30), trans varchar(30), def varchar(80), primary key (id) );
 * create table english ( id INT NOT NULL AUTO_INCREMENT, word varchar(30) NOT NULL, def varchar(80), primary key (id) );
 * migrate database to server :
 * mysqldump -u root --password=167 --host=localhost -C dict | ssh tangmu@13.68.217.80 "mysql -u root --password=167 dict"
 *  mysqldump -u root --password=167 --host=localhost -C dict > dict.mysql &&& do scp &&& ssh root@thomasparadis.me "docker exec -i dict-mysql mysql -p167 dict < dict.mysql"
* MYSQL CONNECTION PROBLEM
* https://stackoverflow.com/questions/50093144/mysql-8-0-client-does-not-support-authentication-protocol-requested-by-server
*
 * Char encoding problem : connect to database with "jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8"
 * 
 * IPA : https://en.wikipedia.org/wiki/Help:IPA_for_French
 * 
 * 38862 french
 * 42489 english
 */

package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class DBAccess {
        private Connection connect = null;
        private Statement statement = null;
        private PreparedStatement preparedStatement = null;
        private ResultSet resultSet = null;
        private String database = null;
        
        public void populate() throws Exception {
        	 try {
        	init("en");
        	addEntryEN("first_word", "firstDef");
        	
        	
        	 } catch (Exception e) {
                 throw e;
	         } finally {
	                 close();
	         }
        }

        public void init(String lang) throws Exception {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            //*** dict database, phonetics table (word, phon)
            connect = DriverManager
                            .getConnection("jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8","root", "167");
            //clear database
            if(lang=="fr")
            	database = "french";
            if(lang=="en")
            	database = "english";
            
            preparedStatement = connect
                    .prepareStatement("delete from dict." + database);
            preparedStatement.executeUpdate();

        }
        public void addEntryFR(String word, String phon, String trad, String def) throws SQLException {
        	if (word.length()>30) {
        		word = word.substring(0, 30);
        	}
        	if (phon.length()>30) {
        		phon = phon.substring(0, 30);
        	}
        	phon = phon.replace(".", "");
        	
        	preparedStatement = connect
                    .prepareStatement("insert into  dict." + database + " values (default, ?, ?, ?, ?)");
        	preparedStatement.setString(1, word);
            preparedStatement.setString(2, phon);
            preparedStatement.setString(3, trad);
            preparedStatement.setString(4, def);
            try {
            preparedStatement.executeUpdate();
            }
            catch(Exception e) {
            	System.out.print("Error at word :  ");
            	System.out.println(word + " " + phon + "  " +  phon.length());
            	e.printStackTrace();
            	System.exit(1);
            }
        }
        public void addEntryEN(String word, String def) throws SQLException {
        	if (word.length()>30) {
        		word = word.substring(0, 30);
        	}
        	
        	preparedStatement = connect
                    .prepareStatement("insert into  dict." + database + " values (default, ?, ?)");
        	preparedStatement.setString(1, word);
            preparedStatement.setString(2, def);
            //TRY ADDING MANY BATCHES (maybe 50 then executing)  TO OBTAIN FASTER SPEED
            preparedStatement.addBatch();
            try {
            preparedStatement.executeBatch();
            }
            catch(Exception e) {
            	System.out.print("Error at word :: def :  ");
            	System.out.println(word + " :: " + def);
            	e.printStackTrace();
            }
        }


        // You need to close the resultSet
        public void close() { //modified to public
                try {
                        if (resultSet != null) {
                                resultSet.close();
                        }

                        if (statement != null) {
                                statement.close();
                        }

                        if (connect != null) {
                                connect.close();
                        }
                } catch (Exception e) {

                }
        }

}