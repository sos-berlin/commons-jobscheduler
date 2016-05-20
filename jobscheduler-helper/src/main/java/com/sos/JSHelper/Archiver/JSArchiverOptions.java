package com.sos.JSHelper.Archiver;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.JSOptionsClass;

/** Klaus Buettner */
@JSOptionClass(name = "JSArchiverOptions", description = "Optionen für die Archivierung von Dateien")
public class JSArchiverOptions extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "JSArchiverOptions";
    private static final Logger LOGGER = Logger.getLogger(JSArchiverOptions.class);
    private static final String ARCHIVE_FOLDER_NAME_SETTINGS_KEY = CLASSNAME + ".ArchiveFolderName";
    private static final String FILE_NAME_SETTINGS_KEY = CLASSNAME + ".FileName";
    private static final String COMPRESS_ARCHIVED_FILE_SETTINGS_KEY = CLASSNAME + ".CompressArchivedFile";
    private static final String DELETE_FILE_AFTER_ARCHIVING_SETTINGS_KEY = CLASSNAME + ".DeleteFileAfterArchiving";
    private static final String CREATE_TIME_STAMP_SETTINGS_KEY = CLASSNAME + ".CreateTimeStamp";
    private static final String USE_ARCHIVE_SETTINGS_KEY = CLASSNAME + ".UseArchive";
    private String strArchiveFolderName = "./archive/";
    private String strFileName = null;
    private boolean flgCompressArchivedFile = false;
    private boolean flgDeleteFileAfterArchiving = false;
    private boolean flgCreateTimeStamp = true;
    private boolean flgUseArchive = false;

    @JSOptionDefinition(name = "ArchiveFolderName", value = "./archive/", description = "Name des Folder mit den archivierten Dateien", key = "ArchiveFolderName", type = "JSOptionFolderName", mandatory = true)
    public SOSOptionFolderName archiveFolderName = new SOSOptionFolderName(this, CLASSNAME + ".ArchiveFolderName", 
            "Name des Folder mit den archivierten Dateien", "./archive/", "./archive/", true);

    public JSArchiverOptions() {
        //
    }

    public JSArchiverOptions(final HashMap<String, String> JSSettings) throws Exception {
        this.setAllOptions(JSSettings);
    }

    @Override
    public void toOut() {
        LOGGER.info(this.getAllOptionsAsString());
    }

    @Override
    public String toString() {
        return this.getAllOptionsAsString();
    }

    private String getAllOptionsAsString() {
        String strT = CLASSNAME + "\n";
        strT += "Create the archive-file-name using a timestamp : " + this.isCreateTimeStamp() + "\n";
        strT += "File has to be archived after processing : " + this.isUseArchive() + "\n";
        return strT;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> JSSettings) {
        this.objSettings = JSSettings;
        super.setSettings(this.objSettings);
        String strT = super.getItem(ARCHIVE_FOLDER_NAME_SETTINGS_KEY);
        if (!this.isEmpty(strT)) {
            this.setArchiveFolderName(super.getItem(ARCHIVE_FOLDER_NAME_SETTINGS_KEY));
        }
        strT = super.getItem(FILE_NAME_SETTINGS_KEY);
        if (!this.isEmpty(strT)) {
            this.setFileName(super.getItem(FILE_NAME_SETTINGS_KEY));
        }
        this.setCompressArchivedFile(super.getBoolItem(COMPRESS_ARCHIVED_FILE_SETTINGS_KEY));
        this.setDeleteFileAfterArchiving(super.getBoolItem(DELETE_FILE_AFTER_ARCHIVING_SETTINGS_KEY));
        this.setCreateTimeStamp(super.getBoolItem(CREATE_TIME_STAMP_SETTINGS_KEY));
        this.setUseArchive(super.getBoolItem(USE_ARCHIVE_SETTINGS_KEY));
    }

    @Override
    public void checkMandatory() throws Exception {
        this.setFileName(this.getFileName());
        this.setArchiveFolderName(this.getArchiveFolderName());
    }

    public String getArchiveFolderName() {
        if (this.strArchiveFolderName == null) {
            this.strArchiveFolderName = "./";
        }
        return this.strArchiveFolderName;
    }

    public JSArchiverOptions setArchiveFolderName(final String pstrArchiveFolderName) {
        final String conMethodName = CLASSNAME + "::ArchiveFolderName";
        this.strArchiveFolderName = this.checkFolder(pstrArchiveFolderName, conMethodName, true);
        return this;
    }

    public String getFileName() {
        return this.strFileName;
    }

    public JSArchiverOptions setFileName(final String pstrFileName) {
        final String conMethodName = CLASSNAME + "::FileName";
        if (this.isEmpty(pstrFileName)) {
            this.signalError(String.format(this.conNullButMandatory, "FileName", FILE_NAME_SETTINGS_KEY, conMethodName));
        }
        if (this.isNotEqual(this.strFileName, pstrFileName)) {
            this.strFileName = this.checkFileIsReadable(pstrFileName, conMethodName);
        }
        return this;
    }

    public boolean isCompressArchivedFile() {
        return this.flgCompressArchivedFile;
    }

    public JSArchiverOptions setCompressArchivedFile(final boolean pflgCompressArchivedFile) {
        this.flgCompressArchivedFile = pflgCompressArchivedFile;
        return this;
    }

    public boolean isDeleteFileAfterArchiving() {
        return this.flgDeleteFileAfterArchiving;
    }

    public JSArchiverOptions setDeleteFileAfterArchiving(final boolean pflgDeleteFileAfterArchiving) {
        this.flgDeleteFileAfterArchiving = pflgDeleteFileAfterArchiving;
        return this;
    }

    public boolean isCreateTimeStamp() {
        return this.flgCreateTimeStamp;
    }

    public JSArchiverOptions setCreateTimeStamp(final boolean pflgCreateTimeStamp) {
        this.flgCreateTimeStamp = pflgCreateTimeStamp;
        return this;
    }

    public boolean isUseArchive() {
        return this.flgUseArchive;
    }

    public JSArchiverOptions setUseArchive(final boolean pflgUseArchive) {
        this.flgUseArchive = pflgUseArchive;
        return this;
    }

    public String getFileNameSettingsKey() throws Exception {
        String RetVal = null;
        try {
            RetVal = FILE_NAME_SETTINGS_KEY;
        } catch (final Exception e) {
            throw e;
        }
        return RetVal;
    }

}