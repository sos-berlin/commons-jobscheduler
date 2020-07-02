package com.sos.JSHelper.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTime;

/** @author Florian Schreiber */
public abstract class SOSFileSystemOperations {

	protected static final Logger LOGGER = LoggerFactory.getLogger(SOSFileSystemOperations.class);
	public static final int CREATE_DIR = 0x01;
	public static final int GRACIOUS = 0x02;
	public static final int NOT_OVERWRITE = 0x04;
	public static final int RECURSIVE = 0x08;
	public static final int REMOVE_DIR = 0x10;
	public static final int WIPE = 0x20;
	public List<File> lstResultList = null;
	final int BUFF_SIZE = 100000;
	final byte[] buffer = new byte[BUFF_SIZE];

	protected abstract String getActionName();

	protected abstract boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious)
			throws Exception;

	public boolean canWrite(final String file) throws Exception {
		File filename = new File(file);
		return canWrite(filename, null, 0);
	}

	public boolean canWrite(final String file, final String fileSpec) throws Exception {
		File filename = new File(file);
		return canWrite(filename, fileSpec, Pattern.CASE_INSENSITIVE);
	}

	public boolean canWrite(final String file, final String fileSpec, final int fileSpecFlags) throws Exception {
		File filename = new File(file);
		return canWrite(filename, fileSpec, fileSpecFlags);
	}

	public boolean canWrite(final File file) throws Exception {
		return canWrite(file, null, 0);
	}

	public boolean canWrite(final File file, final String fileSpec) throws Exception {
		return canWrite(file, fileSpec, Pattern.CASE_INSENSITIVE);
	}

	public boolean canWrite(File file, final String fileSpec, final int fileSpecFlags) throws Exception {
		LOGGER.debug("arguments for canWrite:");
		LOGGER.debug("argument file=" + file.toString());
		LOGGER.debug("argument fileSpec=" + fileSpec);
		String msg = "";
		if (has(fileSpecFlags, Pattern.CANON_EQ)) {
			msg += "CANON_EQ";
		}
		if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE)) {
			msg += "CASE_INSENSITIVE";
		}
		if (has(fileSpecFlags, Pattern.COMMENTS)) {
			msg += "COMMENTS";
		}
		if (has(fileSpecFlags, Pattern.DOTALL)) {
			msg += "DOTALL";
		}
		if (has(fileSpecFlags, Pattern.MULTILINE)) {
			msg += "MULTILINE";
		}
		if (has(fileSpecFlags, Pattern.UNICODE_CASE)) {
			msg += "UNICODE_CASE";
		}
		if (has(fileSpecFlags, Pattern.UNIX_LINES)) {
			msg += "UNIX_LINES";
		}
		LOGGER.debug("argument fileSpecFlags=" + msg);
		String filename = file.getPath();
		filename = substituteAllDate(filename);
		file = new File(filename);
		if (!file.exists()) {
			LOGGER.info("checking file " + file.getAbsolutePath() + ": no such file or directory");
			return true;
		} else {
			if (!file.isDirectory()) {
				LOGGER.info(String.format("checking the file '%1$s' :: file exists", file.getCanonicalPath()));
				boolean writable = false;
				try {
					RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(), "rw");
					f.close();
					writable = true;
				} catch (Exception e) {
					//
				}
				if (!writable) {
					LOGGER.info("file " + file.getCanonicalPath() + ": cannot be written ");
					return false;
				} else {
					return true;
				}
			} else {
				if (fileSpec == null || fileSpec.isEmpty()) {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory exists");
					return true;
				}
				List<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, false, 0, 0, -1, -1, 0, 0);
				if (fileList.isEmpty()) {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory contains no files matching "
							+ fileSpec);
					return false;
				} else {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory contains " + fileList.size()
							+ " file(s) matching " + fileSpec);
					for (int i = 0; i < fileList.size(); i++) {
						File checkFile = fileList.get(i);
						LOGGER.info("found " + checkFile.getCanonicalPath());
						boolean writable = false;
						try {
							RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(), "rw");
							f.close();
							writable = true;
						} catch (Exception e) {
							//
						}
						if (!writable) {
							LOGGER.info("file " + checkFile.getCanonicalPath() + ": cannot be written ");
							return false;
						}
					}
					lstResultList = fileList;
					return true;
				}
			}
		}

	}

	public boolean existsFile(final String file) throws Exception {
		File filename = new File(file);
		return existsFile(filename, null, 0);
	}

	public boolean existsFile(final String file, final String fileSpec) throws Exception {
		File filename = new File(file);
		return existsFile(filename, fileSpec, Pattern.CASE_INSENSITIVE);
	}

	public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags) throws Exception {
		File filename = new File(file);
		return existsFile(filename, fileSpec, fileSpecFlags);
	}

	public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles) throws Exception {
		File filename = new File(file);
		return existsFile(filename, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize,
				skipFirstFiles, skipLastFiles);
	}

	public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles, final int minNumOfFiles, final int maxNumOfFiles)
			throws Exception {
		File filename = new File(file);
		return existsFile(filename, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize,
				skipFirstFiles, skipLastFiles, minNumOfFiles, maxNumOfFiles);
	}

	public boolean existsFile(final File file) throws Exception {
		return existsFile(file, null, 0);
	}

	public boolean existsFile(final File file, final String fileSpec) throws Exception {
		return existsFile(file, fileSpec, Pattern.CASE_INSENSITIVE);
	}

	public boolean existsFile(final File file, final String fileSpec, final int fileSpecFlags) throws Exception {
		return existsFile(file, fileSpec, fileSpecFlags, "0", "0", "-1", "-1", 0, 0);
	}

	public boolean existsFile(final File file, final String fileSpec, final int fileSpecFlags, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles) throws Exception {
		return existsFile(file, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize,
				skipFirstFiles, skipLastFiles, -1, -1);
	}

	public boolean existsFile(File file, final String fileSpec, final int fileSpecFlags, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, final int minNumOfFiles, final int maxNumOfFiles) throws Exception {
		long minAge = 0;
		long maxAge = 0;
		long minSize = -1;
		long maxSize = -1;
		LOGGER.debug("arguments for existsFile:");
		LOGGER.debug("argument file=" + file.toString());
		LOGGER.debug("argument fileSpec=" + fileSpec);
		String msg = "";
		if (has(fileSpecFlags, Pattern.CANON_EQ)) {
			msg += "CANON_EQ";
		}
		if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE)) {
			msg += "CASE_INSENSITIVE";
		}
		if (has(fileSpecFlags, Pattern.COMMENTS)) {
			msg += "COMMENTS";
		}
		if (has(fileSpecFlags, Pattern.DOTALL)) {
			msg += "DOTALL";
		}
		if (has(fileSpecFlags, Pattern.MULTILINE)) {
			msg += "MULTILINE";
		}
		if (has(fileSpecFlags, Pattern.UNICODE_CASE)) {
			msg += "UNICODE_CASE";
		}
		if (has(fileSpecFlags, Pattern.UNIX_LINES)) {
			msg += "UNIX_LINES";
		}
		LOGGER.debug("argument fileSpecFlags=" + msg);
		LOGGER.debug("argument minFileAge=" + minFileAge);
		LOGGER.debug("argument maxFileAge=" + maxFileAge);
		minAge = calculateFileAge(minFileAge);
		maxAge = calculateFileAge(maxFileAge);
		LOGGER.debug("argument minFileSize=" + minFileSize);
		LOGGER.debug("argument maxFileSize=" + maxFileSize);
		minSize = calculateFileSize(minFileSize);
		maxSize = calculateFileSize(maxFileSize);
		LOGGER.debug("argument skipFirstFiles=" + skipFirstFiles);
		LOGGER.debug("argument skipLastFiles=" + skipLastFiles);
		LOGGER.debug("argument minNumOfFiles=" + minNumOfFiles);
		LOGGER.debug("argument maxNumOfFiles=" + maxNumOfFiles);
		if (skipFirstFiles < 0) {
			throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
		}
		if (skipLastFiles < 0) {
			throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
		}
		if (skipFirstFiles > 0 && skipLastFiles > 0) {
			throw new JobSchedulerException("skip only either first files or last files");
		}
		if ((skipFirstFiles > 0 || skipLastFiles > 0) && minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
			throw new JobSchedulerException(
					"missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
		}
		String filename = file.getPath();
		filename = substituteAllDate(filename);
		file = new File(filename);
		if (!file.exists()) {
			LOGGER.info("checking file " + file.getAbsolutePath() + ": no such file or directory");
			return false;
		} else {
			if (!file.isDirectory()) {
				LOGGER.info("checking file " + file.getCanonicalPath() + ": file exists");
				long currentTime = System.currentTimeMillis();
				if (minAge > 0) {
					long interval = currentTime - file.lastModified();
					if (interval < 0) {
						throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath()
								+ "] was modified in the future.");
					}
					if (interval < minAge) {
						LOGGER.info("checking file age " + file.lastModified() + ": minimum age required is " + minAge);
						return false;
					}
				}
				if (maxAge > 0) {
					long interval = currentTime - file.lastModified();
					if (interval < 0) {
						throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath()
								+ "] was modified in the future.");
					}
					if (interval > maxAge) {
						LOGGER.info("checking file age " + file.lastModified() + ": maximum age required is " + maxAge);
						return false;
					}
				}
				if (minSize > -1 && minSize > file.length()) {
					LOGGER.info("checking file size " + file.length() + ": minimum size required is " + minFileSize);
					return false;
				}
				if (maxSize > -1 && maxSize < file.length()) {
					LOGGER.info("checking file size " + file.length() + ": maximum size required is " + maxFileSize);
					return false;
				}
				if (skipFirstFiles > 0 || skipLastFiles > 0) {
					LOGGER.info("file skipped");
					return false;
				}
				lstResultList = new Vector<File>();
				lstResultList.add(file);
				return true;
			} else {
				if (fileSpec == null || fileSpec.isEmpty()) {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory exists");
					return true;
				}
				List<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, false, minAge, maxAge,
						minSize, maxSize, skipFirstFiles, skipLastFiles);
				if (fileList.isEmpty()) {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory contains no files matching "
							+ fileSpec);
					return false;
				} else {
					LOGGER.info("checking file " + file.getCanonicalPath() + ": directory contains " + fileList.size()
							+ " file(s) matching " + fileSpec);
					for (int i = 0; i < fileList.size(); i++) {
						File checkFile = fileList.get(i);
						LOGGER.info("found " + checkFile.getCanonicalPath());
					}
					if (minNumOfFiles >= 0 && fileList.size() < minNumOfFiles) {
						LOGGER.info(
								"found " + fileList.size() + " files, minimum expected " + minNumOfFiles + " files");
						return false;
					}
					if (maxNumOfFiles >= 0 && fileList.size() > maxNumOfFiles) {
						LOGGER.info(
								"found " + fileList.size() + " files, maximum expected " + maxNumOfFiles + " files");
						return false;
					}
					lstResultList = fileList;
					return true;
				}
			}
		}

	}

	public boolean removeFile(final File file) throws Exception {
		return removeFile(file, ".*", 0, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, "name", "asc");
	}

	public boolean removeFile(final File file, final int flags) throws Exception {
		return removeFile(file, ".*", flags, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, "name", "asc");
	}

	public boolean removeFile(final File file, final String fileSpec) throws Exception {
		return removeFile(file, fileSpec, 0, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, "name", "asc");
	}

	public boolean removeFile(final File file, final String fileSpec, final int flags) throws Exception {
		return removeFile(file, fileSpec, flags, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, "name", "asc");
	}

	public boolean removeFile(final File file, final String fileSpec, final int flags, final int fileSpecFlags)
			throws Exception {
		return removeFile(file, fileSpec, flags, fileSpecFlags, "0", "0", "-1", "-1", 0, 0, "name", "asc");
	}

	public boolean removeFile(final File file, final String fileSpec, final int flags, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		int nrOfRemovedObjects = removeFileCnt(file, fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge,
				minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
		return nrOfRemovedObjects > 0;
	}
	
	private void removeFileNio(File f) {
		Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
		try {
			Files.delete(path);
		} catch (NoSuchFileException x) {
			throw new JobSchedulerException(path + "no exists");
		} catch (DirectoryNotEmptyException x) {
			throw new JobSchedulerException(path + "directory not empty");
		} catch (IOException x) {
			throw new JobSchedulerException(path + "file permission issue");
		}
	}

	public int removeFileCnt(final File file, final String fileSpec, final int flags, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		boolean gracious;
		boolean recursive;
		boolean remove_dir;
		boolean wipe;
		lstResultList = new Vector<File>();

		int nrOfRemovedFiles = 0;
		int nrOfRemovedDirectories = 0;
		long minAge = 0;
		long maxAge = 0;
		long minSize = -1;
		long maxSize = -1;

		recursive = has(flags, SOSFileSystemOperations.RECURSIVE);
		gracious = has(flags, SOSFileSystemOperations.GRACIOUS);
		wipe = has(flags, SOSFileSystemOperations.WIPE);
		remove_dir = has(flags, SOSFileSystemOperations.REMOVE_DIR);
		LOGGER.debug("arguments for removeFile:");
		LOGGER.debug("argument file=" + file);
		LOGGER.debug("argument fileSpec=" + fileSpec);
		String msg = "";
		if (has(flags, SOSFileSystemOperations.GRACIOUS)) {
			msg += "GRACIOUS ";
		}
		if (has(flags, SOSFileSystemOperations.RECURSIVE)) {
			msg += "RECURSIVE ";
		}
		if (has(flags, SOSFileSystemOperations.REMOVE_DIR)) {
			msg += "REMOVE_DIR ";
		}
		if (has(flags, SOSFileSystemOperations.WIPE)) {
			msg += "WIPE ";
		}
		LOGGER.debug("argument flags=" + msg);
		msg = "";
		if (has(fileSpecFlags, Pattern.CANON_EQ)) {
			msg += "CANON_EQ ";
		}
		if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE)) {
			msg += "CASE_INSENSITIVE ";
		}
		if (has(fileSpecFlags, Pattern.COMMENTS)) {
			msg += "COMMENTS ";
		}
		if (has(fileSpecFlags, Pattern.DOTALL)) {
			msg += "DOTALL ";
		}
		if (has(fileSpecFlags, Pattern.MULTILINE)) {
			msg += "MULTILINE ";
		}
		if (has(fileSpecFlags, Pattern.UNICODE_CASE)) {
			msg += "UNICODE_CASE ";
		}
		if (has(fileSpecFlags, Pattern.UNIX_LINES)) {
			msg += "UNIX_LINES ";
		}
		LOGGER.debug("argument fileSpecFlags=" + msg);
		LOGGER.debug("argument minFileAge=" + minFileAge);
		LOGGER.debug("argument maxFileAge=" + maxFileAge);
		minAge = calculateFileAge(minFileAge);
		maxAge = calculateFileAge(maxFileAge);
		LOGGER.debug("argument minFileSize=" + minFileSize);
		LOGGER.debug("argument maxFileSize=" + maxFileSize);
		minSize = calculateFileSize(minFileSize);
		maxSize = calculateFileSize(maxFileSize);
		LOGGER.debug("argument skipFirstFiles=" + skipFirstFiles);
		LOGGER.debug("argument skipLastFiles=" + skipLastFiles);
		if (skipFirstFiles < 0) {
			throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
		}
		if (skipLastFiles < 0) {
			throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
		}
		if (skipFirstFiles > 0 && skipLastFiles > 0) {
			throw new JobSchedulerException("skip only either first files or last files");
		}
		if ((skipFirstFiles > 0 || skipLastFiles > 0) && minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
			throw new JobSchedulerException(
					"missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
		}
		if (!file.exists()) {
			if (gracious) {
				LOGGER.info("cannot remove file: file does not exist: " + file.getCanonicalPath());
				return 0;
			} else {
				throw new JobSchedulerException("file does not exist: " + file.getCanonicalPath());
			}
		}
		List<File> fileList;
		if (file.isDirectory()) {
			if (!file.canRead()) {
				throw new JobSchedulerException("directory is not readable: " + file.getCanonicalPath());
			}
			LOGGER.info(
					"remove [" + fileSpec + "] from " + file.getCanonicalPath() + (recursive ? " (recursive)" : ""));
			fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags,
					has(flags, SOSFileSystemOperations.RECURSIVE), minAge, maxAge, minSize, maxSize, 0, 0);
		} else {
			fileList = new Vector<File>();
			fileList.add(file);
			fileList = filelistFilterAge(fileList, minAge, maxAge);
			fileList = filelistFilterSize(fileList, minSize, maxSize);
			if (skipFirstFiles > 0 || skipLastFiles > 0) {
				fileList.clear();
			}
		}

		if (sortCriteria.equalsIgnoreCase("age")) {
			fileList.sort(new FileComparatorAge());
		} else if (sortCriteria.equalsIgnoreCase("size")) {
			fileList.sort(new FileComparatorSize());
		} else {
			fileList.sort(new FileComparatorName());
		}

		File currentFile;

		for (int i = skipFirstFiles; i < fileList.size() - skipLastFiles; i++) {
			currentFile = fileList.get(i);
			lstResultList.add(currentFile);
			LOGGER.info("remove file " + currentFile.getCanonicalPath());
			if (wipe) {
				if (!wipe(currentFile)) {
					throw new JobSchedulerException("cannot remove file: " + currentFile.getCanonicalPath());
				}
			} else {
				removeFileNio(currentFile);
			}
			nrOfRemovedFiles++;
		}
		if (remove_dir) {
			int firstSize = listFolders(file.getPath(), ".*", 0, recursive).size();
			if (recursive) {
				recDeleteEmptyDir(file, fileSpec, fileSpecFlags);
			} else {
				Vector<File> list = listFolders(file.getPath(), fileSpec, fileSpecFlags);
				File f;
				for (int i = 0; i < list.size(); i++) {
					f = list.get(i);
					if (f.isDirectory()) {
						if (!f.canRead()) {
							throw new JobSchedulerException("directory is not readable: " + f.getCanonicalPath());
						}
						if (f.list().length == 0) {
							removeFileNio(f);
							LOGGER.info("remove directory " + f.getPath());
						} else {
							LOGGER.debug("directory [" + f.getCanonicalPath()
									+ "] cannot be removed because it is not empty");
							String lst = f.list()[0];
							for (int n = 1; n < f.list().length; n++) {
								lst += ", " + f.list()[n];
							}
							LOGGER.debug("          contained files " + f.list().length + ": " + lst);
						}
					}
				}
			}
			nrOfRemovedDirectories = firstSize - listFolders(file.getPath(), ".*", 0, recursive).size();
		}
		msg = "";
		if (remove_dir) {
			if (nrOfRemovedDirectories == 1) {
				msg = " + 1 directory removed";
			} else {
				msg = " + " + nrOfRemovedDirectories + " directories removed";
			}
		}
		LOGGER.info(nrOfRemovedFiles + " file(s) removed" + msg);
		return nrOfRemovedFiles + nrOfRemovedDirectories;

	}

	public Vector<File> listFolders(final String folder, final String regexp, final int flag,
			final boolean withSubFolder) throws Exception {
		Vector<File> filelist = new Vector<File>();
		File file = null;
		File[] subDir = null;
		file = new File(folder);
		subDir = file.listFiles();
		filelist.addAll(listFolders(folder, regexp, flag));
		if (withSubFolder) {
			for (File element : subDir) {
				if (element.isDirectory()) {
					filelist.addAll(listFolders(element.getPath(), regexp, flag, true));
				}
			}
		}
		return filelist;
	}

	public Vector<File> listFolders(final String folder, final String regexp, final int flag) throws Exception {
		Vector<File> filelist = new Vector<File>();
		if (folder == null || folder.isEmpty()) {
			throw new FileNotFoundException("empty directory not allowed!!");
		}
		File f = new File(folder);
		if (!f.exists()) {
			throw new FileNotFoundException("directory does not exist: " + folder);
		}
		filelist = new Vector<File>();
		File[] files = f.listFiles(new SOSFilelistFilter(regexp, flag));
		for (int i = 0; i < files.length; i++) {
			if (!".".equals(files[i].getName()) && !"..".equals(files[i].getName())) {
				filelist.add(files[i]);
			}
		}
		return filelist;
	}

	private boolean recDeleteEmptyDir(final File dir, final String fileSpec, final int fileSpecFlags) throws Exception {
		if (dir.isDirectory()) {
			if (!dir.canRead()) {
				throw new JobSchedulerException("directory is not readable: " + dir.getCanonicalPath());
			}
		} else {
			return false;
		}
		File[] list = dir.listFiles();
		if (list.length == 0) {
			return true;
		}
		Pattern p = Pattern.compile(fileSpec, fileSpecFlags);
		File f;
		for (File element : list) {
			f = element;
			if (recDeleteEmptyDir(f, fileSpec, fileSpecFlags)) {
				if (p.matcher(f.getName()).matches()) {
					removeFileNio(f);
					LOGGER.info("remove directory " + f.getPath());
				}
			} else {
				if (f.isDirectory()) {
					LOGGER.debug("directory [" + f.getCanonicalPath() + "] cannot be removed because it is not empty");
					String lst = f.list()[0];
					for (int n = 1; n < f.list().length; n++) {
						lst += ", " + f.list()[n];
					}
					LOGGER.debug("          contained files " + f.list().length + ": " + lst);
				}
			}
		}
		return dir.list().length == 0;
	}

	public boolean removeFile(final String file) throws Exception {
		return removeFile(new File(file));
	}

	public boolean removeFile(final String file, final int flags) throws Exception {
		return removeFile(new File(file), flags);
	}

	public boolean removeFile(final String file, final String fileSpec) throws Exception {
		return removeFile(new File(file), fileSpec);
	}

	public boolean removeFile(final String file, final String fileSpec, final int flags) throws Exception {
		return removeFile(new File(file), fileSpec, flags);
	}

	public boolean removeFile(final String file, final String fileSpec, final int flags, final int fileSpecFlags)
			throws Exception {
		return removeFile(new File(file), fileSpec, flags, fileSpecFlags);
	}

	public boolean removeFile(final String file, final String fileSpec, final int flags, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		return removeFile(new File(file), fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize,
				maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
	}

	public int removeFileCnt(final String file, final String fileSpec, final int flags, final int fileSpecFlags,
			final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
			final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		return removeFileCnt(new File(file), fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize,
				maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
	}

	public boolean copyFile(final File source, final File dest) throws Exception {
		return copyFile(source, dest, false);
	}

	public boolean copyFile(final File source, final File target, final int flags) throws Exception {
		return copyFile(source, target, ".*", flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean copyFile(final File source, final File target, final String fileSpec) throws Exception {
		return copyFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean copyFile(final File source, final File target, final String fileSpec, final int flags)
			throws Exception {
		return copyFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0,
				0, "name", "asc");
	}

	public boolean copyFile(final File source, final String fileSpec, final int flags, final int fileSpecFlags,
			final String replacing, final String replacement) throws Exception {
		return copyFile(source, null, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0,
				0, "name", "asc");
	}

	public boolean copyFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement) throws Exception {
		return copyFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0,
				0, "name", "asc");
	}

	public boolean copyFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		String mode = "copy";
		return transferFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge,
				maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder, mode);
	}

	public boolean copyFile(final String source, final String target) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile);
	}

	public boolean copyFile(final String source, final String target, final int flags) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, flags);
	}

	public boolean copyFile(final String source, final String target, final String fileSpec) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, fileSpec);
	}

	public boolean copyFile(final String source, final String target, final String fileSpec, final int flags)
			throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, fileSpec, flags);
	}

	public boolean copyFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean copyFile(final String source, final String fileSpec, final int flags, final int fileSpecFlags,
			final String replacing, final String replacement) throws Exception {
		File sourceFile = new File(source);
		return copyFile(sourceFile, fileSpec, flags, fileSpecFlags, replacing, replacement);
	}

	public boolean copyFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement);
	}

	public boolean copyFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge,
				maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, "name", "asc");
	}

	public int copyFileCnt(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement,
				minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria,
				sortOrder);
	}

	public boolean renameFile(final File source, final File target) throws Exception {
		return renameFile(source, target, ".*", 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean renameFile(final File source, final File target, final int flags) throws Exception {
		return renameFile(source, target, ".*", flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec) throws Exception {
		return renameFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final int flags)
			throws Exception {
		return renameFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1",
				0, 0, "name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags) throws Exception {
		return renameFile(source, target, fileSpec, flags, fileSpecFlags, null, null, "0", "0", "-1", "-1", 0, 0,
				"name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final String replacing,
			final String replacement) throws Exception {
		return renameFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, replacing, replacement, "0", "0", "-1",
				"-1", 0, 0, "name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final int flags,
			final String replacing, final String replacement) throws Exception {
		return renameFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, replacing, replacement, "0", "0",
				"-1", "-1", 0, 0, "name", "asc");
	}

	public boolean renameFile(final File source, final String fileSpec, final int flags, final int fileSpecFlags,
			final String replacing, final String replacement) throws Exception {
		return renameFile(source, null, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0,
				0, "name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement) throws Exception {
		return renameFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1",
				0, 0, "name", "asc");
	}

	public boolean renameFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		String mode = "rename";
		return transferFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge,
				maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder, mode);
	}

	public boolean renameFile(final String source, final String target) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile);
	}

	public boolean renameFile(final String source, final String target, final int flags) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, flags);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final int flags)
			throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, flags);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final String replacing,
			final String replacement) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, replacing, replacement);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final int flags,
			final String replacing, final String replacement) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, flags, replacing, replacement);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement);
	}

	public boolean renameFile(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge,
				maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
	}

	public int renameFileCnt(final String source, final String target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		File sourceFile = new File(source);
		File targetFile = target == null ? null : new File(target);
		return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement,
				minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria,
				sortOrder);
	}

	private boolean transferFile(final File source, final File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder, final String mode) throws Exception {
		int nrOfTransferedFiles = transferFileCnt(source, target, fileSpec, flags, fileSpecFlags, replacing,
				replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles,
				sortCriteria, sortOrder);
		return nrOfTransferedFiles > 0;
	}

	private int transferFileCnt(final File source, File target, final String fileSpec, final int flags,
			final int fileSpecFlags, final String replacing, final String replacement, final String minFileAge,
			final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles,
			final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
		int nrOfTransferedFiles = 0;
		boolean create_dir;
		boolean gracious;
		boolean overwrite;
		boolean replace = false;

		String targetFilename;
		long minAge = 0;
		long maxAge = 0;
		long minSize = -1;
		long maxSize = -1;
		lstResultList = new Vector<File>();
		if (sortCriteria == null || sortCriteria.isEmpty()) {
			sortCriteria = "name";
		}
		if (sortOrder == null || sortOrder.isEmpty()) {
			sortOrder = "asc";
		}

		create_dir = has(flags, SOSFileSystemOperations.CREATE_DIR);
		gracious = has(flags, SOSFileSystemOperations.GRACIOUS);
		overwrite = !has(flags, SOSFileSystemOperations.NOT_OVERWRITE);
		LOGGER.debug(getActionName() + "arguments:");
		LOGGER.debug("argument source=" + source.toString());
		if (target != null) {
			LOGGER.debug("argument target=" + target.toString());
		}
		LOGGER.debug("argument fileSpec=" + fileSpec);
		String msg = "";
		if (has(flags, SOSFileSystemOperations.CREATE_DIR)) {
			msg += "CREATE_DIR ";
		}
		if (has(flags, SOSFileSystemOperations.GRACIOUS)) {
			msg += "GRACIOUS ";
		}
		if (has(flags, SOSFileSystemOperations.NOT_OVERWRITE)) {
			msg += "NOT_OVERWRITE ";
		}
		if (has(flags, SOSFileSystemOperations.RECURSIVE)) {
			msg += "RECURSIVE ";
		}
		if ("".equals(msg)) {
			msg = "0";
		}
		LOGGER.debug("argument flags=" + msg);
		msg = "";
		if (has(fileSpecFlags, Pattern.CANON_EQ)) {
			msg += "CANON_EQ ";
		}
		if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE)) {
			msg += "CASE_INSENSITIVE ";
		}
		if (has(fileSpecFlags, Pattern.COMMENTS)) {
			msg += "COMMENTS ";
		}
		if (has(fileSpecFlags, Pattern.DOTALL)) {
			msg += "DOTALL ";
		}
		if (has(fileSpecFlags, Pattern.MULTILINE)) {
			msg += "MULTILINE ";
		}
		if (has(fileSpecFlags, Pattern.UNICODE_CASE)) {
			msg += "UNICODE_CASE ";
		}
		if (has(fileSpecFlags, Pattern.UNIX_LINES)) {
			msg += "UNIX_LINES ";
		}
		if ("".equals(msg)) {
			msg = "0";
		}
		LOGGER.debug("argument fileSpecFlags=" + msg);
		LOGGER.debug("argument replacing=" + replacing);
		LOGGER.debug("argument replacement=" + replacement);
		LOGGER.debug("argument minFileAge=" + minFileAge);
		LOGGER.debug("argument maxFileAge=" + maxFileAge);
		minAge = calculateFileAge(minFileAge);
		maxAge = calculateFileAge(maxFileAge);
		LOGGER.debug("argument minFileSize=" + minFileSize);
		LOGGER.debug("argument maxFileSize=" + maxFileSize);
		minSize = calculateFileSize(minFileSize);
		maxSize = calculateFileSize(maxFileSize);
		LOGGER.debug("argument skipFirstFiles=" + skipFirstFiles);
		LOGGER.debug("argument skipLastFiles=" + skipLastFiles);
		if (skipFirstFiles < 0) {
			throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
		}
		if (skipLastFiles < 0) {
			throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
		}

		if (replacing != null || replacement != null) {
			if (replacing == null) {
				throw new JobSchedulerException("replacing cannot be null if replacement is set");
			}
			if (replacement == null) {
				throw new JobSchedulerException("replacement cannot be null if replacing is set");
			}
			if (!"".equals(replacing)) {
				try {
					Pattern.compile(replacing);
				} catch (PatternSyntaxException pse) {
					throw new JobSchedulerException("invalid pattern '" + replacing + "'");
				}
				replace = true;
			}
		}
		if (!source.exists()) {
			if (gracious) {
				LOGGER.info(nrOfTransferedFiles + " file(s) renamed");
				return nrOfTransferedFiles;
			} else {
				throw new JobSchedulerException("file or directory does not exist: " + source.getCanonicalPath());
			}
		}
		if (!source.canRead()) {
			throw new JobSchedulerException("file or directory is not readable: " + source.getCanonicalPath());
		}
		if (target != null) {
			targetFilename = substituteAllDate(target.getPath());
			targetFilename = substituteAllDirectory(targetFilename, source.getPath());

			target = new File(targetFilename);
		}
		if (create_dir && target != null && !target.exists()) {
			if (target.mkdirs()) {
				LOGGER.info("create target directory " + target.getCanonicalPath());
			} else {
				throw new JobSchedulerException("cannot create directory " + target.getCanonicalPath());
			}
		}
		List<File> list = null;
		if (source.isDirectory()) {
			if (target != null) {
				if (!target.exists()) {
					throw new JobSchedulerException("directory does not exist: " + target.getCanonicalPath());
				}
				if (!target.isDirectory()) {
					throw new JobSchedulerException("target is no directory: " + target.getCanonicalPath());
				}
			}
			list = getFilelist(source.getPath(), fileSpec, fileSpecFlags, has(flags, SOSFileSystemOperations.RECURSIVE),
					minAge, maxAge, minSize, maxSize, 0, 0);
		} else {
			list = new Vector<File>();
			list.add(source);
			list = filelistFilterAge(list, minAge, maxAge);
			list = filelistFilterSize(list, minSize, maxSize);
			if (skipFirstFiles > 0 || skipLastFiles > 0) {
				list.clear();
			}
		}
		File sourceFile;
		File targetFile;
		File dir;
		if (sortCriteria.equalsIgnoreCase("age")) {
			list.sort(new FileComparatorAge());
		} else if (sortCriteria.equalsIgnoreCase("size")) {
			list.sort(new FileComparatorSize());
		} else {
			list.sort(new FileComparatorName());
		}
		if (sortOrder.equalsIgnoreCase("desc")) {
			Collections.reverse(list);
		}
		for (int i = skipFirstFiles; i < list.size() - skipLastFiles; i++) {
			sourceFile = list.get(i);
			lstResultList.add(sourceFile);
			if (target != null) {
				if (target.isDirectory()) {
					String root = source.isDirectory() ? source.getPath() : source.getParent();
					targetFilename = target.getPath() + sourceFile.getPath().substring(root.length());
				} else {
					targetFilename = target.getPath();
				}
			} else {
				if (source.isDirectory()) {
					String root = source.isDirectory() ? source.getPath() : source.getParent();
					targetFilename = source.getPath() + sourceFile.getPath().substring(root.length());
				} else {
					targetFilename = source.getParent() + "/" + sourceFile.getName();
				}
			}
			targetFile = new File(targetFilename);
			try {
				if (replace) {
					targetFilename = targetFile.getName();
					targetFilename = replaceGroups(targetFilename, replacing, replacement);
					targetFilename = substituteAllDate(targetFilename);
					targetFilename = substituteAllFilename(targetFilename, targetFile.getName());

					targetFile = new File(targetFile.getParent() + "/" + targetFilename);
				}
			} catch (Exception re) {
				throw new JobSchedulerException("replacement error in file " + targetFilename + ": " + re.getMessage());
			}
			dir = new File(targetFile.getParent());
			if (!dir.exists()) {
				if (dir.mkdirs()) {
					LOGGER.info("create directory " + dir.getCanonicalPath());
				} else {
					throw new JobSchedulerException("cannot create directory " + dir.getCanonicalPath());
				}
			}
			if (!handleOneFile(sourceFile, targetFile, overwrite, gracious)) {
				continue;
			}
			nrOfTransferedFiles++;
		}
		LOGGER.info(getActionName() + nrOfTransferedFiles + " file(s)");

		return nrOfTransferedFiles;

	}

	private List<File> filelistFilterAge(List<File> filelist, final long minAge, final long maxAge) throws Exception {
		long currentTime = System.currentTimeMillis();
		if (minAge != 0) {
			File file;
			List<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file = filelist.get(i);
				long interval = currentTime - file.lastModified();
				if (interval < 0) {
					throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath()
							+ "] was modified in the future.");
				}
				if (interval >= minAge) {
					newlist.add(file);
				}
			}
			filelist = newlist;
		}
		if (maxAge != 0) {
			File file;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file = filelist.get(i);
				long interval = currentTime - file.lastModified();
				if (interval < 0) {
					throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath()
							+ "] was modified in the future.");
				}
				if (interval <= maxAge) {
					newlist.add(file);
				}
			}
			filelist = newlist;
		}
		return filelist;
	}

	private List<File> filelistFilterSize(List<File> filelist, final long minSize, final long maxSize)
			throws Exception {
		if (minSize > -1) {
			File file;
			List<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file = filelist.get(i);
				if (file.length() >= minSize) {
					newlist.add(file);
				}
			}
			filelist = newlist;
		}
		if (maxSize > -1) {
			File file;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file = filelist.get(i);
				if (file.length() <= maxSize) {
					newlist.add(file);
				}
			}
			filelist = newlist;
		}
		return filelist;
	}

	private List<File> filelistSkipFiles(List<File> filelist, final int skipFirstFiles, final int skipLastFiles,
			final String sorting) throws Exception {

		if ("sort_size".equals(sorting)) {
			filelist.sort(new FileComparatorSize());
		} else if ("sort_age".equals(sorting)) {
			filelist.sort(new FileComparatorAge());
		}

		return filelist;
	}

	public Vector<File> listFiles(final String folder, final String regexp, final int flag, final boolean withSubFolder)
			throws Exception {
		Vector<File> filelist = new Vector<File>();
		File file = null;
		File[] subDir = null;
		file = new File(folder);
		subDir = file.listFiles();
		filelist.addAll(listFiles(folder, regexp, flag));
		if (withSubFolder) {
			for (File element : subDir) {
				if (element.isDirectory()) {
					filelist.addAll(listFiles(element.getPath(), regexp, flag, true));
				}
			}
		}
		return filelist;
	}

	public Vector<File> listFiles(final String folder, final String regexp, final int flag) throws Exception {
		Vector<File> filelist = new Vector<File>();
		if (folder == null || folder.isEmpty()) {
			throw new FileNotFoundException("empty directory not allowed!!");
		}
		File f = new File(folder);
		if (!f.exists()) {
			throw new FileNotFoundException("directory does not exist: " + folder);
		}
		filelist = new Vector<File>();
		File[] files = f.listFiles(new SOSFilelistFilter(regexp, flag));
		for (File file : files) {
			if (file.isFile()) {
				filelist.add(file);
			}
		}
		return filelist;
	}

	private Vector<File> getFilelist(final String folder, final String regexp, final int flag,
			final boolean withSubFolder, final long minFileAge, final long maxFileAge, final long minFileSize,
			final long maxFileSize, final int skipFirstFiles, final int skipLastFiles) throws Exception {
		Vector<File> filelist = new Vector<File>();
		List<File> temp = new Vector<File>();
		File file = null;
		File[] subDir = null;
		file = new File(folder);
		subDir = file.listFiles();
		temp = this.listFiles(folder, regexp, flag);
		temp = filelistFilterAge(temp, minFileAge, maxFileAge);
		temp = filelistFilterSize(temp, minFileSize, maxFileSize);
		if ((minFileSize != -1 || maxFileSize != -1) && minFileAge == 0 && maxFileAge == 0) {
			temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_size");
		} else if (minFileAge != 0 || maxFileAge != 0) {
			temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_age");
		}
		filelist.addAll(temp);
		if (withSubFolder) {
			for (File element : subDir) {
				if (element.isDirectory()) {
					filelist.addAll(getFilelist(element.getPath(), regexp, flag, true, minFileAge, maxFileAge,
							minFileSize, maxFileSize, skipFirstFiles, skipLastFiles));
				}
			}
		}
		return filelist;
	}

	private long calculateFileAge(final String fileage) throws Exception {
		long age = 0;
		if (fileage == null || fileage.trim().isEmpty()) {
			return 0;
		}
		if (fileage.indexOf(":") == -1) {
			if (!fileage.matches("[\\d]+")) {
				throw new JobSchedulerException("[" + fileage + "] is no valid file age");
			} else {
				return Long.parseLong(fileage) * 1000;
			}
		}
		if (!fileage.matches("^[\\d].*[\\d]$")) {
			throw new JobSchedulerException("[" + fileage + "] is no valid file age");
		}
		String[] timeArray = fileage.split(":");
		if (timeArray.length > 3) {
			throw new JobSchedulerException("[" + fileage + "] is no valid file age");
		}
		for (int i = 0; i < timeArray.length; i++) {
			if (!timeArray[i].matches("[\\d]+")) {
				throw new JobSchedulerException("[" + fileage + "] is no valid file age");
			}
		}
		long hours = Long.parseLong(timeArray[0]);
		long minutes = Long.parseLong(timeArray[1]);
		long seconds = 0;
		if (timeArray.length > 2) {
			seconds = Long.parseLong(timeArray[2]);
		}
		age = hours * 3600000 + minutes * 60000 + seconds * 1000;
		return age;
	}

	private long calculateFileSize(final String filesize) throws Exception {
		long size;
		if (filesize == null || filesize.trim().isEmpty()) {
			return -1;
		}
		if (filesize.matches("-1")) {
			return -1;
		}
		if (filesize.matches("[\\d]+")) {
			size = Long.parseLong(filesize);
		} else {
			if (filesize.matches("^[\\d]+[kK][bB]$")) {
				size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024;
			} else if (filesize.matches("^[\\d]+[mM][bB]$")) {
				size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024;
			} else if (filesize.matches("^[\\d]+[gG][bB]$")) {
				size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024 * 1024;
			} else {
				throw new JobSchedulerException("[" + filesize + "] is no valid file size");
			}
		}
		return size;
	}

	private String substituteFirstFilename(String targetFilename, final String original) throws Exception {
		Matcher matcher = Pattern.compile("\\[filename:([^\\]]*)\\]").matcher(targetFilename);
		if (matcher.find()) {
			if ("".equals(matcher.group(1))) {
				targetFilename = targetFilename.replaceFirst("\\[filename:\\]", original);
			} else if ("lowercase".equals(matcher.group(1))) {
				targetFilename = targetFilename.replaceFirst("\\[filename:lowercase\\]", original.toLowerCase());
			} else if ("uppercase".equals(matcher.group(1))) {
				targetFilename = targetFilename.replaceFirst("\\[filename:uppercase\\]", original.toUpperCase());
			}
		}
		return targetFilename;
	}

	private String substituteAllFilename(String targetFilename, final String original) throws Exception {
		String temp = substituteFirstFilename(targetFilename, original);
		while (!targetFilename.equals(temp)) {
			targetFilename = temp;
			temp = substituteFirstFilename(targetFilename, original);
		}
		return temp;
	}

	private String substituteFirstDate(String targetFilename) throws Exception {
		final String conVarName = "[date:";
		try {
			if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
				int posBegin = targetFilename.indexOf(conVarName);
				if (posBegin > -1) {
					int posEnd = targetFilename.indexOf("]", posBegin + 6);
					if (posEnd > -1) {
						String strDateMask = targetFilename.substring(posBegin + 6, posEnd);
						if (strDateMask.isEmpty()) {
							strDateMask = SOSOptionTime.dateTimeFormat;
						}
						String strDateTime = SOSOptionTime.getCurrentTimeAsString(strDateMask);
						String strT = (posBegin > 0 ? targetFilename.substring(0, posBegin) : "") + strDateTime;
						if (targetFilename.length() > posEnd) {
							strT += targetFilename.substring(posEnd + 1);
						}
						targetFilename = strT;
					}
				}
			}
			return targetFilename;
		} catch (Exception e) {
			throw new RuntimeException("error substituting [date:]: " + e.getMessage());
		}
	}

	private String substituteAllDate(String targetFilename) throws Exception {
		String temp = substituteFirstDate(targetFilename);
		while (!targetFilename.equals(temp)) {
			targetFilename = temp;
			temp = substituteFirstDate(targetFilename);
		}
		return temp;
	}

	private String substituteFirstDirectory(String target, String source) throws Exception {
		try {
			File sourceFile = new File(source);
			if (!sourceFile.isDirectory()) {
				source = sourceFile.getParent();
			}
			source = source.replaceAll("\\\\", "/");
			target = target.replaceAll("\\\\", "/");
			Pattern p = Pattern.compile("\\[directory:(-[\\d]+|[\\d]*)\\]");
			Matcher m = p.matcher(target);
			if (m.find()) {
				String substitute = "";
				if (m.group(1).isEmpty() || "0".equals(m.group(1)) || "-0".equals(m.group(1))) {
					substitute = source;
				} else {
					int depth = Integer.valueOf(m.group(1)).intValue();
					StringTokenizer st = new StringTokenizer(source, "/");
					int absDepth = depth < 0 ? -depth : depth;
					if (absDepth >= st.countTokens()) {
						substitute = source;
					} else {
						String[] dirs = new String[st.countTokens()];
						int n = 0;
						while (st.hasMoreTokens()) {
							dirs[n++] = st.nextToken();
						}
						if (depth > 0) {
							while (depth > 0) {
								substitute = dirs[--depth] + "/" + substitute;
							}
						} else if (depth < 0) {
							while (depth < 0) {
								substitute = substitute + dirs[dirs.length + depth++] + "/";
							}
						}
					}
				}
				if (substitute.charAt(substitute.length() - 1) == '/') {
					substitute = substitute.substring(0, substitute.length() - 1);
				}
				target = target.replaceFirst("\\[directory:[^\\]]*\\]", substitute);
			}
			return target;
		} catch (Exception e) {
			throw new JobSchedulerException("error substituting [directory]: " + e.getMessage());
		}
	}

	private String substituteAllDirectory(String target, final String source) throws Exception {
		String temp = substituteFirstDirectory(target, source);
		while (!target.equals(temp)) {
			target = temp;
			temp = substituteFirstDirectory(target, source);
		}
		return temp;
	}

	public String replaceGroups(final String input, final String replacing, final String replacements)
			throws Exception {
		if (replacements == null) {
			throw new RuntimeException("replacements missing: 0 replacements defined");
		}
		return replaceGroups(input, replacing, replacements.split(";"));
	}

	public String replaceGroups(final String pstrSourceString, final String replacing, final String[] replacements)
			throws Exception {
		String result = "";
		if (replacements == null) {
			throw new RuntimeException("replacements missing: 0 replacements defined");
		}
		Pattern p = Pattern.compile(replacing);
		Matcher m = p.matcher(pstrSourceString);
		if (!m.find()) {
			return pstrSourceString;
		}
		int intGroupCount = m.groupCount();
		if (replacements.length < intGroupCount) {
			throw new RuntimeException("replacements missing: " + replacements.length + " replacement(s) defined but "
					+ intGroupCount + " groups found");
		}
		if (intGroupCount == 0) {
			result = pstrSourceString.substring(0, m.start()) + replacements[0] + pstrSourceString.substring(m.end());
		} else {
			int index = 0;
			for (int i = 1; i <= intGroupCount; i++) {
				int intStart = m.start(i);
				if (intStart >= 0) {
					String strRepl = replacements[i - 1].trim();
					if (!strRepl.isEmpty()) {
						if (strRepl.contains("\\")) {
							strRepl = strRepl.replaceAll("\\\\-", "");
							for (int j = 1; j <= intGroupCount; j++) {
								strRepl = strRepl.replaceAll("\\\\" + j, m.group(j));
							}
						}
						result += pstrSourceString.substring(index, intStart) + strRepl;
					}
				}
				index = m.end(i);
			}
			result += pstrSourceString.substring(index);
		}
		return result;
	}

	private boolean has(final int flags, final int f) {
		return (flags & f) > 0;
	}

	public String getReplacementFilename(final String input, final String replacing, final String replacements)
			throws Exception {
		String targetFilename = input;

		targetFilename = replaceGroups(targetFilename, replacing, replacements.split(";"));
		targetFilename = substituteAllDate(targetFilename);
		targetFilename = substituteAllFilename(targetFilename, input);
		return targetFilename;

	}

	private boolean wipe(final File file) {
		try {
			RandomAccessFile rwFile = new RandomAccessFile(file, "rw");
			byte[] bytes = new byte[(int) rwFile.length()];
			int i = 0;
			while ((bytes[i++] = (byte) rwFile.read()) != -1) {
				//
			}
			rwFile.seek(0);
			for (i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) (Math.random() * 10 % 9);
			}
			rwFile.write(bytes);
			rwFile.close();
			LOGGER.debug("Deleting file");
			boolean rc = file.delete();
			LOGGER.debug("rc: " + rc);
			return rc;
		} catch (Exception e) {
			LOGGER.warn("Failed to wipe file: " + e);
			return false;
		}
	}

	public boolean copyFile(final File source, final File dest, final boolean append) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		try {
			if (LOGGER != null) {
				LOGGER.debug("Copying file " + source.getAbsolutePath() + " with buffer of " + BUFF_SIZE + " bytes");
			}
			in = new FileInputStream(source);
			out = new FileOutputStream(dest, append);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
			if (LOGGER != null) {
				LOGGER.debug("File " + source.getAbsolutePath() + " with buffer of " + BUFF_SIZE + " bytes");
			}
			return true;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

}