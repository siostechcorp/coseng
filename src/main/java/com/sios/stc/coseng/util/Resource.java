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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import com.sios.stc.coseng.run.CosengException;

/**
 * The Class Resource which will attempt to find a requested resource on the
 * file system or within the classpath.
 *
 * @since 2.0
 * @version.coseng
 */
public class Resource {
    private static final String             PATH_SEPARATOR       = "/";
    private static final String             RESOURCE_FILTER_NAME = "com";
    private static final String             RESOURCES_ROOT       = getResource("");
    private static final Collection<String> resourceDirectories  = getResourceDirectories();

    /**
     * Gets the requested resource by name.
     *
     * @param resource
     *            the resource
     * @return the file
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public static File get(String resource) throws CosengException {
        return get(new File(resource));
    }

    /**
     * Gets the requested resource by file.
     *
     * @param resource
     *            the resource
     * @return the file
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public static File get(File resource) throws CosengException {
        String exceptionMessage = "Resource [" + (resource == null ? null : resource.getName())
                + "] absent or unreadable";
        try {
            // check filesystem
            if (resource.exists() && resource.canRead()) {
                return resource;
            }
            // failing find on filesystem; scan top down of class resources
            String resourceName = resource.getName();
            for (String resourceDirectory : resourceDirectories) {
                String foundResource =
                        getResource(resourceDirectory + PATH_SEPARATOR + resourceName);
                if (foundResource != null) {
                    return new File(foundResource);
                }
            }
            throw new CosengException(exceptionMessage);
        } catch (NullPointerException e) {
            throw new CosengException(exceptionMessage, e);
        }
    }

    /**
     * Gets the resource from the classpath.
     *
     * @param resource
     *            the resource
     * @return the resource
     * @since 2.0
     * @version.coseng
     */
    private static String getResource(String resource) {
        URL resourceUrl = Resource.class.getClassLoader().getResource(resource);
        if (resourceUrl != null) {
            return resourceUrl.getPath();
        }
        return null;
    }

    /**
     * Gets the resource directories within the classpath. Best effort. Paths
     * are relative to the classpath. Excludes {@value #RESOURCE_FILTER_NAME}
     * directory.
     *
     * @return the resource directories
     * @since 2.0
     * @version.coseng
     */
    private static Collection<String> getResourceDirectories() {
        Collection<String> relativeResourceDirectories = new ArrayList<String>();
        IOFileFilter filterDir = new NameFileFilter(RESOURCE_FILTER_NAME);
        Collection<File> resourceDirectories = FileUtils.listFilesAndDirs(new File(RESOURCES_ROOT),
                new NotFileFilter(TrueFileFilter.INSTANCE),
                new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(filterDir)));
        try {
            /* Remove canonical root; making relative to class loader */
            for (File dir : resourceDirectories) {
                String relativePath =
                        StringUtils.removeStart(dir.getCanonicalPath(), RESOURCES_ROOT);
                relativeResourceDirectories.add(relativePath);
            }
        } catch (IOException | NullPointerException e) {
            // OK; skip on exception
        }
        return relativeResourceDirectories;
    }
}
