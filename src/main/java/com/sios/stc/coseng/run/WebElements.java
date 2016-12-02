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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * The Class WebElements holds the collection of
 * com.sios.stc.coseng.run.WebElement.
 *
 * @since 2.0
 * @version.coseng
 */
public class WebElements {

    private List<WebElement> webElements = new ArrayList<WebElement>();
    // private org.openqa.selenium.WebElement webElement;
    private WebDriver webDriver;
    private By        by;

    /**
     * Instantiates a new web elements.
     * 
     * @see com.sios.stc.coseng.run.WebElements#WebElements(List)
     * @since 2.0
     * @version.coseng
     */
    public WebElements() {
        // nothing to do
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
     * Instantiates a new web elements. Creating with a By will fill with
     * findElements() when findAll() is called.
     *
     * @param by
     *            the by
     * @since 2.2
     * @version.coseng
     */
    public WebElements(By by) {
        this.by = by;
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
        if (by == null && webElement != null) {
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
     * Gets the by.
     *
     * @return the by
     * @since 2.2
     * @version.coseng
     */
    public By getBy() {
        return by;
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
        if (by == null) {
            if (webElements != null) {
                for (WebElement webElement : webElements) {
                    webElement.find();
                }
            }
        } else {
            webDriver = CosengRunner.getWebDriver();
            if (webDriver != null) {
                webElements = new ArrayList<WebElement>();
                List<org.openqa.selenium.WebElement> seleniumWebElements =
                        webDriver.findElements(by);
                if (seleniumWebElements != null) {
                    for (org.openqa.selenium.WebElement seleniumWebElement : seleniumWebElements) {
                        WebElement webElement;
                        try {
                            webElement = new WebElement(seleniumWebElement);
                            webElement.setBy(by);
                            webElements.add(webElement);
                        } catch (CosengException e) {
                            throw new NoSuchElementException(e.getMessage());
                        }
                    }
                }
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
