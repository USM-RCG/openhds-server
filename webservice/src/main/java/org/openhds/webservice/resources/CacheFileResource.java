package org.openhds.webservice.resources;

import com.github.batkinson.jrsync.Metadata;

import org.openhds.task.service.AsyncTaskService;
import org.openhds.task.support.FileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import static org.openhds.task.service.AsyncTaskService.MOBILEDB_TASK_NAME;


/**
 * Shared controller for all pre-generated cache files for tablet
 * synchronization. It uses content negotiation and awareness of sync metadata
 * to help tablets optimize bandwidth consumption:
 *
 *   <li>skip syncing when its local content is identical</li>
 *   <li>use a zsync to efficiently synchronize with existing content</li>
 *   <li>perform a full-download is syncing is not possible</li>
 */
@Controller
public class CacheFileResource implements ServletContextAware {

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    private static Logger log = LoggerFactory.getLogger(CacheFileResource.class);

    private ServletContext ctx;

    @Autowired
    private FileResolver fileResolver;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Override
    public void setServletContext(ServletContext servletContext) {
        ctx = servletContext;
    }

    /**
     * Allows us to set the ETag and forward to another servlet without it
     * overwriting it.
     */
    private static class ETagIgnoringResponse extends HttpServletResponseWrapper {
        public ETagIgnoringResponse(HttpServletResponse response) {
            super(response);
        }

        public void setHeader(String name, String value) {
            if (!Headers.ETAG.equalsIgnoreCase(name)) {
                super.setHeader(name, value);
            }
        }
    }

    private String contextPath(File file) {
        return "/" + file.getAbsolutePath().replaceFirst(ctx.getRealPath("/"), "");
    }

    private void serviceTask(String taskName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String contentHash = asyncTaskService.getContentHash(taskName);
            String eTag = request.getHeader(Headers.IF_NONE_MATCH);

            if (eTag != null && eTag.equals(contentHash)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            if (contentHash != null) {
                response.setHeader(Headers.ETAG, contentHash);
                response = new ETagIgnoringResponse(response);
            }

            File xmlFile = fileResolver.getFileForTask(taskName);
            File metaFile = new File(xmlFile.getParentFile(), xmlFile.getName() + "." + Metadata.FILE_EXT);

            String accept = request.getHeader(Headers.ACCEPT);
            if (accept != null && accept.contains(Metadata.MIME_TYPE) && metaFile.exists()) {
                response.setContentType(Metadata.MIME_TYPE);
                request.getRequestDispatcher(contextPath(metaFile)).forward(request, response);
                return;
            }

            request.getRequestDispatcher(contextPath(xmlFile)).forward(request, response);

        } catch (Exception e) {
            log.error("problem servicing request" + e.getMessage());
        }
    }

    @RequestMapping(value = "/mobiledb/cached", method = RequestMethod.GET, produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    public void mobileDB(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        serviceTask(MOBILEDB_TASK_NAME, request, response);
    }
}
