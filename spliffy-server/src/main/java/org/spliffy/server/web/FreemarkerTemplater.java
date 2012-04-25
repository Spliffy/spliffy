package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;
import com.bradmcevoy.http.SpringMiltonFilter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class FreemarkerTemplater implements Templater{
        
    private String basePath = "WEB-INF/templates";
        
       
    @Override
    public void writePage(String template, CollectionResource aThis, Map<String, String> params, OutputStream out) throws IOException {
        Map datamodel = new HashMap();
        datamodel.put("page", aThis);
        Configuration cfg = new Configuration();        
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

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    
}
