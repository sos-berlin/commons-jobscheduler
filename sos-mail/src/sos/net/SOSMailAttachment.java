package sos.net;


import java.io.File;


/**
 *
 * @version $Id: SOSMailAttachment.java 1849 2006-03-22 08:52:18Z gb $
 */


public class SOSMailAttachment {

         private String contentType="";
         private String encoding="";
         private String charset="";
         private File file=null;

        private String filename;
        private String fileExtension;

        private byte[] content;

        private String contentid;         
         
            public SOSMailAttachment( SOSMail sosmail, File f){
               file = f;
               contentType = sosmail.getAttachmentContentType();
               if (contentType.length()==0){
                  contentType = sosmail.getContentType();
               }

               encoding = sosmail.getAttachmentEncoding();
               if (encoding.length()==0){
                  encoding = sosmail.getEncoding();
               }
            
               charset = sosmail.getAttachmentCharset();
               if (charset.length()==0){
                  charset = sosmail.getCharset();
               }
        
            }
     
            public SOSMailAttachment() {}

            public String getCharset() {
              return charset;
            }
     
            public String getContentType() {
              return contentType;
            }
     
              public String getEncoding() {
                return encoding;
              }
     
              public File getFile() {
                return file;
              }
              public void setCharset(String charset) {
                if (charset != null && charset.length() > 0){    
                this.charset = charset;
                }
              }
              public void setContentType(String content_type) {
                if (content_type != null && content_type.length() > 0){    
                  this.contentType = content_type;
                }
              }
              public void setEncoding(String encoding) {
                if (encoding != null && encoding.length() > 0){    
                  this.encoding = encoding;
                }
              }
              
            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public byte[] getContent() {
                return content;
            }

            public void setContent(byte[] content) {
                this.content = content;
            }

            public String getContentid() {
                return contentid;
            }

            public void setContentid(String contentid) {
                this.contentid = contentid;
            }

            /**
             * @return Returns the fileExtension.
             */
            public final String getFileExtension() {
                return fileExtension;
            }

            /**
             * @param fileExtension The fileExtension to set.
             */
            public final void setFileExtension(String fileExtension) {
                this.fileExtension = fileExtension;
            }
            
              
        }
