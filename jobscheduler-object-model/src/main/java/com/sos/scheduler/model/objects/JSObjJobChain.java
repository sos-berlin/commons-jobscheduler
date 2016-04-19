package com.sos.scheduler.model.objects;

import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.shell.cmdShell;
import com.sos.scheduler.converter.graphviz.Dot;
import com.sos.scheduler.model.SchedulerObjectFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/** @author oh */
public class JSObjJobChain extends JobChain {

    public static final String fileNameExtension = ".job_chain.xml";
    public static final String conFileNameExtension4NodeParameterFile = ".config.xml";

    public JSObjJobChain(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        super.strFileNameExtension = fileNameExtension;
    }

    public JSObjJobChain(final SchedulerObjectFactory schedulerObjectFactory, final JobChain origOrder) {
        this(schedulerObjectFactory);
        setObjectFieldsFrom(origOrder);
    }

    public JSObjJobChain(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        this(schedulerObjectFactory);
        super.objVirtualFile = pobjVirtualFile;
        loadObject();
    }

    public void loadObject() {
        JobChain objJobChain = (JobChain) unMarshal(super.objVirtualFile);
        setObjectFieldsFrom(objJobChain);
        setHotFolderSrc(super.objVirtualFile);
    }

    public void setNameIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setName(value);
        }
    }

    public void setDistributedNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setDistributed(value);
        }
    }

    public void setMaxOrdersIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setMaxorders(value);
        }
    }

    public void setOrdersRecoverableIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setOrdersRecoverable(value);
        }
    }

    public void setVisibleIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setVisible(value);
        }
    }

    public void setTitleIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.setTitle(value);
        }
    }

    public boolean isNestedJobChain() {
        boolean flgR = false;
        if (jobChainNodeJobChain != null) {
            flgR = true;
        }
        return flgR;
    }

    public void setOrdersRecoverable(final boolean pflgIsOrdersRecoverable) {
        if (canUpdate() && pflgIsOrdersRecoverable != getYesOrNo(this.getOrdersRecoverable())) {
            if (pflgIsOrdersRecoverable) {
                this.setOrdersRecoverable(conYES);
            } else {
                this.setOrdersRecoverable(conNO);
            }
            setDirty();
        }
    }

    public void setVisible(final boolean pflgIsVisible) {
        if (canUpdate()) {
            if (pflgIsVisible) {
                this.setVisible(conYES);
            } else {
                this.setVisible(conNO);
            }
            setDirty();
        }
    }

    public void setDistributed(final boolean pflgIsDistributed) {
        if (canUpdate() && getYesOrNo(super.getDistributed()) != pflgIsDistributed) {
            if (pflgIsDistributed) {
                this.setDistributed(conYES);
            } else {
                this.setDistributed(conNO);
            }
            setDirty();
        }
    }

    public String createFileName(final String pstrPathName) {
        return pstrPathName + "/" + this.getName() + JSObjJobChain.fileNameExtension;
    }

    @Override
    public String getObjectName() {
        if (getHotFolderSrc() == null) {
            return "";
        }
        String name = this.getHotFolderSrc().getName();
        if (name == null) {
            name = "???";
        } else {
            name = name.substring(0, name.indexOf(JSObjJobChain.fileNameExtension));
            name = new File(name).getName();
        }
        return name;
    }

    @Override
    public String getObjectNameAndTitle() {
        String strT = this.getObjectName();
        String strV = this.getTitle();
        if (strV != null && !strV.isEmpty()) {
            strT += " - " + this.getTitle();
        }
        return strT;
    }

    @Override
    public void setName(final String pstrName) {
        if (!canUpdate()) {
            return;
        }
        String strOldName = getObjectName();
        if (!strOldName.equals(pstrName)) {
            changeSourceName(pstrName);
            super.setName(pstrName);
            setDirty();
        }
    }

    @Override
    public String getTitle() {
        String strT = "";
        if (title == null) {
            strT = getObjectName();
        } else {
            strT = title;
        }
        return strT;
    }

    @Override
    public void setTitle(final String pstrTitle) {
        if (!canUpdate()) {
            return;
        }
        String strOldTitle = getTitle();
        if (!strOldTitle.equals(pstrTitle)) {
            super.setTitle(pstrTitle);
            setDirty();
        }
    }

    public void setMaxorders(final String strMaxOrders) {
        if (!canUpdate()) {
            return;
        }
        int maxOrders;
        try {
            maxOrders = Integer.parseInt(strMaxOrders);
            setMaxorders(maxOrders);
        } catch (NumberFormatException e) {
            maxOrders = 0;
        }
    }

    public void setMaxorders(final int maxOrders) {
        super.maxOrders = maxOrders;
        setDirty();
    }

    public boolean isDistributed() {
        return getYesOrNo(distributed);
    }

    public boolean isRecoverable() {
        return getYesOrNo(getOrdersRecoverable());
    }

    public boolean isVisible() {
        return getYesOrNo(getVisible());
    }

    public String getMaxOrders() {
        Integer intM = getmaxOrders();
        String strM = "";
        if (intM != null) {
            strM = String.valueOf(intM);
        }
        return strM;
    }

    public String createDOTFile() throws Exception {
        boolean flgCreateCluster = false;
        boolean flgCreateErrorNodes = false;
        JSObjJobChain objChain = this;
        String strName = objChain.getName();
        if (strName == null) {
            strName = objChain.getObjectName();
        }
        String strFileName = "c:/temp/dottest/" + strName;
        JSTextFile objDotFile = new JSTextFile(strFileName + ".dot");
        objDotFile.deleteOnExit();
        objDotFile.WriteLine("digraph " + getQuoted(strName) + " {");
        objDotFile.WriteLine("rankdir = TB;");
        objDotFile.WriteLine("graph [");
        objDotFile.WriteLine("label = " + getQuoted(objChain.getTitle()));
        objDotFile.WriteLine("fontsize = 8");
        objDotFile.WriteLine("];");
        objDotFile.WriteLine("node [");
        objDotFile.WriteLine("fontsize = 8");
        objDotFile.WriteLine("shape = " + getQuoted("box"));
        objDotFile.WriteLine("style = " + getQuoted("rounded,filled"));
        objDotFile.WriteLine("fillcolor = " + getQuoted("#CCFF99"));
        objDotFile.WriteLine("fontname = " + getQuoted("Arial"));
        objDotFile.WriteLine("];");
        objDotFile.WriteLine("edge [").WriteLine("color = " + getQuoted("#31CEF0")).WriteLine("arrowsize = " + getQuoted("0.5")).WriteLine("];");
        Hashtable<String, JobChainNode> tblNodes = new Hashtable<String, JobChainNode>();
        int intFileOrderSourceCnt = 0;
        String strFirstState = "";
        for (Object objO : objChain.getFileOrderSourceList()) {
            if (objO instanceof FileOrderSource) {
                FileOrderSource objFOS = (FileOrderSource) objO;
                String strDir = objFOS.getDirectory();
                String strRegExp = objFOS.getRegex();
                String strNextState = objFOS.getNextState();
                intFileOrderSourceCnt++;
                String strH = "";
                strH = "<b>" + "Folder: " + strDir + " </b>" + conHtmlBR;
                strH += "<i><b><font color=\"blue\" >" + escapeHTML("RegExp: " + strRegExp) + "</font></b></i>" + conHtmlBR;
                objDotFile.WriteLine(getQuoted("FileOrderSource" + intFileOrderSourceCnt) + " [label = <" + strH + ">];");
            }
        }
        for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (objO instanceof JobChainNode) {
                JobChainNode objNode = (JobChainNode) objO;
                String strState = objNode.getState();
                if (strFirstState.isEmpty()) {
                    strFirstState = strState;
                }
                if (tblNodes.get(strState) == null) {
                    tblNodes.put(strState, objNode);
                    String strJobName = objNode.getJob();
                    if (strJobName == null) {
                        strJobName = "endNode";
                    }
                    String strT = strState;
                    if (strState.startsWith("!") && !flgCreateErrorNodes) {
                        if (!strState.equalsIgnoreCase(strJobName)) {
                            strT = strState + ": " + strJobName;
                        }
                        objDotFile.WriteLine(getQuoted(strState) + " [label = " + getQuoted(strT) + "];");
                    }
                    if (flgCreateErrorNodes) {
                        String strErrorState = objNode.getErrorState();
                        if (strErrorState != null && tblNodes.get(strErrorState) == null) {
                            tblNodes.put(strErrorState, objNode);
                            objDotFile.WriteLine(getQuoted(strErrorState) + " [label = " + getQuoted(strErrorState)
                                    + ", color=\"red\", fillcolor=\"yellow\", style=\"filled\", fontcolor=\"blue\"];");
                        }
                    }
                }
            }
        }
        boolean flgStart = true;
        if (flgStart) {
            flgStart = false;
            if (flgCreateCluster) {
                objDotFile.WriteLine("subgraph cluster_0 {");
                objDotFile.WriteLine("    style=filled;");
                objDotFile.WriteLine("    color=lightgrey;");
                objDotFile.WriteLine("    node [style=filled,color=white];");
            }
        }
        String strState = null;
        String strNextState = null;
        String strLastNextState = null;
        intFileOrderSourceCnt = 0;
        for (Object objO : objChain.getFileOrderSourceList()) {
            if (objO instanceof FileOrderSource) {
                FileOrderSource objFOS = (FileOrderSource) objO;
                intFileOrderSourceCnt++;
                strNextState = objFOS.getNextState();
                if (strNextState.trim().isEmpty()) {
                    strNextState = strFirstState;
                }
                objDotFile.WriteLine(getQuoted("FileOrderSource" + intFileOrderSourceCnt) + " -> " + getQuoted(strNextState) + " [constraint=true]");
            }
        }
        for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (objO instanceof JobChainNode) {
                JobChainNode objNode = (JobChainNode) objO;
                strState = objNode.getState();
                int i = strState.indexOf(":");
                if (i > 0) {
                    String strFrom = strState.substring(0, i);
                    objDotFile.WriteLine(getQuoted(strFrom) + " -> " + getQuoted(strState) + " [constraint=true]");
                }
                if (strState.startsWith("split") == false | i > 0) {
                    strNextState = objNode.getNextState();
                    if (strNextState != null && !strNextState.isEmpty()) {
                        objDotFile.WriteLine(getQuoted(strState) + " -> " + getQuoted(strNextState));
                        strLastNextState = strNextState;
                    }
                }
            }
        }
        if (flgCreateCluster) {
            objDotFile.WriteLine("label = \"Process\";");
            objDotFile.WriteLine("}");
        }
        if (flgCreateErrorNodes) {
            Hashtable<String, JobChainNode> tblErrNodes = new Hashtable<String, JobChainNode>();
            for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
                if (objO instanceof JobChainNode) {
                    JobChainNode objNode = (JobChainNode) objO;
                    strState = objNode.getState();
                    String strErrorState = objNode.getErrorState();
                    if (strErrorState != null) {
                        objDotFile.WriteLine(getQuoted(strState) + " -> " + getQuoted(strErrorState) + " [style=\"dotted\", constraint=false]");
                        tblErrNodes.put(strErrorState, objNode);
                    }
                }
            }
            if (flgCreateCluster) {
                objDotFile.WriteLine("subgraph cluster_1 {");
                objDotFile.WriteLine("    style=filled;");
                objDotFile.WriteLine("color=lightgrey;");
                objDotFile.WriteLine("node [style=filled,color=white];");
            }
            String strLastErrNode = "";
            for (JobChainNode objErrNode : tblErrNodes.values()) {
                String strErrNodeName = objErrNode.getErrorState();
                if (flgCreateCluster) {
                    if (strLastErrNode.isEmpty()) {
                        strLastErrNode = strErrNodeName;
                    } else {
                        objDotFile.WriteLine(getQuoted(strLastErrNode) + " -> " + getQuoted(strErrNodeName) + " [style=invis]");
                        strLastErrNode = strErrNodeName;
                    }
                }
                objDotFile.WriteLine(getQuoted(strErrNodeName) + " -> " + getQuoted("end"));
            }
            if (flgCreateCluster) {
                objDotFile.WriteLine("label = \"Error\";");
                objDotFile.WriteLine("}");
            }
        }
        objDotFile.WriteLine("}");
        objDotFile.close();
        cmdShell objShell = new cmdShell();
        String strCommandString = String.format(Dot.Command + " -x -T%1$s %2$s.dot > %2$s.%1$s", "jpg", strFileName);
        objShell.setCommand(strCommandString);
        objShell.run();
        return strFileName + ".jpg";
    }

    @Override
    public Integer getmaxOrders() {
        if (maxOrders == null) {
            return 0;
        }
        return maxOrders;
    }

    public List<FileOrderSink> getFileOrderSinkList() {
        List<FileOrderSink> objList = new ArrayList<FileOrderSink>();
        for (Object objO : this.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (objO instanceof FileOrderSink) {
                objList.add((FileOrderSink) objO);
            }
        }
        return objList;
    }

    public List<FileOrderSource> getFileOrderSourceList() {
        List<FileOrderSource> objList = new ArrayList<FileOrderSource>();
        for (Object objO : this.getFileOrderSource()) {
            if (objO instanceof FileOrderSource) {
                objList.add((FileOrderSource) objO);
            }
        }
        return objList;
    }

    public List<JobChainNode> getJobChainNodeList() {
        List<JobChainNode> objList = new ArrayList<JobChainNode>();
        for (Object objO : this.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (objO instanceof JobChainNode) {
                objList.add((JobChainNode) objO);
            }
        }
        return objList;
    }

    public boolean isStateDefined(final String state) {
        for (String _state : getAllStates()) {
            if (_state.equals(state)) {
                return true;
            }
        }
        return false;
    }

    public String[] getAllStates() {
        return getStates();
    }

    public String[] getStates() {
        List<String> strStatesList = new ArrayList<String>();
        for (JobChainNode objNode : getJobChainNodeList()) {
            addToList(strStatesList, objNode.errorState);
            addToList(strStatesList, objNode.nextState);
            addToList(strStatesList, objNode.state);
        }
        return arrayListToStringArray(strStatesList);
    }

    public String[] getErrorStates() {
        List<String> strStatesList = new ArrayList<String>();
        for (JobChainNode objNode : getJobChainNodeList()) {
            addToList(strStatesList, objNode.errorState);
        }
        return arrayListToStringArray(strStatesList);
    }

    public String[] getNextStates() {
        List<String> strStatesList = new ArrayList<String>();
        for (JobChainNode objNode : getJobChainNodeList()) {
            addToList(strStatesList, objNode.nextState);
        }
        return arrayListToStringArray(strStatesList);
    }

    private void addToList(List<String> pobjL, final String pstrS) {
        if (pstrS != null && !pobjL.contains(pstrS)) {
            pobjL.add(pstrS);
        }
    }

    public List<JobChainNodeEnd> getJobChainNodeEndList() {
        List<JobChainNodeEnd> objList = new ArrayList<JobChainNodeEnd>();
        for (Object objO : this.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (objO instanceof JobChainNodeEnd) {
                objList.add((JobChainNodeEnd) objO);
            }
        }
        return objList;
    }

}