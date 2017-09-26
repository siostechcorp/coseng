/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2017 SIOS Technology Corp.  All rights reserved.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.sios.stc.coseng.run.CosengException;

import wiremock.com.google.common.io.CharStreams;

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
    public static InputStream getStream(String resource) throws CosengException {
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
     * Gets the string.
     *
     * @param inputStream
     *            the input stream
     * @return the string
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static String getString(InputStream inputStream) throws CosengException {
        try {
            Reader reader = new InputStreamReader(inputStream);
            return CharStreams.toString(reader);
        } catch (Exception e) {
            throw new CosengException("Unable to read input stream", e);
        }
    }

    /**
     * Gets the string.
     *
     * @param resource
     *            the resource
     * @return the string
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static String getString(String resource) throws CosengException {
        try {
            InputStream inputStream = getStream(resource);
            return getString(inputStream);
        } catch (Exception e) {
            throw new CosengException("Unable to read resource", e);
        }
    }

    /**
     * Gets the object from json.
     *
     * @param jsonFile
     *            the json file
     * @param desiredClass
     *            the desired class
     * @return the object from json
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static Object getObjectFromJson(String jsonFile, Class<?> desiredClass)
            throws CosengException {
        return getObjectFromJson(jsonFile, null, desiredClass);
    }

    /**
     * Gets the object from json.
     *
     * @param jsonFile
     *            the json file
     * @param typeAdapters
     *            the type adapters
     * @param desiredClass
     *            the desired class
     * @return the object from json
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static Object getObjectFromJson(String jsonFile,
            Map<Class<?>, JsonDeserializer<?>> typeAdapters, Class<?> desiredClass)
            throws CosengException {
        try {
            InputStream jsonInputStream = getStream(jsonFile);
            GsonBuilder gsonBuilder = new GsonBuilder();
            if (typeAdapters != null) {
                for (Class<?> typeClass : typeAdapters.keySet()) {
                    gsonBuilder.registerTypeAdapter(typeClass, typeAdapters.get(typeClass));
                }
            }
            Gson gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();
            Reader jsonReader = new InputStreamReader(jsonInputStream);
            return (Object) gson.fromJson(jsonReader, desiredClass);
        } catch (Exception e) {
            throw new CosengException("Exception reading JSON file [" + jsonFile + "]", e);
        }
    }

    /**
     * Gets the json from object.
     *
     * @param object
     *            the object
     * @return the json from object
     * @since 3.0
     * @version.coseng
     */
    public static String getJsonFromObject(Object object) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                .create();
        return gson.toJson(object);
    }

    /**
     * Zip folder.
     *
     * @param sourcePath
     *            the source path
     * @param zipPath
     *            the zip path
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static void zipFolder(Path sourcePath, Path zipPath) throws CosengException {
        if (sourcePath != null && zipPath != null) {
            try {
                FileOutputStream zipFile = new FileOutputStream(zipPath.toFile());
                ZipOutputStream zipOutput = new ZipOutputStream(zipFile);
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        String relativeFile = sourcePath.relativize(file).toString();
                        zipOutput.putNextEntry(new ZipEntry(relativeFile));
                        Files.copy(file, zipOutput);
                        zipOutput.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
                zipOutput.close();
                zipFile.close();
            } catch (IOException e) {
                throw new CosengException("Unable to create zip file [" + zipPath.toString()
                        + "] with [" + sourcePath.toString() + "] content", e);
            }
        }
    }

    /**
     * Gets the Log4J file.
     *
     * @return the log4j file
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public static File getLog4jFile() throws CosengException {
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            FileAppender logFile = (FileAppender) config.getAppender("File");
            return new File(logFile.getFileName());
        } catch (Exception e) {
            throw new CosengException(
                    "Unable to reference Log4J log file from log manager configuration", e);
        }
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
