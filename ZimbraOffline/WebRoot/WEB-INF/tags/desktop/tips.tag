<%--
 * 
--%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<%@ taglib prefix="zdf" uri="com.zimbra.cs.offline.jsp" %>
<%@ attribute name="userAgent" required="true" %>

<table class="ZPanelBottom" align="center" cellpadding="4" cellspacing="0">
	<tr>
		<td class="ZTip"><a href="http://www.zimbra.com/products/desktop2.html" target="_blank"><fmt:message key='TipsHome'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://www.zimbra.com/desktop7/help/en_US/Zimbra_Mail_Help.htm" target="_blank"><fmt:message key='TipsHelp'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://wiki.zimbra.com/index.php?title=Zimbra_Desktop_7" target="_blank"><fmt:message key='TipsNotes'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://wiki.zimbra.com/index.php?title=Zimbra_Desktop_7_FAQ" target="_blank"><fmt:message key='TipsFaq'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://www.zimbra.com/forums/zimbra-desktop/" target="_blank"><fmt:message key='TipsForums'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="${zdf:addAuthToken(zdf:getBaseUri(), pageContext.request)}" target="_blank" class="js-external-link"><fmt:message key='TipsOpenInBrowser'/></a></td>
	</tr>
</table>
