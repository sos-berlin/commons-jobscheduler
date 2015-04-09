package com.sos.scheduler.plugins.globalmonitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;




import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sos.scheduler.model.objects.Job;

public class JobConfigurationFileChanger {
    private JobSchedulerFileElement jobfileToChange;
    private ArrayList <JobSchedulerFileElement> listOfMonitors;
    private Job job;
    
    
    public JobConfigurationFileChanger(JobSchedulerFileElement jobfileToChange_) {
        super();
        this.jobfileToChange = jobfileToChange_;
    }
    
     public Job readConfigurationFile() throws JAXBException, FileNotFoundException  {
        
        JAXBContext context = JAXBContext.newInstance( Job.class );
        Unmarshaller unmarshaller = context.createUnmarshaller();
    
        job = (Job) unmarshaller.unmarshal( jobfileToChange.getConfigurationFile());
        return job;
      }
     
     public Job readConfigurationFile(Document e) throws JAXBException, FileNotFoundException  {
         
         JAXBContext context = JAXBContext.newInstance( Job.class );
         Unmarshaller unmarshaller = context.createUnmarshaller();
     
         job = (Job) unmarshaller.unmarshal(e);
         return job;
       }
     
     
     public void changeConfigurationFile(){
         String s = "xxxxxxxxxxxx";
         for(JobSchedulerFileElement monitor:listOfMonitors)  {
             s = s + monitor.getJobSchedulerElementName() + ",";
         }
         job.setTitle(s);
     }
      
      public void setListOfMonitors(ArrayList<JobSchedulerFileElement> listOfMonitors) {
        this.listOfMonitors = listOfMonitors;
    }

    public void writeFile() throws JAXBException, ParseException, FileNotFoundException {
          if (job != null){
              JAXBContext context = JAXBContext.newInstance( Job.class );

              Marshaller m = context.createMarshaller();
              m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
              m.marshal( job, new FileOutputStream( jobfileToChange.getConfigurationFile()) );      
          }
    }
    
    public Document getJobAsDocument() throws JAXBException, ParseException, FileNotFoundException {
        if (job != null){

            DOMResult res = new DOMResult();
            JAXBContext context = JAXBContext.newInstance(Job.class);
            context.createMarshaller().marshal(job, res);
            Document doc = (Document) res.getNode();
            return doc;
        }
        return null;
  }
}
