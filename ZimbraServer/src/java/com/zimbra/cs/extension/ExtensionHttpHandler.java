/*
 * 
 */

/*
 * Created on Oct 26, 2005
 *
 */
package com.zimbra.cs.extension;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.service.ServiceException;

/**
 * HTTP handler for an extension. HTTP GET and POST requests are dispatched to the handler
 * for processing. Each extension can define multiple handlers and register them under the
 * extension with different paths.
 * 
 * @author kchen
 *
 */
public abstract class ExtensionHttpHandler {

    protected ZimbraExtension mExtension;
    
    /**
     * The path under which the handler is registered for an extension.
     * @return
     */
    public String getPath() {
        return "/" + mExtension.getName();
    }
    
    /**
     * Processes HTTP OPTIONS requests.
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        throw new ServletException("HTTP OPTIONS requests are not supported");
    }
    
    /**
     * Processes HTTP GET requests.
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        throw new ServletException("HTTP GET requests are not supported");
    }

    /**
     * Processes HTTP POST requests.
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        throw new ServletException("HTTP POST requests are not supported");
    }

    /**
     * Called to initialize the handler. If initialization fails, the handler is not registered.
     * @param ext the extension to which this handler belongs
     * @throws ServiceException
     */
    public void init(ZimbraExtension ext) throws ServiceException {
        mExtension = ext;
    }
    
    /**
     * Called to terminate the handler.
     */
    public void destroy() {
        
    }

    /**
     * Hides the extension for requests sent to the default mail port and
     * mail SSL port.
     */
    public boolean hideFromDefaultPorts() {
    	return false;
    }
}
