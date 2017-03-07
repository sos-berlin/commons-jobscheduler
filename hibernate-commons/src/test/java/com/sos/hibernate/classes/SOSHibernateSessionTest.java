package com.sos.hibernate.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.query.NativeQuery;
import org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateSessionTest {

    final static Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSessionTest.class);

    public void extractCommands(String file) throws Exception {
        MultipleLinesSqlCommandExtractor ex = new MultipleLinesSqlCommandExtractor();
        Reader reader = null;
        try {
            reader = new FileReader(file);
            String[] commands = ex.extractCommands(reader);

            for (int i = 0; i < commands.length; i++) {
                LOGGER.info("####### " + i + " START #####");
                LOGGER.info(commands[i]);
                LOGGER.info("####### " + i + " END #####");
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
   
    
    public StringBuilder getFileContent(String inputFile) throws Exception{
        FileReader fr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader(inputFile);
            br = new BufferedReader(fr);
            String nextLine = "";
            while ((nextLine = br.readLine()) != null) {
                sb.append(nextLine);
                sb.append("\n");
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ex) {
                    //
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
        return sb;
    }
    
    public static void main(String[] args) throws Exception {
        String configFile = "./src/test/resources/hibernate.cfg.xml";
        String sqlFile = "test.sql";
        
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(configFile);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.build();

            LOGGER.info(factory.getDialect().toString());
            session = factory.openStatelessSession();
            session.executeNativeQueries(Paths.get(sqlFile));
            
            //SOSHibernateSessionTest t = new SOSHibernateSessionTest();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (factory != null) {
                factory.close();
            }
        }

    }

}
