package com.sos.scheduler.model.objects;

import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;


import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSObjJobChain extends JobChain {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjJobChain.class);
    public final static String fileNameExtension = ".job_chain.xml";
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
        setHotFolderSrc(super.objVirtualFile);
        loadObject();
    }

    public void loadObject() {
        JobChain objJobChain = (JobChain) unMarshal(super.objVirtualFile);
        setObjectFieldsFrom(objJobChain);
        setHotFolderSrc(super.objVirtualFile);
    }

    public void loadObject(File file) {
        JobChain jobChain = (JobChain) unMarshal(file);
        setObjectFieldsFrom(jobChain);
        if (super.objVirtualFile == null) {
            try {
                ISOSVFSHandler sosVFSHandler = VFSFactory.getHandler("local");
                ISOSVfsFileTransfer sosVFSFileTransfer = (ISOSVfsFileTransfer) sosVFSHandler;
                ISOSVirtualFile virtualFile = sosVFSFileTransfer.getFileHandle(file.getAbsolutePath());
                super.objVirtualFile = virtualFile;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        setHotFolderSrc(super.objVirtualFile);
    }

    public void loadObject(String xml) {
        JobChain jobChain = (JobChain) unMarshal(xml);
        setObjectFieldsFrom(jobChain);
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
        String strT = "";
        strT = pstrPathName + "/" + this.getName() + JSObjJobChain.fileNameExtension;
        return strT;
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
        BigInteger maxOrders;
        try {
            maxOrders = new BigInteger(strMaxOrders);
            setMaxorders(maxOrders);
        } catch (NumberFormatException e) {
            maxOrders = new BigInteger("0");
        }
    }

    public void setMaxorders(final BigInteger maxOrders) {
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
        BigInteger intM = getmaxOrders();
        String strM = "";
        if (intM != null) {
            strM = String.valueOf(intM);
        }
        return strM;
    }

  

    @Override
    public BigInteger getmaxOrders() {
        if (maxOrders == null) {
            return new BigInteger("0");
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
