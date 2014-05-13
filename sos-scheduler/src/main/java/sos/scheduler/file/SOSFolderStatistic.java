package sos.scheduler.file;


/**
 * @author SGLO111
 *
 * 09.12.2003
 * 
 */
 public class SOSFolderStatistic
{
	private long lngNoOfFiles = 0;
	private long lngNoOfFolders = 0;
	private long lngSize = 0;

	private SOSXMLHelper objXML = null;

	public SOSFolderStatistic(SOSXMLHelper pobjXML)
	{
		objXML = pobjXML;
	}
	/**
	 * Returns the lngNoOfFiles.
	 * @return long
	 */
	public long getNoOfFiles()
	{
		return lngNoOfFiles;
	}

	/**
	 * Returns the NoOfFolders.
	 * @return long
	 */
	public long getNoOfFolders()
	{
		return lngNoOfFolders;
	}

	/**
	 * Returns the lngSize.
	 * @return long
	 */
	public long getSize()
	{
		return lngSize;
	}

	/**
	 * Sets the lngNoOfFiles.
	 * @param lngNoOfFiles1 The lngNoOfFiles to set
	 */
	public void setNoOfFiles(long lngNoOfFiles1)
	{
		this.lngNoOfFiles = lngNoOfFiles1;
	}

	/**
	 * Method incrNoOfFiles.
	 */
	public void incrNoOfFiles()
	{
		this.lngNoOfFiles++;
	}

	/**
	 * Sets the lngNoOfFolders.
	 * @param lngNoOfFolders1 The lngNoOfFolders to set
	 */
	public void setNoOfFolders(long lngNoOfFolders1)
	{
		this.lngNoOfFolders = lngNoOfFolders1;
	}

	/**
	 * Method incrNoOfFolders.
	 */
	public void incrNoOfFolders()
	{
		this.lngNoOfFolders++;
	}

	/**
	 * Sets the lngSize.
	 * @param lngSize1 The lngSize to set
	 */
	public void setSize(long lngSize1)
	{
		this.lngSize = lngSize1;
	}

	/**
	 * Method incrSize.
	 * @param lngSize1
	 */
	public void incrSize(long lngSize1)
	{
		this.lngSize += lngSize1;
	}

	/**
	 * Method Cumulate.
	 * @param pobjS
	 */
	public void Cumulate(SOSFolderStatistic pobjS)
	{
		this.lngNoOfFiles += pobjS.getNoOfFiles();
		this.lngNoOfFolders += pobjS.getNoOfFolders();
		this.lngSize += pobjS.getSize();
	}

	/**
	 * Method toXML.
	 * @param strTagName
	 * @throws Exception
	 */
	public void toXML(String strTagName) throws Exception
	{

		if (objXML != null)
		{
			objXML.XMLTag(strTagName);
			objXML.XMLTagV("FileCnt", lngNoOfFiles);
			objXML.XMLTagV("FolderCnt", lngNoOfFolders);
			objXML.XMLTagV("Size", lngSize);
			objXML.XMLTagE(strTagName);
		}

	}
}
