package org.spliffy.server.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.User;
import org.spliffy.server.db.UserDao;
import org.spliffy.server.db.VersionNumberGenerator;

/**
 *
 * @author brad
 */
public class SpliffyResourceFactory implements ResourceFactory {

    private final UserDao userDao;    
    private final HashStore hashStore;
    private final BlobStore blobStore;
    private final VersionNumberGenerator versionNumberGenerator;
    private final SpliffySecurityManager securityManager;

    public SpliffyResourceFactory(UserDao userDao, HashStore hashStore, BlobStore blobStore, VersionNumberGenerator versionNumberGenerator, SpliffySecurityManager securityManager) {
        this.userDao = userDao;
        this.hashStore = hashStore;
        this.blobStore = blobStore;
        this.versionNumberGenerator = versionNumberGenerator;
        this.securityManager = securityManager;
    }

    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(sPath);
        Resource r = find(host, path);
        return r;
    }

    private Resource find(String host, Path p) throws NotAuthorizedException, BadRequestException {        
        if (p.isRoot()) {
            RootFolder rootFolder = (RootFolder) HttpManager.request().getAttributes().get("_spliffy_root_folder");
            if( rootFolder == null ) {
                rootFolder = new RootFolder();
                HttpManager.request().getAttributes().put("_spliffy_root_folder", rootFolder);
            }
            return rootFolder;
        } else {
            Resource rParent = find(host, p.getParent());
            if (rParent == null) {
                return null;
            } else {
                if (rParent instanceof CollectionResource) {
                    CollectionResource parent = (CollectionResource) rParent;
                    return parent.child(p.getName());
                } else {
                    return null;
                }
            }
        }
    }

    public class RootFolder implements CollectionResource, GetableResource {

        private Map<String,Resource> children = new HashMap<>();
        
        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Object authenticate(String user, String password) {
            return securityManager.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Request.Method method, Auth auth) {
            return securityManager.authorise(request, method, auth, null);
        }

        @Override
        public String getRealm() {
            return securityManager.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        @Override
        public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
            Resource r = children.get(childName);
            if( r != null ) {
                return r;
            }
            User u = userDao.getUser(childName);
            if (u == null) {
                return null;
            } else {
                UserResource ur = new UserResource(u, hashStore, blobStore, versionNumberGenerator);
                children.put(childName, ur);
                return ur;
            }
        }

        @Override
        public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
            return Collections.EMPTY_LIST; // browsing not supported
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
            System.out.println("sendContent");
            HttpServletRequest req = ServletRequest.getRequest();
            //HttpServletResponse resp = ServletResponse.getResponse();
            RequestDispatcher d = req.getRequestDispatcher("/jsps/home.jsp");
            JspResponse jspResponse = new JspResponse(out);
//            jspResponse.getOutputStream().write("hello word".getBytes());
            try {
                d.include(req, jspResponse);

            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("done");
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public String getContentType(String accepts) {
            return "text/html";
        }

        @Override
        public Long getContentLength() {
            return null;
        }
    }

    public class JspResponse implements HttpServletResponse {

        private final ServletOutputStream out;
        private final PrintWriter pw;

        public JspResponse(final OutputStream o) {
            this.out = new ServletOutputStream() {

                @Override
                public void write(int b) throws IOException {
                    System.out.println("write byet");
                    o.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    System.out.println("write2");
                    o.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    System.out.println("write3");
                    o.write(b, off, len);
                }
            };
            pw = new PrintWriter(o);
        }

        @Override
        public void addCookie(javax.servlet.http.Cookie cookie) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsHeader(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeURL(String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeRedirectURL(String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeUrl(String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeRedirectUrl(String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendError(int sc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDateHeader(String name, long date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addDateHeader(String name, long date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setHeader(String name, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addHeader(String name, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setIntHeader(String name, int value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addIntHeader(String name, int value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setStatus(int sc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setStatus(int sc, String sm) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return out;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return pw;
        }

        @Override
        public void setContentLength(int len) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContentType(String type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBufferSize(int size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void flushBuffer() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocale(Locale loc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
