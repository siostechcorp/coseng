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
package com.sios.stc.coseng.run;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;

/**
 * The Class WebElements holds the collection of
 * com.sios.stc.coseng.run.WebElement.
 *
 * @since 2.0
 * @version.coseng
 */
public class WebElements {

    private List<WebElement> webElements = new ArrayList<WebElement>();

    /**
     * Instantiates a new web elements.
     *
     * @see com.sios.stc.coseng.run.WebElements#WebElements(List)
     * @since 2.0
     * @version.coseng
     */
    public WebElements() {
        this(null);
    }

    /**
     * Instantiates a new web elements.
     *
     * @param webElements
     *            the web elements; may not be null
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public WebElements(List<WebElement> webElements) {
        if (webElements != null) {
            this.webElements = webElements;
        }
    }

    /**
     * Adds to web elements. If web element is already contained in the
     * collection it will be removed and added.
     *
     * @param webElement
     *            the web element; may not be null
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public void add(WebElement webElement) {
        if (webElement != null) {
            if (webElements.contains(webElement)) {
                webElements.remove(webElement);
            } else {
                webElements.add(webElement);
            }
        }
    }

    /**
     * Adds all the web element to the collection. Only non-null web element
     * added.
     *
     * @param webElements
     *            the web elements; may not be null
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public void addAll(List<WebElement> webElements) {
        if (webElements != null) {
            for (WebElement webElement : webElements) {
                add(webElement);
            }
        }
    }

    /**
     * Gets the collection of web elements.
     *
     * @return the list
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public List<WebElement> get() {
        return webElements;
    }

    /**
     * Find all web element.
     *
     * @throws NoSuchElementException
     *             the no such element exception
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public void findAll() throws NoSuchElementException {
        if (webElements != null && !webElements.isEmpty()) {
            for (WebElement webElement : webElements) {
                webElement.find();
            }
        }
    }

    /**
     * Clear all the web element.
     *
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    public void clearAll() {
        if (webElements != null && !webElements.isEmpty()) {
            for (WebElement webElement : webElements) {
                if (webElement != null) {
                    webElement.clear();
                }
            }
        }
    }

    /**
     * Removes all web element from the collection.
     *
     * @since 2.0
     * @version.coseng
     */
    public void removeAll() {
        webElements = new ArrayList<WebElement>();
    }

}
