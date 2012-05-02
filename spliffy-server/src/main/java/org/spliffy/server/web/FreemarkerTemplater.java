package org.spliffy.server.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ServletRequest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import org.spliffy.server.db.User;

/**
 *
 * @author brad
 */
public class FreemarkerTemplater implements Templater {

    private String basePath = "WEB-INF/templates";
    
    /**
     * Lazily loaded so there is a servlet context available when
     */
    private Configuration freemarkerConfig;

    @Override
    public void writePage(String template, Resource aThis, Map<String, String> params, OutputStream out, User user) throws IOException {
        Map datamodel = new HashMap();
        datamodel.put("page", aThis);
        if( user != null) {
            datamodel.put("user", user);
        }
        Configuration cfg = freemarkerConfig();
        Object servletContext = ServletRequest.getTLServletContext();
        cfg.setServletContextForTemplateLoading(servletContext, basePath);
        Template tpl = cfg.getTemplate(template);
        OutputStreamWriter output = new OutputStreamWriter(out);
        try {
            tpl.process(datamodel, output);
        } catch (TemplateException ex) {
            throw new IOException("Template: " + template, ex);
        }
    }

    /**
     * Just want to create this once so we have a cache of templates, but
     * it would be better to hook this into the servlet life cycle
     * 
     * Of course this means we now depend on the servlet API, but if you
     * want to run in a non-servlet container just use a different Templater
     * implementation
     * 
     * @return 
     */
    private synchronized Configuration freemarkerConfig() {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration();
            Object servletContext = ServletRequest.getTLServletContext();
            freemarkerConfig.setServletContextForTemplateLoading(servletContext, basePath);
        }
        return freemarkerConfig;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
