package com.sos.JSHelper.Archiver;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSFile;


public class JSArchiver extends JSListenerClass {

	private final String		conClassName	= "JSArchiver";

	// private String fileNameToArchive = null;
	// private String archiveFolderName = null;
	// private boolean archiveWithDelete = false;

	private JSArchiverOptions	objOptions		= null;

	public JSArchiver() {
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

	/**
	 *
	 * \brief Archive
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws Exception
	 */
	public void Archive() throws Exception {

		final String conMethodName = conClassName + "::Archive";

		String nameOfArchiveFile = null;
		JSFile fleArchiveFile = null;
		String strFName = null;
		String strFileName = null;

		SignalInfo(String.format("%1$s: starting, file to archive: '%2$s', archive-folder: '%3$s'.", conMethodName, objOptions.FileName(), objOptions
				.ArchiveFolderName()));

		if (objOptions == null) {
			SignalAbort(String.format("%1$s: no Options specified. Archive aborted.", conMethodName));
		}

		objOptions.CheckMandatory();

		strFileName = objOptions.FileName();
		JSFile fileToArchive = new JSFile(strFileName);
		fileToArchive.registerMessageListener(this);
		fileToArchive.MustExist();
//		String strOriginalFileName = fileToArchive.getName();

		if (objOptions.CreateTimeStamp()) {
			/*
			 * kein Rename-TimeStamp verwenden, zur Sicherheit, damit die
			 * Original-Datei erhalten bleibt (jedenfalls erstmal).
			 */
			fileToArchive = new JSFile(fileToArchive.CopyTimeStamp());
//		geht nicht auf be0027	fileToArchive.deleteOnExit();
			strFName = fileToArchive.getName();
		}
		else {
			strFName = fileToArchive.getName();
		}

		if (objOptions.CompressArchivedFile()) {
			// TODO: eigene Klasse mit eigenen Optionen implementieren
			// TODO: entweder als "eigenen" File ablegen oder in ein bestehendes ZIP-File integrieren.
			nameOfArchiveFile = objOptions.ArchiveFolderName() + strFName + ".gz";
			fleArchiveFile = new JSFile(nameOfArchiveFile);
			if (fleArchiveFile.exists()) {
				SignalAbort(String.format("%1$s: file '%2$s' already exist.", conMethodName, nameOfArchiveFile));
			}
			SignalInfo(String.format("%3$s: Zip '%1$s' into Zip-File '%2$s'", fileToArchive.getAbsoluteFile(), fleArchiveFile.getAbsoluteFile(), conMethodName));

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

		}
		else {
			nameOfArchiveFile = objOptions.ArchiveFolderName() + strFName;
			fileToArchive.copy(nameOfArchiveFile);
		}

		message(String.format("%1$s: Archive file created '%2$s'.", conMethodName, nameOfArchiveFile));
		if (objOptions.DeleteFileAfterArchiving() || objOptions.CreateTimeStamp()) {
			final JSFile fileToDelete = new JSFile(strFileName);
			fileToDelete.delete();
			SignalInfo(String.format("%1$s: archived file deleted '%2$s'.", conMethodName, strFileName));
		}
		if (objOptions.CreateTimeStamp()) {
/*
 * hier muß gelöscht werden, da der die Datei mit dem Time-Stamp-Namen
 * auch noch im Source-Verzeichnis liegt. Und hier muß diese weg.
 *
 */
			fileToArchive.delete();
			SignalInfo(String.format("%1$s: archived file deleted '%2$s'.", conMethodName, fileToArchive.getName()));
		}
		SignalInfo(String.format("%1$s: end", conMethodName));
	}
} // public class JSArchiver
