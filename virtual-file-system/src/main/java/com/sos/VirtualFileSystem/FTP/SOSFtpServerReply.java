/*
 * ftp4j - A pure Java FTP client library Copyright (C) 2008-2010 Carlo
 * Pelliccia (www.sauronsoftware.it) This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License version 2.1, as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License 2.1 for
 * more details. You should have received a copy of the GNU Lesser General
 * Public License version 2.1 along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sos.VirtualFileSystem.FTP;

import java.util.ArrayList;

import com.sos.JSHelper.Basics.JSToolBox;

/** This class represents FTP server replies in a manageable object oriented way.
 * 
 * @author Carlo Pelliccia */
public class SOSFtpServerReply extends JSToolBox {

    /** The reply code. */
    private int code = 0;

    /** The reply message(s). */
    private String[] messages;

    /** Build the reply.
     * 
     * @param code1 The code of the reply.
     * @param message The textual message(s) in the reply. */
    public SOSFtpServerReply(final int code1, final String[] messages1) {
        code = code1;
        messages = messages1;
    }

    public SOSFtpServerReply(final String messages1) {
        ParseServerReply(messages1);
    }

    public void ParseServerReply(final String pstrReply) {
        if (isEmpty(pstrReply)) {
            return;
        }

        int code1 = 0;
        ArrayList<String> messages1 = new ArrayList<String>();
        String[] strM = pstrReply.split("\r\n");
        for (String statement : strM) {
            if (statement.startsWith("\n")) {
                statement = statement.substring(1);
            }
            int l = statement.length();
            if (code1 == 0 && l < 3) {
                throw new FTPIllegalReplyException();
            }
            int aux;
            try {
                aux = Integer.parseInt(statement.substring(0, 3));
            } catch (Exception e) {
                if (code1 == 0) {
                    throw new FTPIllegalReplyException();
                } else {
                    aux = 0;
                }
            }
            if (code1 != 0 && aux != 0 && aux != code1) {
                throw new FTPIllegalReplyException();
            }
            if (code1 == 0) {
                code1 = aux;
            }
            if (aux > 0) {
                if (l > 3) {
                    char s = statement.charAt(3);
                    String message = statement.substring(4, l);
                    messages1.add(message);
                    if (s == ' ') {
                        break;
                    } else if (s == '-') {
                        continue;
                    } else {
                        throw new FTPIllegalReplyException();
                    }
                } else if (l == 3) {
                    break;
                } else {
                    messages1.add(statement);
                }
            } else {
                messages1.add(statement);
            }
        }

        int size = messages1.size();

        String[] m = new String[size];
        for (int i = 0; i < size; i++) {
            m[i] = messages1.get(i);
        }

        code = code1;
        messages = m;
    }

    /** Returns the code of the reply.
     * 
     * @return The code of the reply. */
    public int getCode() {
        return code;
    }

    /** Returns true if the code of the reply is in the range of success codes
     * (2**).
     * 
     * @return true if the code of the reply is in the range of success codes
     *         (2**).
     * 
     *         see http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes */

    public boolean isSuccessCode() {
        int aux = code - 200;
        return aux >= 0 && aux < 100;
    }

    public boolean isErrorCode() {
        return code >= 500;
    }

    /** Returns the textual message(s) of the reply.
     * 
     * @return The textual message(s) of the reply. */
    public String[] getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append(" [code=");
        buffer.append(code);
        buffer.append(", message=");
        for (int i = 0; i < messages.length; i++) {
            if (i > 0) {
                buffer.append(" ");
            }
            buffer.append(messages[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }

}
