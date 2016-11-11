/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2016 SIOS Technology Corp.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sios.stc.coseng.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.sios.stc.coseng.run.CosengException;

/**
 * The Class Resource which will attempt to find a requested resource on the
 * file system or within the classpath.
 *
 * @since 2.0
 * @version.coseng
 */
public class Resource {

    /**
     * Gets the name of the resource.
     *
     * @param resource
     *            the resource
     * @return the name
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public static String getName(String resource) throws CosengException {
        if (resource != null && !resource.isEmpty()) {
            String name = FilenameUtils.getName(resource);
            if (name != null && !name.isEmpty()) {
                return name;
            }
            throw new CosengException("Can't get name for the resource [" + resource + "]");
        }
        throw new CosengException("Can't get name for null or empty resource");
    }

    /**
     * Gets the relative path.
     *
     * @param resource
     *            the resource
     * @return the relative path
     * @throws CosengException
     *             the coseng exception
     * @since 2.1
     * @version.coseng
     */
    public static String getRelativePath(String resource) throws CosengException {
        if (resource != null && !resource.isEmpty()) {
            /* Removes leading file separator */
            String path = FilenameUtils.getPath(resource);
            if (path != null && !path.isEmpty()) {
                return path;
            }
            throw new CosengException(
                    "Can't get relative path for the resource [" + resource + "]");
        }
        throw new CosengException("Can't get relative path for null or empty resource");
    }

    /**
     * Creates the resource.
     *
     * @param input
     *            the input
     * @param resource
     *            the resource
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public static void create(InputStream input, File resource) throws CosengException {
        String message = "Unable to create resource ["
                + (resource == null ? null : resource.getPath()) + "]";
        if (resource != null && !resource.isDirectory() && input != null) {
            try {
                FileUtils.copyInputStreamToFile(input, resource);
                return;
            } catch (IOException e) {
                throw new CosengException(message, e);
            }
        }
        throw new CosengException(message);
    }

    /**
     * Gets the resource from filesystem or the class loader.
     *
     * @param resource
     *            the resource
     * @return the input stream
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.util.Resource#getResource(String)
     * @since 2.0
     * @version.coseng
     */
    public static InputStream get(String resource) throws CosengException {
        if (resource != null && !resource.isEmpty()) {
            // check if URL
            try {
                URL url = new URL(resource);
                return url.openStream();
            } catch (IOException e) {
                // wasn't a url; keep processing
            }
            InputStream input = null;
            // check filesystem
            File file = new File(resource);
            if (file != null && file.exists() && file.canRead()) {
                try {
                    input = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    // OK; skip if not found and try class
                }
            } else {
                input = getResource(resource);
            }
            if (input != null) {
                return input;
            }
        }
        throw new CosengException(
                "Resource [" + (resource == null ? null : resource) + "] absent or unreadable");
    }

    /**
     * Gets the resource from the class loader.
     *
     * @param resource
     *            the resource
     * @return the resource
     * @see com.sios.stc.coseng.util.Resource#get(String)
     * @since 2.0
     * @version.coseng
     */
    private static InputStream getResource(String resource) {
        InputStream input = Resource.class.getClassLoader().getResourceAsStream(resource);
        if (input != null) {
            return input;
        }
        return null;
    }

}
