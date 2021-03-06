<%--
 * 
--%>
<%@ tag body-content="empty" dynamic-attributes="dynattrs" %>
<%@ attribute name="urlTarget" rtexprvalue="true" required="true" %>
<%@ attribute name="context" rtexprvalue="true" required="true" type="com.zimbra.cs.taglib.tag.SearchContext"%>
<%@ attribute name="keys" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlclient" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>

<c:if test="${context.searchResult.hasPrevPage}">
    <zm:prevResultUrl var="url" value="${urlTarget}" index="0" context="${context}"/>
    <a <c:if test="${keys}">id="PREV_PAGE"</c:if> href="${fn:escapeXml(url)}"><app:img altkey="ALT_PAGE_PREVIOUS" src="startup/ImgLeftArrow.png" border="0"/></a>
</c:if>
<c:if test="${!context.searchResult.hasPrevPage}">
  <app:img altkey='ALT_PAGE_NO_PREVIOUS' disabled='true' src="startup/ImgLeftArrow.png" border="0"/>
</c:if>
