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

    public JSArchiverOptions Options() {
        if (objOptions == null) {
            objOptions = new JSArchiverOptions();
        }
        return objOptions;
    }

    public void Options(final JSArchiverOptions pobjOptions) throws Exception {
        objOptions = pobjOptions;
        objOptions.CheckMandatory();
    }

    public void Archive(final JSArchiverOptions pobjArchiveOptions) throws Exception {
        this.Options(pobjArchiveOptions);
        if (pobjArchiveOptions.UseArchive()) {
            this.Archive();
        }
    }

    public void Archive() throws Exception {
        final String methodName = CLASSNAME + "::Archive";
        String nameOfArchiveFile = null;
        JSFile fleArchiveFile = null;
        String strFName = null;
        String strFileName = null;
        SignalInfo(String.format("%1$s: starting, file to archive: '%2$s', archive-folder: '%3$s'.", methodName, objOptions.FileName(),
                objOptions.ArchiveFolderName()));
        if (objOptions == null) {
            SignalAbort(String.format("%1$s: no Options specified. Archive aborted.", methodName));
        }
        objOptions.CheckMandatory();
        strFileName = objOptions.FileName();
        JSFile fileToArchive = new JSFile(strFileName);
        fileToArchive.registerMessageListener(this);
        fileToArchive.MustExist();
        if (objOptions.CreateTimeStamp()) {
            fileToArchive = new JSFile(fileToArchive.CopyTimeStamp());
            strFName = fileToArchive.getName();
        } else {
            strFName = fileToArchive.getName();
        }
        if (objOptions.CompressArchivedFile()) {
            nameOfArchiveFile = objOptions.ArchiveFolderName() + strFName + ".gz";
            fleArchiveFile = new JSFile(nameOfArchiveFile);
            if (fleArchiveFile.exists()) {
                SignalAbort(String.format("%1$s: file '%2$s' already exist.", methodName, nameOfArchiveFile));
            }
            SignalInfo(String.format("%3$s: Zip '%1$s' into Zip-File '%2$s'", fileToArchive.getAbsoluteFile(), fleArchiveFile.getAbsoluteFile(),
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
            nameOfArchiveFile = objOptions.ArchiveFolderName() + strFName;
            fileToArchive.copy(nameOfArchiveFile);
        }
        message(String.format("%1$s: Archive file created '%2$s'.", methodName, nameOfArchiveFile));
        if (objOptions.DeleteFileAfterArchiving() || objOptions.CreateTimeStamp()) {
            final JSFile fileToDelete = new JSFile(strFileName);
            fileToDelete.delete();
            SignalInfo(String.format("%1$s: archived file deleted '%2$s'.", methodName, strFileName));
        }
        if (objOptions.CreateTimeStamp()) {
            fileToArchive.delete();
            SignalInfo(String.format("%1$s: archived file deleted '%2$s'.", methodName, fileToArchive.getName()));
        }
        SignalInfo(String.format("%1$s: end", methodName));
    }

}