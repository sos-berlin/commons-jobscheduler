package com.sos.graphviz;

import com.google.common.io.Files;
import com.sos.graphviz.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GraphIO {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphIO.class);

    public static final String name = System.getProperty("os.name");
    public static final boolean isWindows = name.startsWith("Windows");
	private static final String DOT = isWindows ? "dot.exe" : "dot";
	
	private String tempDir = System.getProperty("java.io.tmpdir");
    private String dotDir = null;

    private final Graph graph;

	public GraphIO(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Writes the graph's image in a file.

     * @param  type The type of file.
     * @param  file A Filename to where we want to write.
	 */
	public void writeGraphToFile(FileType type, String file) throws IOException {
		File to = new File(file);
		writeGraphToFile(type, to);
	}

	/**
	 * Writes the graph's image in a file.
	 *
	 * @param  type The type of file.
	 * @param  to A File object to where we want to write.
	 */
	public void writeGraphToFile(FileType type, File to) throws IOException {
		logger.debug("Write output to file " + to.getAbsolutePath() + ".");
		FileOutputStream fos = new FileOutputStream(to);
        File dot = writeDotSourceToTemporaryFile(graph.getSource());
		fos.write(getGraph(dot,type));
		fos.close();
        if(getDotDir()!=null) {
            // String targetName = replaceLast(to.getName(),Files.getFileExtension(to.getName()),"dot"); // getFileExtension nicht in Guava 10,0
            String targetName = replaceLast(to.getName(),getFileExtension(to.getName()),"dot");
            File targetFile = new File(getDotDir(),targetName);
            logger.debug("try to move {} to {}.", dot.getAbsolutePath(), targetFile.getAbsolutePath());
            Files.move(dot, targetFile);
        } else
            deleteDotFile(dot);
	}

    private static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        }
        return string;
    }

    private String getFileExtension(String file) {
        int i = file.lastIndexOf(".");
        return (i > 0) ? file.substring(i+1) : "???";
    }

    private void deleteDotFile(File dotFile) {
        if (dotFile != null) {
            if (dotFile.delete() == false)
                logger.warn(dotFile.getAbsolutePath() + " could not be deleted!");
        }
    }

	/**
	 * Returns the graph as an image in binary format.
	 *
	 * @param dotFile The file with the dotContent of the graph.
	 * @param type Type of the output image to be produced, e.g.: gif, dot, svg.
	 * @return A byte array containing the image of the graph.
	 */
	private byte[] getGraph(File dotFile, FileType type) {
		byte[] imgStream = null;
        if (dotFile != null) {
            imgStream = getImgStream(dotFile, type.name());
            return imgStream;
        }
        return null;
	}

	/**
	 * It will call the external dot program, and return the image in binary
	 * format.
	 * 
	 * @param dot
	 *            Source of the graph (in dot language).
	 * @param type
	 *            Type of the output image to be produced, e.g.: gif, dot, fig,
	 *            pdf, ps, svg, png.
	 * @return The image of the graph in .gif format.
	 */
	private byte[] getImgStream(File dot, String type) {
		File img;
		byte[] img_stream = null;

		try {
			img = File.createTempFile("graph_", "." + type, new File(getTempDir()));
			Runtime rt = Runtime.getRuntime();

			// patch by Mike Chenault
			String[] args = { DOT, "-T" + type, dot.getAbsolutePath(), "-o", img.getAbsolutePath() };
			Process p = rt.exec(args);
            String cmd = getCommandString(args);
            logger.info("Calling: " + cmd);

            InputStream stderrIs = p.getErrorStream();
            InputStreamReader stderrReader = new InputStreamReader(stderrIs);
            BufferedReader stderr = new BufferedReader(stderrReader);

            String line = null;
            while ( (line = stderr.readLine()) != null) {
                if (line.startsWith("Warning:")) {
                    logger.warn(line);
                } else {
                    logger.error(line);
                }
            }
            int exitVal = p.waitFor();
            logger.info("Process ends with cc=" + exitVal);
            if (exitVal != 0)
                throw new RuntimeException(cmd + " ends with cc=" + exitVal);

			FileInputStream in = new FileInputStream(img.getAbsolutePath());
			img_stream = new byte[in.available()];
			in.read(img_stream);

			// Close it if we need to
			if (in != null)
				in.close();

			if (img.delete() == false)
				System.err.println("Warning: " + img.getAbsolutePath() + " could not be deleted!");
		} catch (java.io.IOException ioe) {
			System.err.println("Error:    in I/O processing of tempfile in dir " + getTempDir() + "\n");
			System.err.println("       or in calling external command");
			ioe.printStackTrace();
		} catch (java.lang.InterruptedException ie) {
			System.err.println("Error: the execution of the external program was interrupted");
			ie.printStackTrace();
		}

		return img_stream;
	}

    private String getCommandString(String[] args) {
        StringBuffer b = new StringBuffer();
        for(String s : args) {
            b.append(s + " ");
        }
        return b.toString();
    }

	/**
	 * Writes the source of the graph in a file, and returns the written file as
	 * a File object.
	 * 
	 * @param content
	 *            Source of the graph (in dot language).
	 * @return The file (as a File object) that contains the source of the
	 *         graph.
	 */
	private File writeDotSourceToTemporaryFile(String content) throws java.io.IOException {
		File temp = File.createTempFile("graph_", ".dot.tmp", new File(getTempDir()));
        try {
			FileWriter fout = new FileWriter(temp);
			fout.write(content);
			fout.close();
		} catch (Exception e) {
            logger.error("Error: I/O error while writing the dot source to temp file {}.", temp.getAbsolutePath(), e);
			return null;
		}
		return temp;
	}

	public String getTempDir() {
		return this.tempDir;
	}

	public String getDotDir() {
		return this.dotDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public void setDotDir(String dotDir) {
		this.dotDir = dotDir;
	}

}

