<?xml version='1.0' encoding='ISO-8859-1' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"
  xmlns:sos="http://www.sos-berlin.com/namespace"
  version="1.0">
  <xsl:decimal-format NaN=" " decimal-separator="," grouping-separator="." />
  <xsl:variable name="QtyEditMask">###.###.##0,000 </xsl:variable>
<!--
  <xsl:variable name="ItemNoEditMask">###.###.##0 </xsl:variable>
-->
  <xsl:variable name="ItemNoEditMask">########0 </xsl:variable>
  <xsl:variable name="ServerPathName">http://bes100.schering.de/systems/</xsl:variable>
  <xsl:variable name="BackColor4Details">#FFE0D0</xsl:variable>
  <xsl:variable name="BackColor4MainTable">#FFEEDF</xsl:variable>
  <xsl:variable name="Picture4CollapsedNode">
    <xsl:value-of select="concat($ServerPathName,'Pictures/arrowrt.gif')" />
  </xsl:variable>
             <msxsl:script language="JScript" implements-prefix="sos" >
                            <![CDATA[
                                    var number = 0;
                                    var lang = "en";
                                    function DoNumber() {
                                    	++number;
                                    	return number;
                                    }
                                    function ResetNumber() {
                                    	number = 0;
                                    	return "";
                                    }
                                    function GetNumber() {
                                    	return number;
                                    }
                        function getLanguage(pCompNo){
                        	return "de";
                                      	var CompNo = new String(" ");
                                      	CompNo = pCompNo;
                    	alert ("company = " + CompNo);
                    		if (CompNo == "31") {
                    			return "de";
                    			}
                    		else {
                    			if (CompNo == "174") {
                    				return "en";
                    				}
                    			else {
                    				return "en";
                    				}
                    			}
                        }
                                      function lFormatDate (strNodeValue) {
                                      	var strT = new String(" ");
                                      	strT = strNodeValue;
                                      	if (strT == "") {
                                      		return " ";
                                      		}
                                      	else {
                                      		return strT.substr(6,2) + "." + strT.substr(4,2) + "." + strT.substr(0, 4);
                                      	}
                                      }
                                      function lFormatTime (strNodeValue) {
                                      	var strT = new String(" ");
                                      	strT = strNodeValue;
                                      	if (strT == "") {
                                      		return " ";
                                      		}
                                      	else {
                                      		return strT.substr(0,2) + ":" + strT.substr(2,2) + ":" + strT.substr(4, 2);
                                      	}
                                      }
                            	function MyTest () {
                            		return 0;
                            		}
              function getUoMText(strUoM) {
                    var strT = new String(" ");
              		strT = strUoM;
              		switch (strT) {
              		    case 'ST':
              		        return "pieces";
              		    case 'KG':
              		        return "kilogram";
              		    case 'TST':
              		        return "thousand pieces";
              		    case 'TS':
              		        return "thousand pieces";
              		    case 'G':
              		        return "gram"
              		    case 'M':
              		        return "meter"
              		    default:
              		        return "unit of measure : " + strT;
              		}
              }
              function getItemCategoryText(strItemCat) {
                    var strT = new String(" ");
              		strT = strItemCat;
              		switch (strT) {
            	  		case '01':
            	  			return "raw material";
            	  		case '03':
            	  			return "active ingredience";
            	  		case '04':
            	  			return "intermediate, bulk";
            	  		case '05':
            	  			return "finished goods";
            	  		case '07':
            	  			return "trading goods";
            	  		case '09':
            	  			return "packaging material, glas";
              			case '10':
            //  			return "Konfektionierungsmaterial, bedruckt";
              				return "printed packaging material";
              			case '11':
              				return "unprinted packaging material";
              			case '12':
              				return "wrapping material, glas";
              			case '13':
              				return "wrapping material, non glas";
              			case '25':
              				return "purchased active ingredience";
              			case '27':
              				return "purchased intermediate";
              			case '33':
              				return "active ingredience, free of charge for toll manufacturing";
              			case '34':
              				return "intermediate, free of charge for toll manufacturing";
              			case '36':
              				return "toll manufacturing accounting";
              			case '51':
              				return "intermediate, toll manufacturing";
              			case '52':
              				return "finished good, toll manufacturing";
              			case '60':
              				return "finished good, purchased tm";
              			case '61':
              				return "intermediate, purchased toll manufacturing";
            	  		default:
            	  			return "ItemCategory : " + strT;
              		}
              	}
                            ]]>
  </msxsl:script>
  <xsl:template name="Listenfuss">
</xsl:template>
    <xsl:template name="CreateHeading" >
  </xsl:template>
  <xsl:template name="CreateHTMLIncludes">
    <script>
      <xsl:attribute name="language">Javascript</xsl:attribute>
      <xsl:attribute name="type">text/javascript</xsl:attribute>
      <xsl:attribute name="src">
        <xsl:value-of select="concat($ServerPathName,'includes/elog-html.js')" />
      </xsl:attribute>
    </script>
    <link >
      <xsl:attribute name="rel">stylesheet</xsl:attribute>
      <xsl:attribute name="type">text/css</xsl:attribute>
      <xsl:attribute name="href">
        <xsl:value-of select="concat($ServerPathName,'includes/elog.css')" />
      </xsl:attribute>
    </link >
  </xsl:template>
  <xsl:template name="CreateDisplayIcon" >
    <xsl:param name="ParaNo" />
    <xsl:param name="DetailIcon" select="$Picture4CollapsedNode" />
    <xsl:attribute name="onClick">
      <xsl:value-of select="concat('doExpand(exp', $ParaNo, ',ar', $ParaNo, ')')" />
    </xsl:attribute>
    <xsl:attribute name="href">#nowhere</xsl:attribute>
    <img>
      <xsl:attribute name="src">
        <xsl:value-of select="$DetailIcon" />
      </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="concat('ar',$ParaNo)" />
      </xsl:attribute>
      <xsl:attribute name="border">0</xsl:attribute>
      <xsl:attribute name="alt">click 4 details</xsl:attribute>
    </img>
    <!-- <xsl:text>
         </xsl:text>
    -->
  </xsl:template>
  <xsl:template name="DisplayErrorCode" >
    <xsl:param name="ParaNo"> </xsl:param>
    <xsl:choose>
      <xsl:when test="ERROR/ERRORCODE">
        <p>
          <xsl:attribute name="title">
            <xsl:value-of select="ERROR/SHORTTXT" />
          </xsl:attribute>
          <a>
            <xsl:call-template name="CreateDisplayIcon">
              <xsl:with-param name="ParaNo" select="$ParaNo" />
              <xsl:with-param name="DetailIcon" select="concat($ServerPathName,'Pictures/x-red.gif')" />
            </xsl:call-template>
          </a>
          <xsl:value-of select="ERROR/ERRORCODE" />
        </p>
      </xsl:when>
      <xsl:otherwise>
        <img>
          <xsl:attribute name="src">
            <xsl:value-of select="concat($ServerPathName,'Pictures/check-green.gif')" />
          </xsl:attribute>
          <xsl:attribute name="border">0</xsl:attribute>
          <xsl:attribute name="alt">ok, no errors</xsl:attribute>
        </img>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="CreateErrorDetails" >
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--
           <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      <td />
      <td align="left" colspan="9">
        <br />
        <xsl:choose>
          <xsl:when test="CONFIRMATIONCODE">
            <xsl:value-of select="concat('ErrorText : ',./CONFIRMATIONCODE,' = ',./CONFIRMATIONCODETEXT)" />
          </xsl:when>
          <xsl:when test="ERROR/ERRORCODE">
            <xsl:value-of select="concat('ErrorText : ',./ERROR/SHORTTXT,' = ',./ERROR/ERRORTXT)" />
          </xsl:when>
          <xsl:otherwise />
        </xsl:choose>
        <br />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="CreateManOrdDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--
           <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      <td />
      <td align="left" colspan="8">
        <br />
        <xsl:value-of select="concat('Notice : ',./MONOTICE)" />
        <br />
        <br />
        <xsl:value-of select="concat('Bulk-Date      : ',sos:lFormatDate(string(./BULKDATE)))" />
        <br />
        <xsl:value-of select="concat('Order-No       : ',./ORDERNO)" />
        <br />
        <xsl:value-of select="concat('ISN            : ',./ISN)" />
        <br />
        <xsl:value-of select="concat('DVL-Code       : ',./DVLCODE)" />
        <br />
        <xsl:value-of select="concat('Shipping-Order : ',./SHIPPINGORDERNO)" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="CreateHeadingDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <td align="left" colspan="8">
        <xsl:value-of select="concat('Program      : ',//CREATOR/PROGRAM)" />
        <br />
        <xsl:value-of select="concat('Library      : ',//CREATOR/LIB)" />
        <br />
        <xsl:value-of select="concat('Server       : ',//CREATOR/SERVER)" />
        <br />
        <xsl:value-of select="concat('DV-System    : ',//CREATOR/DVSYSTEM)" />
        <br />
        <xsl:value-of select="concat('JobName      : ',//CREATOR/JOBNAME)" />
        <br />
        <xsl:value-of select="concat('Who          : ',//CREATOR/WHO)" />
        <xsl:if test="//SSTINTERFACE" >
        <br />
        <xsl:value-of select="concat('Sender       : ',//SSTINTERFACE/SENDER)" />
        <br />
        <xsl:value-of select="concat('Receiver     : ',//SSTINTERFACE/RECEIVER)" />
        <br />
        <xsl:value-of select="concat('InterfaceNumb: ',//SSTINTERFACE/SEQUNUMBER)" />
        <br />
        <xsl:value-of select="concat('RecordLength : ',//SSTINTERFACE/RECLENGTH)" />

        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="CreateItemDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--         <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      <td />
      <td align="left" colspan="8">
        <table >
          <colgroup>
            <col width = "5%" />
            <col width = "40%" />
            <col width = "4%" />
            <col width = "5%" />
            <col width = "5%" />
            <col width = "10%" />
            <col width = "5%" />
            <col width = "5%" />
            <col width = "5%" />
          </colgroup>
          <thead >
            <tr >
              <xsl:call-template name="th_ItemNumber" />
              <th>Description</th>
              <xsl:call-template name="th_UoM" />
              <th>Presentation</th>
              <xsl:call-template name="th_ItemCategory" />
              <th title="valid from date">From</th>
              <th title="valid to date">To</th>
            </tr>
          </thead>
          <TBODY>
            <tr bgcolor="#ffffe7">
              <td align="right">
                <xsl:value-of select="format-number(./ITEM/ITEMNO, $ItemNoEditMask)"/>
              </td>
              <td>
                <xsl:value-of select="./ITEM/DESC" />
              </td>
              <td align="center">
                <xsl:value-of select="./ITEM/UNITOFMEASURES/UOM1" />
              </td>
              <td>
                <xsl:value-of select="./ITEM/PRESENTATION" />
              </td>
              <td align="center">
                <xsl:attribute name="title">
                  <xsl:value-of select="sos:getItemCategoryText(string(./ITEM/ITEMCATEGORY))" />
                </xsl:attribute>
                <xsl:value-of select="./ITEM/ITEMCATEGORY" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(./ITEM/VALIDFROM)))" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(./ITEM/VALIDTO)))" />
              </td>
            </tr>
          </TBODY>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="CreateCacheItemDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <xsl:param name="pItemNode"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--         <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      Das ist der Node :<xsl:value-of select="$pItemNode" />
      <br />
      <td />
      <td align="left" colspan="8">
        <table >
          <colgroup>
            <col width = "5%" />
            <col width = "40%" />
            <col width = "4%" />
            <col width = "5%" />
            <col width = "5%" />
            <col width = "10%" />
            <col width = "5%" />
            <col width = "5%" />
            <col width = "5%" />
          </colgroup>
          <thead >
            <tr >
              <xsl:call-template name="th_ItemNumber" />
              <th>Description</th>
              <xsl:call-template name="th_UoM" />
              <th>Presentation</th>
              <xsl:call-template name="th_ItemCategory" />
              <th title="valid from date">From</th>
              <th title="valid to date">To</th>
            </tr>
          </thead>
          <TBODY>
            <tr bgcolor="#ffffe7">
              <td align="right">
                <xsl:value-of select="format-number($pItemNode/ITEMNO, $ItemNoEditMask)"/>
              </td>
              <td>
                <xsl:value-of select="$pItemNode/DESC" />
              </td>
              <td align="center">
                <xsl:value-of select="$pItemNode/UNITOFMEASURES/UOM1" />
              </td>
              <td>
                <xsl:value-of select="$pItemNode/PRESENTATION" />
              </td>
              <td align="center">
                <xsl:attribute name="title">
                  <xsl:value-of select="sos:getItemCategoryText(string($pItemNode/ITEMCATEGORY))" />
                </xsl:attribute>
                <xsl:value-of select="$pItemNode/ITEMCATEGORY" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(/VALIDFROM)))" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(/VALIDTO)))" />
              </td>
            </tr>
          </TBODY>
        </table>
      </td>
    </tr>
  </xsl:template>


  <xsl:template name="CreateOrderDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--         <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      <td />
      <td align="left" colspan="8">
        <table >
          <colgroup>
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "3%" />
            <col width = "30%" />
          </colgroup>
          <thead >
            <tr >
              <xsl:call-template name="th_ShippingOrderNo" />
              <th>CustomerNo</th>
              <th>BookingCode</th>
              <th>TermsOf<br/>Delivery</th>
              <th>OrderType</th>
              <th>TypeOf<br/>Shippment</th>
              <th>Destination</th>
            </tr>
          </thead>
          <TBODY>
            <tr bgcolor="#ffffe7">
<!--
              <td align="center">
                <xsl:value-of select="format-number(./ORDERDATA/PEXORDERNO, $ItemNoEditMask)"/>
              </td>
 -->
              <td align="center">
                <xsl:value-of select="./ORDERDATA/PEXORDERNO" />
              </td>
              <td align="center">
                <xsl:attribute name="title">
                  <xsl:value-of select="CUSTOMERNAME" />
                </xsl:attribute>
                <xsl:value-of select="./ORDERDATA/CUSTOMERNO" />
              </td>
              <td align="center">
                <xsl:value-of select="./ORDERDATA/BOOKINGCODE" />
              </td>
              <td align="center">
                <xsl:value-of select="./ORDERDATA/TERMSOFDELIVERY" />
              </td>
              <td align="center">
                <xsl:value-of select="./ORDERDATA/ORDERTYPE" />
              </td>
                <!--
                <xsl:attribute name="title">
                  <xsl:value-of select="sos:getItemCategoryText(string(./ORDERDATA/ITEMCATEGORY))" />
                </xsl:attribute>
                 -->
              <td align="center">
                <xsl:variable name="shipment">
                  <xsl:value-of select="./ORDERDATA/TYPEOFSHIPPMENT" />
                </xsl:variable>
                <!-- was soll das? kb
                <xsl:attribute name="title">
                  <xsl:value-of select="$Shipments//SHIPMENT [@NO = $shipment]" />
                </xsl:attribute>
                -->
                <xsl:value-of select="ORDERDATA/TYPEOFSHIPPMENT" />
<!--
                <xsl:value-of select="$Shipments//SHIPMENT [@NO = $shipment]" />
                <xsl:value-of select="concat(ORDERDATA/TYPEOFSHIPPMENT' = ',$Shipments//SHIPMENT [@NO = $shipment])" />
 -->
              </td>
              <td align="center">
                <xsl:value-of select="./ORDERDATA/DESTINATION" />
              </td>
<!--
              <td align="center">
                <xsl:attribute name="title">
                  <xsl:value-of select="sos:getItemCategoryText(string(./ORDERDATA/ITEMCATEGORY))" />
                </xsl:attribute>
                <xsl:value-of select="./ITEM/ITEMCATEGORY" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(./ORDERDATA/VALIDFROM)))" />
              </td>
              <td align="center">
                <xsl:value-of select="sos:lFormatDate(normalize-space(string(./ORDERDATA/VALIDTO)))" />
              </td>
 -->
            </tr>
          </TBODY>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="CreateProcessDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <!--         <xsl:attribute name="onClick">
           <xsl:value-of select="concat('doExpand(exp',$ParaNo,',ar',$ParaNo')')" />
           </xsl:attribute>
      -->
      <td />
      <td align="left" colspan="8">
        <br />
        <xsl:value-of select="concat('Process-No : ',./PROCES/NO)" />
        <br />
        <br />
        <xsl:value-of select="concat('Valid from : ',sos:lFormatDate(string(./PROCES/VALIDFROM)))" />
        <xsl:value-of select="concat(' Valid to   : ',sos:lFormatDate(string(./PROCES/VALIDTO)))" />
        <br />
        <xsl:value-of select="concat('Routing-No : ',./PROCES/ROUTINGNO)" />
        <br />
        <xsl:value-of select="concat('BoM-No : ',./PROCES/BOMNO)" />
        <br />
        <xsl:value-of select="concat('Gross BatchSize : ',format-number(./PROCES/GROSSBATCHSIZE, $QtyEditMask),' ',./UOM)" />
        <br />
        <xsl:value-of select="concat('Net BatchSize : ',format-number(./PROCES/NETBATCHSIZE, $QtyEditMask),' ',./UOM)" />
        <br />
        <br />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="SetAttribute4ManOrdBookingCode">
    <xsl:param name="BookingCode"> </xsl:param>
    <xsl:attribute name="style">
      <xsl:choose>
        <xsl:when test="$BookingCode = 'A'">
          <xsl:value-of select="concat('color',':','green')" />
        </xsl:when>
        <xsl:when test="$BookingCode = 'C'">
          <xsl:value-of select="concat('color',':','blue')" />
        </xsl:when>
        <xsl:when test="$BookingCode = 'U'">
          <xsl:value-of select="concat('color',':','blue')" />
        </xsl:when>
        <xsl:when test="$BookingCode = 'D'">
          <xsl:value-of select="concat('color',':','red')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('color',':','#404040',';font-style',':','italic')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  <xsl:template name="SetText4ManOrdBookingCode">
    <xsl:param name="BookingCode"> </xsl:param>
    <xsl:choose>
      <xsl:when test="$BookingCode = 'A'">
        <xsl:text>Add/New</xsl:text>
      </xsl:when>
      <xsl:when test="$BookingCode = 'C'">
        <xsl:text>Change/update</xsl:text>
      </xsl:when>
      <xsl:when test="$BookingCode = 'U'">
        <xsl:text>Update/change</xsl:text>
      </xsl:when>
      <xsl:when test="$BookingCode = 'D'">
        <xsl:text>Delete</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="style">
          <xsl:value-of select="concat('font-style',':','normal')" />
        </xsl:attribute>
        <xsl:value-of select="concat('? ',$BookingCode,' ?')" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="CreatePurchaseOrderPosDetails">
    <xsl:param name="ParaNo"> </xsl:param>
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$BackColor4Details" />
      </xsl:attribute>
      <xsl:attribute name="style">display: none; margin-left: +2em</xsl:attribute>
      <xsl:attribute name="ID">
        <xsl:value-of select="concat('exp',$ParaNo)" />
      </xsl:attribute>
      <td  />
      <td align="left" colspan="8">
        Purchase Order :
        <xsl:value-of select="concat(./RECEIPT/REQUISITION,'/',./RECEIPT/REQPOS)" />
        <br/>
        <table >
          <colgroup>
            <col width = "20%" />
            <col width = "10%" />
            <col width = "10%" />
            <col width = "10%" />
          </colgroup>
          <thead >
            <tr >
              <th> </th>
              <th>Requisition</th>
              <th>BAnf</th>
              <th>Purchase Order</th>
            </tr>
          </thead>
          <TBODY>
            <tr bgcolor="#ffffe7">
              <td align="left">Number</td>
              <td align="right">
                <xsl:value-of select="./RECEIPT/IDENT/NO" />
              </td>
              <td align="right">
                <xsl:choose>
                  <xsl:when test="./RECEIPT/BANF/NO">
                    <xsl:value-of select="concat(./RECEIPT/BANF/NO,'/',./RECEIPT/BANF/POS)" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text> </xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <td align="right">
                <xsl:value-of select="concat(./RECEIPT/REQUISITION,'/',./RECEIPT/REQPOS)" />
              </td>
            </tr>
            <tr bgcolor="#ffffe7">
              <td align="left">Created</td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/IDENT/CREATED))" />
              </td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/BANF/RECEIPT/CREATED))" />
              </td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/CREATED))" />
              </td>
            </tr>
            <tr bgcolor="#ffffe7">
              <td align="left">Delivery-Date</td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/IDENT/DTE))" />
              </td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/BANF/DTE))" />
              </td>
              <td align="right">
                <xsl:value-of select="sos:lFormatDate(string(./RECEIPT/DTE))" />
              </td>
            </tr>
            <tr bgcolor="#ffffe7">
              <td align="left">Order-Quantity</td>
              <td align="right">
                <xsl:choose>
                  <xsl:when test="./RECEIPT/IDENT/QTY">
                    <xsl:value-of select="concat(format-number(./RECEIPT/IDENT/QTY, $QtyEditMask),' ',./UOM)" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text> </xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <td align="right">
                <xsl:choose>
                  <xsl:when test="./RECEIPT/BANF/QTY">
                    <xsl:value-of select="concat(format-number(./RECEIPT/BANF/QTY, $QtyEditMask),' ',./UOM)" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text> </xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <td align="right">
                <xsl:value-of select="concat(format-number(./RECEIPT/QTY, $QtyEditMask),' ',./UOM)" />
              </td>
            </tr>
            <tr bgcolor="#ffffe7">
              <td align="left">Delivered-Quantity</td>
              <td align="right" />
              <td align="right" />
              <td align="right">
                <xsl:choose>
                  <xsl:when test="./RECEIPT/ACTQTY">
                    <xsl:value-of select="concat(format-number(./RECEIPT/ACTQTY, $QtyEditMask),' ',./UOM)" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="concat(format-number('0', $QtyEditMask),' ',./UOM)" />
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr bgcolor="#ffffe7">
              <td align="left">Order-Status</td>
              <td align="right" />
              <td align="right" />
              <td align="right">
                <xsl:choose>
                  <xsl:when test="./RECEIPT/STATUS = ''">
                    <xsl:text>open</xsl:text>
                  </xsl:when>
                  <xsl:when test="./RECEIPT/STATUS = '1'">
                    <xsl:text>delivering</xsl:text>
                  </xsl:when>
                  <xsl:when test="./RECEIPT/STATUS = '2'">
                    <xsl:text>closed</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text>open</xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </TBODY>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="td_ItemNumber">
    <xsl:param name="Details"> </xsl:param>
    <td align="right">
      <xsl:attribute name="title">
        <xsl:value-of select="string(./ITEM/DESC)" />
      </xsl:attribute>
<!-- <a> -->
      <xsl:if test="$Details != ''">
        <a>
          <xsl:call-template name="CreateDisplayIcon">
            <xsl:with-param name="ParaNo" select="$Details" />
          </xsl:call-template>
        </a>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="ITEM/ITEMNO" >
          <!-- <xsl:value-of select="ITEM/ITEMNO" /> -->
          <xsl:value-of select="format-number(./ITEM/ITEMNO, $ItemNoEditMask)"/>
        </xsl:when>
        <xsl:when test="ITEMNO" >
          <xsl:value-of select="ITEMNO" />
        </xsl:when>
        <xsl:when test="ITEMNUMBER" >
          <xsl:value-of select="ITEMNUMBER" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>* ??? *</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
<!-- </a> -->
    </td>
  </xsl:template>

  <xsl:template name="td_ItemNumber2">
    <xsl:param name="Details"> </xsl:param>
    <td align="right">
      <xsl:attribute name="title">
        <xsl:value-of select="string(./DESC)" />
      </xsl:attribute>
      <xsl:if test="$Details != ''">
        <a>
          <xsl:call-template name="CreateDisplayIcon">
            <xsl:with-param name="ParaNo" select="$Details" />
          </xsl:call-template>
        </a>
      </xsl:if>
      <xsl:value-of select="format-number(ITEMNO, $ItemNoEditMask)" />
    </td>
  </xsl:template>
  <xsl:template name="td_Quantity">
    <td align="right">
      <xsl:choose>
        <xsl:when test="QTY" >
          <xsl:value-of select="format-number(QTY, $QtyEditMask)" />
        </xsl:when>
        <xsl:when test="MOQTY" >
          <xsl:value-of select="format-number(MOQTY, $QtyEditMask)" />
        </xsl:when>
        <xsl:otherwise />
      </xsl:choose>
    </td>
  </xsl:template>

  <xsl:template name="td_ShippingOrder">
    <xsl:param name="Details"> </xsl:param>
    <td align="center">
      <xsl:attribute name="title">
        <xsl:value-of select="'Click4Details PEX OrderNo'" />
      </xsl:attribute>
      <a>
      <xsl:if test="$Details != ''">
          <xsl:call-template name="CreateDisplayIcon">
            <xsl:with-param name="ParaNo" select="$Details" />
          </xsl:call-template>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="ORDERDATA/PEXORDERNO" >
          <xsl:value-of select="ORDERDATA/PEXORDERNO" />
        </xsl:when>
        <xsl:when test="ORDERDATA/ORDERNO" >
          <xsl:value-of select="format-number(ORDERDATA/ORDERNO, $ItemNoEditMask)" />
        </xsl:when>
        <xsl:when test="ORDERNO" >
          <xsl:value-of select="ORDERNO" />
        </xsl:when>
        <xsl:when test="ORDER/ORDERNO" >
          <xsl:value-of select="ORDER/ORDERNO" />
        </xsl:when>
        <xsl:when test="SHIPPINGORDERNO" >
          <xsl:value-of select="SHIPPINGORDERNO" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>** ? ? ? **</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      </a>
    </td>
  </xsl:template>

  <xsl:template name="td_Process">
    <xsl:param name="Details"> </xsl:param>
    <td align="center">
      <xsl:attribute name="title">
        <xsl:value-of select="concat('Routing-No : ',./PROCES/ROUTINGNO,' BoM-No : ',./PROCES/BOMNO)" />
      </xsl:attribute>
      <a>
        <xsl:call-template name="CreateDisplayIcon">
          <xsl:with-param name="ParaNo" select="$Details" />
        </xsl:call-template>
      </a>
      <xsl:value-of select="PROCESSNO" />
    </td>
  </xsl:template>
  <xsl:template                                                   name="td_UoM">
    <td align="center">
      <xsl:choose>
        <xsl:when test="./UNITOFMEASURES/UOM1" >
          <xsl:attribute name="title">
            <xsl:choose>
              <xsl:when test="./UNITOFMEASURES/UOM2" >
                <xsl:value-of select="concat(sos:getUoMText(string(./UNITOFMEASURES/UOM1)),', UoM2 = ',sos:getUoMText(string(./UNITOFMEASURES/UOM2)))" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="sos:getUoMText(string(./UNITOFMEASURES/UOM1))" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:value-of select="./UNITOFMEASURES/UOM1" />
        </xsl:when>
        <xsl:when test="UOM" >
          <xsl:attribute name="title">
            <xsl:value-of select="sos:getUoMText(string(UOM))" />
          </xsl:attribute>
          <xsl:value-of select="UOM" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="title">
            <xsl:choose>
              <xsl:when test="./ITEM/UNITOFMEASURES/UOM2" >
                <xsl:value-of select="concat(sos:getUoMText(string(./ITEM/UNITOFMEASURES/UOM1)),', UoM2 = ',sos:getUoMText(string(./ITEM/UNITOFMEASURES/UOM2)))" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="sos:getUoMText(string(./ITEM/UNITOFMEASURES/UOM1))" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:value-of select="./ITEM/UNITOFMEASURES/UOM1" />
        </xsl:otherwise>
      </xsl:choose>
    </td>
  </xsl:template>
  <xsl:template name="td_ItemDescription">
    <td align="left">
      <xsl:attribute name="title">
        <xsl:value-of select="string(./ITEM/ITEMNO)" />
      </xsl:attribute>
      <xsl:value-of select="./ITEM/DESC" />
    </td>
  </xsl:template>
  <xsl:template name="td_ItemDescription2">
    <td align="left">
      <xsl:attribute name="title">
        <xsl:value-of select="string(./ITEMNO)" />
      </xsl:attribute>
      <xsl:value-of select="./DESC" />
    </td>
  </xsl:template>
  <xsl:template name="td_ItemCategory">
    <td align="center">
      <xsl:attribute name="title">
        <xsl:value-of select="sos:getItemCategoryText(string(./ITEM/ITEMCATEGORY))" />
      </xsl:attribute>
      <xsl:value-of select="./ITEM/ITEMCATEGORY" />
    </td>
  </xsl:template>
  <xsl:template name="td_ItemCategory2">
    <td align="center">
      <xsl:attribute name="title">
        <xsl:value-of select="sos:getItemCategoryText(string(ITEMCATEGORY))" />
      </xsl:attribute>
      <xsl:value-of select="ITEMCATEGORY" />
    </td>
  </xsl:template>
  <!-- Table Headers -->
  <xsl:template name="th_BookingCode">
    <th align="center" title="Type of process the order">
      Booking
      <br />
      Code
    </th>
  </xsl:template>
  <xsl:template name="th_ItemNumber">
    <th align="right" title="Item or material codenumber">ItemNo</th>
  </xsl:template>
  <xsl:template name="th_ItemDescription">
    <th align="left">ItemDescription</th>
  </xsl:template>
  <xsl:template name="th_ItemCategory">
    <th align="center" title="Item/material Category" >
      Item
      <br />
      Cat
    </th>
  </xsl:template>
  <xsl:template name="th_Quantity">
    <th align="right" title="planned quantity of the order">Quantity</th>
  </xsl:template>
  <xsl:template name="th_UoM">
    <th align="center" title="unit of measurement">UoM</th>
  </xsl:template>
  <xsl:template name="th_PONumber" >
    <th title="purchase order number and -pos">PO Number</th>
  </xsl:template>
  <xsl:template name="th_MONo" >
    <th title="manufacturing order number (created by KAP)">MO Number</th>
  </xsl:template>
  <xsl:template name="th_DeliveryDate">
    <th title="Date of planned delivery">
      Delivery
      <br />
      Date
    </th>
  </xsl:template>
  <xsl:template name="th_OrderDate">
    <th title="Planned Date of Shipping Order">
      Order
      <br />
      Date
    </th>
  </xsl:template>
  <xsl:template name="th_Error">
    <th title="ErrorCode from ELOG">Error</th>
  </xsl:template>
  <xsl:template name="th_ConfirmationCode">
              <th title="Confirmation Code">
                Confirmation
                <br />
                Code
              </th>
  </xsl:template>
  <xsl:template name="th_ShippingOrderNo">
    <th title="PEX shipping order number (VA)">
      Shipping
      <br />
      OrderNo
    </th>
  </xsl:template>
  <xsl:template name="th_ShippingOrderSubNo">
    <th title="sequence number for partial delivery">
      Sh-Sub
      <br />
      OrderNo
    </th>
  </xsl:template>
  <xsl:template name="th_PEXOrderNo">
    <th title="PEX shipping order number (VA)">
      Shipping<br/>Order/SubOrderNo
    </th>
  </xsl:template>
  <xsl:template name="th_ColliNo">
    <th>ColliNo</th>
  </xsl:template>
  <xsl:template name="th_ColliNumber">
    <th align="center" title="Number of Collis">Colli<br/>Number</th>
  </xsl:template>
  <xsl:template name="th_ColliQty">
    <th align="right" title="Quantity per Colli">ColliQty</th>
  </xsl:template>
  <xsl:template name="th_BatchNo">
    <th title="pharmaceutical batch number">BatchNo</th>
  </xsl:template>
  <xsl:template name="th_PackBatchNo">
    <th title="packet batch number">PackBatchNo</th>
  </xsl:template>
  <xsl:template name="th_CountryNo">
    <th title="Country Number">CountryNo</th>
  </xsl:template>
  <xsl:template name="th_PallettNo">
    <th title="palett-reference number from ELOG">PallettNo</th>
  </xsl:template>
  <xsl:template name="th_PurchaseOrderNo">
    <th title="purchase-order and -pos number ">
      Purchase
      <br/>
      OrderNo
    </th>
  </xsl:template>
</xsl:stylesheet>










