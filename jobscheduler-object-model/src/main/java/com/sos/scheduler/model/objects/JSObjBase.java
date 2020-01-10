package com.sos.scheduler.model.objects;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerHotFolder;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
@XmlTransient
public class JSObjBase implements Comparable<JSObjBase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjBase.class);
    protected static final String conEMPTY = "";
    protected static final String conNO = "no";
    protected static final String conYES = "yes";
    protected static final String conHtmlBR = "<br/>";
    @XmlTransient
    protected SchedulerObjectFactory objFactory = null;
    @XmlTransient
    private ISOSVirtualFile hotFolderSrc = null;
    @XmlTransient
    protected JAXBElement<JSObjBase> objJAXBElement = null;
    @XmlTransient
    private boolean flgIsDirty = false;
    @XmlTransient
    protected String strFileNameExtension = "";
    @XmlTransient
    protected ISOSVirtualFile objVirtualFile = null;
    @XmlTransient
    protected boolean flgIsInit = false;

    public JSObjBase() {
        //
    }

    @XmlTransient
    public void setInit(final boolean flgF) {
        flgIsInit = flgF;
    }

    public boolean canUpdate() {
        return flgIsInit == false;
    }

    @XmlTransient
    public void setDirty(final boolean flgF) {
        flgIsDirty = flgF;
    }

    public boolean isEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    public void setDirty() {
        flgIsDirty = true;
    }

    public boolean isDirty() {
        return flgIsDirty;
    }

    public void setParent(final SchedulerObjectFactory objParent) {
        objFactory = objParent;
    }

    public Object unMarshal(final File pobjFile) {
        return objFactory.unMarshall(pobjFile);
    }

    public Object unMarshal(final InputStream pobjInputStream) {
        return objFactory.unMarshall(pobjInputStream);
    }

    public Object unMarshal(final String pobjString) {
        return objFactory.unMarshall(pobjString);
    }

    public Object unMarshal(final ISOSVirtualFile pobjVirtualFile) {
        return objFactory.unMarshall(pobjVirtualFile);
    }

    public Object marshal(final Object objO, final File objF) {
        return objFactory.marshal(objO, objF);
    }

    public Object marshal(final File objF) {
        Object objO = null;
        if (objJAXBElement == null) {
            objO = this;
        } else {
            objJAXBElement.setValue(this);
            objO = objJAXBElement;
        }
        return marshal(objO, objF);
    }

    public String marshal() {
        Object objO = null;
        if (objJAXBElement == null) {
            objO = this;
        } else {
            objJAXBElement.setValue(this);
            objO = objJAXBElement;
        }
        return objFactory.marshal(objO);
    }

    public Object toXMLFile(final Object objO, final ISOSVirtualFile pobjVirtualFile) {
        return objFactory.toXMLFile(objO, pobjVirtualFile);
    }

    public Object toXMLFile(final ISOSVirtualFile pobjVirtualFile) {
        Object objO = null;
        if (objJAXBElement == null) {
            objO = this;
        } else {
            objJAXBElement.setValue(this);
            objO = objJAXBElement;
        }
        return toXMLFile(objO, pobjVirtualFile);
    }

    public Object save() {
        return toXMLFile(hotFolderSrc);
    }

    public Object saveAs(final String pstrSaveAsFileName) {
        return null;
    }

    public Object rename(final String pstrRenameFileName) {
        return null;
    }

    public Object toXMLFile() {
        return toXMLFile(hotFolderSrc);
    }

    public String toXMLString(final Object objO) {
        return objFactory.toXMLString(objO);
    }

    public String toXMLString() {
        String xmlString = null;
        if (objJAXBElement == null) {
            xmlString = objFactory.toXMLString(this);
        } else {
            objJAXBElement.setValue(this);
            xmlString = objFactory.toXMLString(objJAXBElement);
        }
        return xmlString;
    }

    @XmlTransient
    public void setHotFolderSrc(final ISOSVirtualFile pobjVirtualFile) {
        hotFolderSrc = pobjVirtualFile;
    }

    public ISOSVirtualFile getHotFolderSrc() {
        return hotFolderSrc;
    }

    @Override
    public int compareTo(final JSObjBase o) {
        int compareRet = 0;
        boolean thisIsFolder = "SchedulerHotFolder".equals(this.getClass().getSimpleName());
        boolean oIsFolder = o instanceof SchedulerHotFolder;
        if (thisIsFolder && !oIsFolder) {
            compareRet = -1;
        } else if (!thisIsFolder && oIsFolder) {
            compareRet = 1;
        } else if (this.getHotFolderSrc() == null) {
            compareRet = 1;
        } else if (o.getHotFolderSrc() == null) {
            compareRet = -1;
        } else {
            String hotFolderSrcName1 = this.getHotFolderSrc().getName().toLowerCase();
            String hotFolderSrcName2 = o.getHotFolderSrc().getName().toLowerCase();
            compareRet = hotFolderSrcName1.compareTo(hotFolderSrcName2);
        }
        return compareRet;
    }

    /** This method has to be implemented in this class, because of the Override
     * of the compareTo Method above Sonar Rule: "equals(Object obj)" should be
     * overridden along with the "compareTo(T obj)" method */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** This method has to be implemented in this class, because of the Override
     * of the equals Method above Sonar Rule: "equals(Object obj)" and
     * "hashCode()" should be overridden in pairs */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setObjectFieldsFrom(final JSObjBase origObj) {
        Field[] fields;
        Class<?> origClass = origObj.getClass();
        if (!origClass.isAssignableFrom(this.getClass())) {
            throw new JobSchedulerException(String.format("%1$s must be a subclass of %2$s", this.getClass().getName(), origClass.getName()));
        }
        fields = origClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                // to see private and protected fields too
                field.setAccessible(true);
                if (field.isAccessible()) {
                    // final fields couldn't set and throws
                    // IllegalAccessException
                    // but that's ok because we don't want set final fields
                    field.set(this, field.get(origObj));
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("", new JobSchedulerException("IllegalArgumentException", e));
            } catch (IllegalAccessException e) {
                // this catch block is reached for final fields (that's ok)
            }
        }
    }

    public boolean getYesOrNo(final String yesOrNo) {
        if (yesOrNo != null) {
            String work = yesOrNo.toLowerCase();
            return "1".equals(work) || "yes".equals(work) || "true".equals(work) || "ja".equals(work);
        }
        return false;
    }

    public String setYesOrNo(final boolean pflgV) {
        if (pflgV) {
            return "yes";
        } else {
            return "no";
        }
    }

    public String getObjectName() {
        String name = this.getHotFolderSrc().getName();
        int i = name.indexOf(strFileNameExtension);
        if (i != -1) {
            name = name.substring(0, name.indexOf(strFileNameExtension));
        }
        name = new File(name).getName();
        return name;
    }

    public String getObjectNameAndTitle() {
        String strT = getObjectName();
        String strV = getTitle();
        if (strV != null && !strV.isEmpty()) {
            strT += " - " + getTitle();
        }
        return strT;
    }

    public static String notNull(final String pstrS) {
        String strR = pstrS;
        if (strR == null) {
            strR = "";
        }
        return strR;
    }

    public String getTitle() {
        return "";
    }

    public boolean isEnabled() {
        return true;
    }

    protected void changeSourceName(final String pstrName) {
        ISOSVirtualFile objF = getHotFolderSrc();
        if (objF != null) {
            String name = objF.getName();
            name = name.replace(getObjectName(), pstrName);
            ISOSVirtualFile objNF = objF.getHandler().getFileHandle(name);
            setHotFolderSrc(objNF);
        }
    }

    protected String avoidNull(final String pstrV) {
        if (pstrV == null) {
            return "";
        }
        return pstrV;
    }

    protected String bigInt2String(final BigInteger pbigI) {
        if (pbigI != null) {
            long lngT = pbigI.longValue();
            return String.valueOf(lngT);
        }
        return "";
    }

    protected BigInteger int2BigInteger(final int pintVal) {
        return BigInteger.valueOf(pintVal);
    }

    protected String getQuoted(final String pstrVal) {
        return "\"" + pstrVal.trim() + "\"";
    }

    public static final String escapeHTML(final String s) {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case 'à':
                sb.append("&agrave;");
                break;
            case 'À':
                sb.append("&Agrave;");
                break;
            case 'â':
                sb.append("&acirc;");
                break;
            case 'Â':
                sb.append("&Acirc;");
                break;
            case 'ä':
                sb.append("&auml;");
                break;
            case 'Ä':
                sb.append("&Auml;");
                break;
            case 'å':
                sb.append("&aring;");
                break;
            case 'Å':
                sb.append("&Aring;");
                break;
            case 'æ':
                sb.append("&aelig;");
                break;
            case 'Æ':
                sb.append("&AElig;");
                break;
            case 'ç':
                sb.append("&ccedil;");
                break;
            case 'Ç':
                sb.append("&Ccedil;");
                break;
            case 'é':
                sb.append("&eacute;");
                break;
            case 'É':
                sb.append("&Eacute;");
                break;
            case 'è':
                sb.append("&egrave;");
                break;
            case 'È':
                sb.append("&Egrave;");
                break;
            case 'ê':
                sb.append("&ecirc;");
                break;
            case 'Ê':
                sb.append("&Ecirc;");
                break;
            case 'ë':
                sb.append("&euml;");
                break;
            case 'Ë':
                sb.append("&Euml;");
                break;
            case 'ï':
                sb.append("&iuml;");
                break;
            case 'Ï':
                sb.append("&Iuml;");
                break;
            case 'ô':
                sb.append("&ocirc;");
                break;
            case 'Ô':
                sb.append("&Ocirc;");
                break;
            case 'ö':
                sb.append("&ouml;");
                break;
            case 'Ö':
                sb.append("&Ouml;");
                break;
            case 'ø':
                sb.append("&oslash;");
                break;
            case 'Ø':
                sb.append("&Oslash;");
                break;
            case 'ß':
                sb.append("&szlig;");
                break;
            case 'ù':
                sb.append("&ugrave;");
                break;
            case 'Ù':
                sb.append("&Ugrave;");
                break;
            case 'û':
                sb.append("&ucirc;");
                break;
            case 'Û':
                sb.append("&Ucirc;");
                break;
            case 'ü':
                sb.append("&uuml;");
                break;
            case 'Ü':
                sb.append("&Uuml;");
                break;
            case '®':
                sb.append("&reg;");
                break;
            case '©':
                sb.append("&copy;");
                break;
            case '€':
                sb.append("&euro;");
                break;
            // be carefull with this one (non-breaking white space)
            case ' ':
                sb.append("&nbsp;");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    protected String[] arrayListToStringArray(final List<String> pobjArray) {
        String[] strA = new String[pobjArray.size()];
        return pobjArray.toArray(strA);
    }

}