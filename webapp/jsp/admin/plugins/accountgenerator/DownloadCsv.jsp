<%@ page import="fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJob" %>
<%@ page import="fr.paris.lutece.plugins.accountgenerator.service.AccountGenerationJobService" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.nio.file.Path" %>
<%@ page import="java.util.Optional" %>
<%
    String reference = request.getParameter( "reference" );
    if ( reference != null )
    {
        Optional<AccountGenerationJob> optJob = AccountGenerationJobService.instance().findByReference( reference );
        if ( optJob.isPresent() )
        {
            Path csvPath = AccountGenerationJobService.instance().getCsvPath( optJob.get() );
            if ( csvPath != null )
            {
                response.setContentType( "text/csv; charset=UTF-8" );
                response.setHeader( "Content-Disposition", "attachment; filename=\"" + csvPath.getFileName() + "\"" );
                response.setHeader( "Content-Length", String.valueOf( Files.size( csvPath ) ) );
                Files.copy( csvPath, response.getOutputStream() );
                response.getOutputStream().flush();
                return;
            }
        }
    }
    response.sendError( 404, "File not found" );
%>
