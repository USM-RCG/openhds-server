package org.openhds.controller.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.PrivilegeConstants;

public class CacheResponseWriter {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    public void writeResponse(File fileToWrite, HttpServletResponse response) throws IOException {
        if (!fileToWrite.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        response.setStatus(HttpServletResponse.SC_OK);

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileToWrite));
            IOUtils.copy(is, response.getOutputStream());
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }

}