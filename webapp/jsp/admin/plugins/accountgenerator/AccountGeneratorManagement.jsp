<jsp:useBean id="accountGeneratorManagement" scope="session" class="fr.paris.lutece.plugins.accountgenerator.web.AccountGeneratorJspBean" />
<% String strContent = accountGeneratorManagement.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
