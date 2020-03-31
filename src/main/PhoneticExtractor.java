package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import preprocessing.wikipedia.Extractor;


public class PhoneticExtractor extends Extractor {
	int entry_count;
	long start, stop;
	DBAccess dao;
	static boolean writeToFile = false;
	String lang = null;
	

	public PhoneticExtractor(String xmlName, String lang_, String outputFile) throws Exception {
		super();
		setParser(xmlName);
		setWriter(outputFile);
		dao = new DBAccess();
		dao.init(lang_);
		entry_count=0;
		start=0;stop=0;
		lang = lang_;
	}
	public void closeWriter() throws IOException {
		super.closeWriter();
		dao.close();
	}

	@Override
	public void extract() {
		try {
			parser.setPageCallback(new PageCallbackHandler() {
				@Override
				public void process(WikiPage page) {
					StringBuilder line = new StringBuilder();
					String title = page.getTitle().replaceAll("\n", "").trim();
					if (!validTitle(title))
						return;
					if (page.isRedirect())
						return;
					

					String wikitext = page.getWikiText();
					if (!StringUtils.isEmpty(wikitext)) {
						if (isTargetLang(wikitext,lang)) {
							String pron="",trans="";
							if(lang!="en") {
								pron = Pronounciation(title,wikitext);
								trans = Translation(wikitext);
							}
							String def = Definition(page,wikitext);
							
							if(writeToFile) {
								line.append(title + "\t\t\t\t" + pron + "\t\t\t\t" + trans + "\t\t\t\t" + def);;
								write(line.toString());
							}
							
							try {
								if(lang=="fr")
									dao.addEntryFR(title, pron, trans, def);
								if(lang=="en") {
									dao.addEntryEN(title, def);
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
							entry_count++;
							if(entry_count%1000==0) {
								stop = System.currentTimeMillis();
								System.out.println(entry_count + "\t ellapsed : " + (stop - start));
								start = System.currentTimeMillis();
								if(entry_count==408000)
									return;
							}
						}
					}
					else {
						System.out.println("empty\n\n");
					}
				}
			});
			parser.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean isTargetLang(String wikiText, String lang) {
		String langStr=null;
		if(lang=="fr")
			langStr = "{{langue|fr}}";
		if(lang=="en")
			langStr = "==English==";
		String[] texts = wikiText.split("\n");
		for (int i = 0; i < Math.min(3, texts.length); i++) { //language should be at the begginning of the text
				if (texts[i].contains(langStr)) {
					return true;
				}
		}
		return false;
	}
	
	protected String Pronounciation(String title,String wikiText) {
		//HANDLE THIS CASE:   '''haïr''' {{h|*}} {{pron|a.iʁ|fr}} {{t|fr}}, {{conjugaison|fr|grp=2}}
		final Matcher pron = Pattern.compile("'''" + title + "''' \\{\\{pron\\|(.+?)\\|fr\\}\\}").matcher(wikiText);
		if(pron.find()) {
			return pron.group(1).trim();
		}
		else
			return "";
	}
	protected String Definition(WikiPage page, String wikiText) {
		final Matcher pron = Pattern.compile("# (.+?)\\n").matcher(wikiText);
		if(pron.find()) {
			String def = pron.group(1);
			def = page.getPlainText(def);
			if (def.length()>74)
				def = def.substring(0, 74)+" (...)";
			return def;
		}
		else
			return "";
		
	}
	protected String Translation(String wikiText) {
		final Matcher pron = Pattern.compile("\\{\\{trad.\\|en\\|(.+?)\\}\\}").matcher(wikiText);
		if(pron.find()) {
			String trans = pron.group(1);
			
			if (trans.length()>29)
				trans = trans.substring(0, 29);
			
			int idx = trans.lastIndexOf("|");
			if(idx>0)
				trans=trans.substring(0,idx);
			
			return trans.trim();
		}
		else
			return "";
		
	}
	
}
