package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Abstraction for generating templated page content
 *
 * @author brad
 */
public interface Templater {
    void writePage(String template, CollectionResource aThis, Map<String, String> params, OutputStream out) throws IOException;
}
