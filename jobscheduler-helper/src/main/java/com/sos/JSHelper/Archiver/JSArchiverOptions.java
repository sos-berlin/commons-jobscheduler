package com.sos.JSHelper.Archiver;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.JSOptionsClass;

/** Klaus Buettner */
@JSOptionClass(name = "JSArchiverOptions", description = "Optionen für die Archivierung von Dateien")
public class JSArchiverOptions extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "JSArchiverOptions";
    private String strArchiveFolderName = "./archive/";
    private final String conArchiveFolderNameSettingsKey = this.CLASSNAME + ".ArchiveFolderName";
    private String strFileName = null;
    private final String conFileNameSettingsKey = this.CLASSNAME + ".FileName";
    private boolean flgCompressArchivedFile = false;
    private final String conCompressArchivedFileSettingsKey = this.CLASSNAME + ".CompressArchivedFile";
    private boolean flgDeleteFileAfterArchiving = false;
    private final String conDeleteFileAfterArchivingSettingsKey = this.CLASSNAME + ".DeleteFileAfterArchiving";
    private boolean flgCreateTimeStamp = true;
    private final String conCreateTimeStampSettingsKey = this.CLASSNAME + ".CreateTimeStamp";
    private boolean flgUseArchive = false;
    private final String conUseArchiveSettingsKey = this.CLASSNAME + ".UseArchive";

    @JSOptionDefinition(name = "ArchiveFolderName", value = "./archive/", description = "Name des Folder mit den archivierten Dateien", key = "ArchiveFolderName", type = "JSOptionFolderName", mandatory = true)
    public SOSOptionFolderName ArchiveFolderName = new SOSOptionFolderName(this, this.CLASSNAME + ".ArchiveFolderName",
            "Name des Folder mit den archivierten Dateien", "./archive/", "./archive/", true);

    public JSArchiverOptions() {
    }

    public JSArchiverOptions(final HashMap<String, String> JSSettings) throws Exception {
        this.setAllOptions(JSSettings);
    }

    @Override
    public void toOut() {
        System.out.println(this.getAllOptionsAsString());
    }

    @Override
    public String toString() {
        return this.getAllOptionsAsString();
    }

    private String getAllOptionsAsString() {
        String strT = CLASSNAME + "\n";
        strT += "Create the archive-file-name using a timestamp : " + this.CreateTimeStamp() + "\n";
        strT += "File has to be archived after processing : " + this.UseArchive() + "\n";
        return strT;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> JSSettings) {
        this.objSettings = JSSettings;
        super.Settings(this.objSettings);
        String strT = super.getItem(this.conArchiveFolderNameSettingsKey);
        if (!this.isEmpty(strT)) {
            this.ArchiveFolderName(super.getItem(this.conArchiveFolderNameSettingsKey));
        }
        strT = super.getItem(this.conFileNameSettingsKey);
        if (!this.isEmpty(strT)) {
            this.FileName(super.getItem(this.conFileNameSettingsKey));
        }
        this.CompressArchivedFile(super.getBoolItem(this.conCompressArchivedFileSettingsKey));
        this.DeleteFileAfterArchiving(super.getBoolItem(this.conDeleteFileAfterArchivingSettingsKey));
        this.CreateTimeStamp(super.getBoolItem(this.conCreateTimeStampSettingsKey));
        this.UseArchive(super.getBoolItem(this.conUseArchiveSettingsKey));
    }

    @Override
    public void CheckMandatory() throws Exception {
        this.FileName(this.FileName());
        this.ArchiveFolderName(this.ArchiveFolderName());
    }

    public String ArchiveFolderName() {
        if (this.strArchiveFolderName == null) {
            this.strArchiveFolderName = "./";
        }
        return this.strArchiveFolderName;
    }

    public JSArchiverOptions ArchiveFolderName(final String pstrArchiveFolderName) {
        final String conMethodName = this.CLASSNAME + "::ArchiveFolderName";
        this.strArchiveFolderName = this.CheckFolder(pstrArchiveFolderName, conMethodName, true);
        return this;
    }

    public String FileName() {
        return this.strFileName;
    }

    public JSArchiverOptions FileName(final String pstrFileName) {
        final String conMethodName = this.CLASSNAME + "::FileName";
        if (this.isEmpty(pstrFileName)) {
            this.SignalError(String.format(this.conNullButMandatory, "FileName", this.conFileNameSettingsKey, conMethodName));
        }
        if (this.isNotEqual(this.strFileName, pstrFileName)) {
            this.strFileName = this.CheckFileIsReadable(pstrFileName, conMethodName);
        }
        return this;
    }

    public boolean CompressArchivedFile() {
        return this.flgCompressArchivedFile;
    }

    public JSArchiverOptions CompressArchivedFile(final boolean pflgCompressArchivedFile) {
        this.flgCompressArchivedFile = pflgCompressArchivedFile;
        return this;
    }

    public boolean DeleteFileAfterArchiving() {
        return this.flgDeleteFileAfterArchiving;
    }

    public JSArchiverOptions DeleteFileAfterArchiving(final boolean pflgDeleteFileAfterArchiving) {
        this.flgDeleteFileAfterArchiving = pflgDeleteFileAfterArchiving;
        return this;
    }

    public boolean CreateTimeStamp() {
        return this.flgCreateTimeStamp;
    }

    public JSArchiverOptions CreateTimeStamp(final boolean pflgCreateTimeStamp) {
        this.flgCreateTimeStamp = pflgCreateTimeStamp;
        return this;
    }

    public boolean UseArchive() {
        return this.flgUseArchive;
    }

    public JSArchiverOptions UseArchive(final boolean pflgUseArchive) {
        this.flgUseArchive = pflgUseArchive;
        return this;
    }

    public String getFileNameSettingsKey() throws Exception {
        String RetVal = null;
        try {
            RetVal = this.conFileNameSettingsKey;
        } catch (final Exception e) {
            throw e;
        }
        return RetVal;
    }

}