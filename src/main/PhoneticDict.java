package main;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.vogella.mysql.first.*;

public class PhoneticDict {

	public static void main(String[] args) throws Exception {
//		DBAccess dao = new DBAccess();
//        dao.populate();
		
		PhoneticExtractor p = new PhoneticExtractor("frwiktionary-20170420-pages-articles.xml","fr","titles.txt");
		p.extract();
		p.closeWriter();
		
//		PhoneticExtractor p = new PhoneticExtractor("enwiktionary-20170520-pages-articles.xml","en","titles-en.txt");
//		p.extract();
//		p.closeWriter();
	}

}
