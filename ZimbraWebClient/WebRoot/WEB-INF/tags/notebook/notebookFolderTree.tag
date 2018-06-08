<%--
 * 
--%>
<%@ tag body-content="empty" %>
<%@ attribute name="editmode" rtexprvalue="true" required="false" %>
<%@ attribute name="keys" rtexprvalue="true" required="true" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlclient" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>

<zm:getMailbox var="mailbox"/>

<jsp:useBean id="expanded" scope="session" class="java.util.HashMap" />
<c:set var="expanded" value="${sessionScope.expanded.notebook ne 'collapse'}"/>

<div class="Tree">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <c:url var="toggleUrl" value="/h/search">
                <c:param name="st" value="wiki"/>
                 <c:param name="${expanded ? 'collapse' : 'expand'}" value="notebook"/>
             </c:url>
             <th style='width:20px'><a href="${fn:escapeXml(toggleUrl)}"><app:img altkey="${ expanded ? 'ALT_TREE_EXPANDED' : 'ALT_TREE_COLLAPSED'}" src="${ expanded ? 'startup/ImgNodeExpanded.png' : 'startup/ImgNodeCollapsed.png'}"/></a></th>
            <th class='Header'><fmt:message key="folders"/></th>
            <!--th width='1%' align='right' class='ZhTreeEdit'>
                <c:url value="/h/mnotebook" var="mabUrl">
                    <c:if test="${not empty param.sfi}">
                        <c:param name="sfi" value="${param.sfi}"/>
                    </c:if>
                </c:url>
                <a id="MNOTEBOOK" href="${mabUrl}" ><fmt:message key="TREE_EDIT"/></a>
            </th-->
        </tr>
        <c:if test="${expanded}">
            <zm:forEachFolder var="folder" skiproot="${false}" skipsystem="${false}" expanded="${sessionScope.expanded}" skiptrash="${true}">
                <c:if test="${folder.isWikiView}">
                    <app:notebookFolder folder="${folder}" keys="${keys}"/>
                </c:if>
            </zm:forEachFolder>

        </c:if>
    </table>
</div>