package com.sos.JSHelper.Archiver;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSFile;

public class JSArchiver extends JSListenerClass {

    private static final String CLASSNAME = "JSArchiver";
    private JSArchiverOptions objOptions = null;

    public JSArchiver() {
        //
    }

    public JSArchiver(final JSListener pobjListener) {
        registerMessageListener(pobjListener);
    }

    public JSArchiverOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JSArchiverOptions();
        }
        return objOptions;
    }

    public void setOptions(final JSArchiverOptions pobjOptions) throws Exception {
        objOptions = pobjOptions;
        objOptions.checkMandatory();
    }

    public void archive(final JSArchiverOptions pobjArchiveOptions) throws Exception {
        this.setOptions(pobjArchiveOptions);
        if (pobjArchiveOptions.isUseArchive()) {
            this.archive();
        }
    }

    public void archive() throws Exception {
        final String methodName = CLASSNAME + "::Archive";
        String nameOfArchiveFile = null;
        JSFile fleArchiveFile = null;
        String strFName = null;
        String strFileName = null;
        signalInfo(String.format("%1$s: starting, file to archive: '%2$s', archive-folder: '%3$s'.", methodName, objOptions.getFileName(),
                objOptions.getArchiveFolderName()));
        if (objOptions == null) {
            signalAbort(String.format("%1$s: no Options specified. Archive aborted.", methodName));
        }
        objOptions.checkMandatory();
        strFileName = objOptions.getFileName();
        JSFile fileToArchive = new JSFile(strFileName);
        fileToArchive.registerMessageListener(this);
        fileToArchive.mustExist();
        if (objOptions.isCreateTimeStamp()) {
            fileToArchive = new JSFile(fileToArchive.copyTimeStamp());
            strFName = fileToArchive.getName();
        } else {
            strFName = fileToArchive.getName();
        }
        if (objOptions.isCompressArchivedFile()) {
            nameOfArchiveFile = objOptions.getArchiveFolderName() + strFName + ".gz";
            fleArchiveFile = new JSFile(nameOfArchiveFile);
            if (fleArchiveFile.exists()) {
                signalAbort(String.format("%1$s: file '%2$s' already exist.", methodName, nameOfArchiveFile));
            }
            signalInfo(String.format("%3$s: Zip '%1$s' into Zip-File '%2$s'", fileToArchive.getAbsoluteFile(), fleArchiveFile.getAbsoluteFile(),
                    methodName));
            final BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileToArchive));
            final GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(fleArchiveFile));
            try {
                final byte buffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (final Exception e) {
                throw e;
            } finally {
                try {
                    in.close();
                } catch (final Exception e) {
                    throw e;
                }
                try {
                    out.close();
                } catch (final Exception e) {
                    throw e;
                }
            }
        } else {
            nameOfArchiveFile = objOptions.getArchiveFolderName() + strFName;
            fileToArchive.copy(nameOfArchiveFile);
        }
        message(String.format("%1$s: Archive file created '%2$s'.", methodName, nameOfArchiveFile));
        if (objOptions.isDeleteFileAfterArchiving() || objOptions.isCreateTimeStamp()) {
            final JSFile fileToDelete = new JSFile(strFileName);
            fileToDelete.delete();
            signalInfo(String.format("%1$s: archived file deleted '%2$s'.", methodName, strFileName));
        }
        if (objOptions.isCreateTimeStamp()) {
            fileToArchive.delete();
            signalInfo(String.format("%1$s: archived file deleted '%2$s'.", methodName, fileToArchive.getName()));
        }
        signalInfo(String.format("%1$s: end", methodName));
    }

}