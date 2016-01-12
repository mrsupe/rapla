/**
 * 
 */
package org.rapla.server.servletpages;

import org.rapla.RaplaResources;
import org.rapla.components.util.DateTools;
import org.rapla.components.util.IOUtil;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.framework.internal.ContainerImpl;
import org.rapla.inject.Extension;
import org.rapla.inject.dagger.DaggerReflectionStarter;
import org.rapla.server.ServerServiceContainer;
import org.rapla.server.extensionpoints.RaplaPageExtension;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Extension(provides = RaplaPageExtension.class,id="raplaclient.jnlp")
@Singleton
public class RaplaJNLPPageGenerator  implements RaplaPageExtension{

    private final ClientFacade facade;
    private final RaplaResources i18n;
    private final String moduleId;

    @Inject
    public RaplaJNLPPageGenerator( ClientFacade facade,RaplaResources i18n )
    {
        this.facade = facade;
        this.i18n = i18n;
        moduleId = DaggerReflectionStarter.loadModuleId(ServerServiceContainer.class.getClassLoader());
    }

    private String getCodebase( HttpServletRequest request)  {
        StringBuffer codebaseBuffer = new StringBuffer();
        String forwardProto = request.getHeader("X-Forwarded-Proto");
        boolean secure = (forwardProto != null && forwardProto.toLowerCase().equals("https"))  || request.isSecure();
        codebaseBuffer.append(secure ? "https://" : "http://");
        codebaseBuffer.append(request.getServerName());
        if (request.getServerPort() != (!secure ? 80 : 443))
        {
           codebaseBuffer.append(':');
           codebaseBuffer.append(request.getServerPort());
        }
        codebaseBuffer.append(request.getContextPath());
        codebaseBuffer.append('/');
        return codebaseBuffer.toString();
    }

    private String getLibsJNLP(ServletContext context, String webstartRoot) throws java.io.IOException {
        List<String> list = getClientLibs(context);
        
        StringBuffer buf = new StringBuffer();
        for (String file:list) {
              buf.append("\n<jar href=\""+webstartRoot + "/");
              buf.append(file);
              buf.append("\"");
              if (file.indexOf("raplaclient.jar")>=0) {
                 buf.append(" main=\"true\"");
              }
              buf.append("/>");
           }

        return buf.toString();
    }

    public static List<String> getClientLibs(ServletContext context)
            throws IOException {
        List<String> list = new ArrayList<String>();
        URL resource = RaplaJNLPPageGenerator.class.getResource("/clientlibs.properties");
        if (resource != null)
        {
            byte[] bytes = IOUtil.readBytes( resource);
            String string = new String( bytes);
            
            String[] split = string.split(";");
            for ( String file:split)
            {
                list.add( "webclient/" + file);
            }
        }
        else
        {
            String base = context.getRealPath(".");
            if ( base != null)
            {
                java.io.File baseFile = new java.io.File(base);
                java.io.File[] files = IOUtil.getJarFiles(base,"webclient");
                for (File file:files) {
                  String relativeURL = IOUtil.getRelativeURL(baseFile,file);
                  list.add( relativeURL);
               }
            }
        }
        int size = list.size();
        for (int i=0;i< size;i++)
        {
            String entry = list.get(i);
            if ( entry.indexOf("raplaclient")>=0)
            {
                list.remove(i);
                list.add(0, entry);
            }
        }
        return list;
    }
    
    protected List<String> getProgramArguments() {
        List<String> list = new ArrayList<String>();
        return list;
    }
    
    public void generatePage( ServletContext context, HttpServletRequest request, HttpServletResponse response ) throws IOException {
        java.io.PrintWriter out = response.getWriter();
        String webstartRoot = ".";
        long currentTimeMillis = System.currentTimeMillis();
        response.setDateHeader("Last-Modified",currentTimeMillis);
        response.addDateHeader("Expires", currentTimeMillis + DateTools.MILLISECONDS_PER_MINUTE);
        response.addDateHeader("Date", currentTimeMillis);
        response.setHeader("Cache-Control", "no-cache");
        final String defaultTitle = i18n.getString("rapla.title");
        String menuName;
        try
        {
            menuName= facade.getSystemPreferences().getEntryAsString(ContainerImpl.TITLE, defaultTitle);
        }
        catch (RaplaException e) {
            menuName = defaultTitle;
        }
        response.setContentType("application/x-java-jnlp-file;charset=utf-8");
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<jnlp spec=\"1.0+\" codebase=\"" + getCodebase(request) + "\" href=\"" + getCodebase(request) + "rapla/raplaclient.jnlp\" >");
        out.println("<information>");
        out.println(" <title>"+menuName+"</title>");
        out.println(" <vendor>Rapla team</vendor>");
        out.println(" <homepage href=\"http://code.google.com/p/rapla/\"/>");
        out.println(" <description>Resource Scheduling Application</description>");
        // we changed the logo from .gif to .png to make it more sexy
        //differentiate between icon and splash because of different sizes!
        out.println(" <icon kind=\"default\" href=\""+webstartRoot+"/webclient/rapla_64x64.png\" width=\"64\" height=\"64\"/> ");
        out.println(" <icon kind=\"desktop\" href=\""+webstartRoot+"/webclient/rapla_128x128.png\" width=\"128\" height=\"128\"/> ");
        out.println(" <icon kind=\"shortcut\" href=\""+webstartRoot+"/webclient/rapla_64x64.png\" width=\"64\" height=\"64\"/> ");
        // and here aswell


        out.println(" <icon kind=\"splash\" href=\""+webstartRoot+ "/webclient/logo.png\"/> ");
        out.println(" <update check=\"always\" policy=\"always\"/>");
        out.println(" <shortcut online=\"true\">");
        out.println("       <desktop/>");
        out.println("       <menu submenu=\"" + menuName +  "\"/>");
        out.println(" </shortcut>");
        out.println("</information>");
        out.println("<security>");
        out.println("  <all-permissions/>");
        out.println("</security>");
        out.println("<resources>");
        out.println("  <j2se version=\"1.4+\"/>");
        
        String passedUsername = request.getParameter("username");
        if ( passedUsername != null)
        {
            String usernameProperty = "jnlp.org.rapla.startupUser";
            String safeUsername = URLEncoder.encode(passedUsername, "UTF-8");
            out.println("  <property name=\"" +usernameProperty +"\" value=\"" + safeUsername + "\"/>");
        }
        {
            String moduleIdProperty = "jnlp.org.rapla.moduleId";
            out.println("  <property name=\"" +moduleIdProperty +"\" value=\"" + moduleId + "\"/>");
        }
        out.println(getLibsJNLP(context, webstartRoot));
        out.println("</resources>");
        out.println("<application-desc main-class=\"org.rapla.client.MainWebstart\">");
        for (Iterator<String> it = getProgramArguments().iterator(); it.hasNext();)
        {
            out.println("  <argument>" + it.next() + "</argument> ");
        }
        out.println("</application-desc>");
        

        out.println("</jnlp>");
        out.close();
     }
    
    
}