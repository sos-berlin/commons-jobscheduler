/*
 * Created on 28.02.2011 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.InstallationService.batchInstallationModel;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;

import sos.scheduler.InstallationService.batchInstallationModel.installationFile.AutomatedInstallation;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsFinishPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsHTMLLicencePanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsInstallPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsProcessPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsTargetPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsUserInputPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ComIzforgeIzpackPanelsUserPathPanel;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.Entry;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.ObjectFactory;
import sos.scheduler.InstallationService.batchInstallationModel.installationFile.UserInput;
import sos.scheduler.InstallationService.batchInstallationModel.installations.Installation;

public class JSXMLInstallationFile {

    private AutomatedInstallation automatedInstallation;

    public JSXMLInstallationFile() {
        super();
    }

    public void setValues(Installation installation) {

        ObjectFactory o = new ObjectFactory();

        automatedInstallation = o.createAutomatedInstallation();

        ComIzforgeIzpackPanelsUserInputPanel home = o.createComIzforgeIzpackPanelsUserInputPanel();
        UserInput userInput_home = o.createUserInput();
        home.setUserInput(userInput_home);
        home.setId("home");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(home);

        ComIzforgeIzpackPanelsUserInputPanel licences = o.createComIzforgeIzpackPanelsUserInputPanel();
        UserInput userInput_licences = o.createUserInput();
        licences.setUserInput(userInput_licences);
        licences.setId("licences");
        Entry entryLicence = o.createEntry();
        entryLicence.setKey("licence");
        entryLicence.setValue(installation.getLicence());
        licences.getUserInput().getEntry().add(entryLicence);

        Entry entryLicencOptions = o.createEntry();
        entryLicencOptions.setKey("licenceOptions");
        entryLicencOptions.setValue(installation.getLicenceOptions());
        licences.getUserInput().getEntry().add(entryLicencOptions);

        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel()
                .add(licences);

        ComIzforgeIzpackPanelsHTMLLicencePanel comIzforgeIzpackPanelsHTMLLicencePanelGpl = o.createComIzforgeIzpackPanelsHTMLLicencePanel();
        comIzforgeIzpackPanelsHTMLLicencePanelGpl.setId("gpl_licence");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsHTMLLicencePanelGpl);

        ComIzforgeIzpackPanelsHTMLLicencePanel comIzforgeIzpackPanelsHTMLLicencePanelCommercial = o.createComIzforgeIzpackPanelsHTMLLicencePanel();
        comIzforgeIzpackPanelsHTMLLicencePanelGpl.setId("commercial_licence");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsHTMLLicencePanelCommercial);

        ComIzforgeIzpackPanelsTargetPanel comIzforgeIzpackPanelsTargetPanel = o.createComIzforgeIzpackPanelsTargetPanel();
        comIzforgeIzpackPanelsTargetPanel.setId("target");

        comIzforgeIzpackPanelsTargetPanel.setInstallpath(installation.getInstallPath());
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsTargetPanel);

        ComIzforgeIzpackPanelsUserPathPanel comIzforgeIzpackPanelsUserPathPanel = o.createComIzforgeIzpackPanelsUserPathPanel();
        comIzforgeIzpackPanelsUserPathPanel.setId("userpath");
        comIzforgeIzpackPanelsUserPathPanel.setUserPathPanelElement(installation.getUserPathPanelElement());
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsUserPathPanel);

        ComIzforgeIzpackPanelsUserInputPanel network = o.createComIzforgeIzpackPanelsUserInputPanel();
        UserInput userInput_network = o.createUserInput();
        network.setUserInput(userInput_network);
        network.setId("network");

        Entry entry_schedulerHost = o.createEntry();
        entry_schedulerHost.setKey("schedulerHost");
        entry_schedulerHost.setValue(installation.getHost());
        network.getUserInput().getEntry().add(entry_schedulerHost);

        Entry entry_schedulerId = o.createEntry();
        entry_schedulerId.setKey("schedulerId");
        entry_schedulerId.setValue(installation.getSchedulerId());
        network.getUserInput().getEntry().add(entry_schedulerId);

        Entry entry_schedulerAllowedHost = o.createEntry();
        entry_schedulerAllowedHost.setKey("schedulerAllowedHost");
        entry_schedulerAllowedHost.setValue(installation.getSchedulerAllowedHost());
        network.getUserInput().getEntry().add(entry_schedulerAllowedHost);

        Entry entry_schedulerPort = o.createEntry();
        entry_schedulerPort.setKey("schedulerPort");
        entry_schedulerPort.setValue(String.valueOf(installation.getSchedulerPort()));
        network.getUserInput().getEntry().add(entry_schedulerPort);
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(network);

        ComIzforgeIzpackPanelsInstallPanel comIzforgeIzpackPanelsInstallPanel = o.createComIzforgeIzpackPanelsInstallPanel();
        comIzforgeIzpackPanelsInstallPanel.setId("install");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsInstallPanel);

        ComIzforgeIzpackPanelsProcessPanel comIzforgeIzpackPanelsProcessPanel = o.createComIzforgeIzpackPanelsProcessPanel();
        comIzforgeIzpackPanelsProcessPanel.setId("process");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsProcessPanel);

        ComIzforgeIzpackPanelsFinishPanel comIzforgeIzpackPanelsFinishPanel = o.createComIzforgeIzpackPanelsFinishPanel();
        comIzforgeIzpackPanelsFinishPanel.setId("finish");
        automatedInstallation.getComIzforgeIzpackPanelsUserInputPanelOrComIzforgeIzpackPanelsHTMLLicencePanelOrComIzforgeIzpackPanelsTargetPanel().add(
                comIzforgeIzpackPanelsFinishPanel);

    }

    public void writeFile(File output) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AutomatedInstallation.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(automatedInstallation, new FileOutputStream(output));
    }

}
