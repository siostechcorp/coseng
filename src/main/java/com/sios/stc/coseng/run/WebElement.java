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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.sios.stc.coseng.run.Matcher.MatchBy;

/**
 * The Class WebElement. Selenium's WebElement is not an object. This helper
 * class wraps the Selenium WebElement to offer some convenience methods to
 * create and manipulate the Selenium WebElement.
 *
 * @since 2.0
 * @version.coseng
 */
public class WebElement {

    // common DOM attributes and values
    public static final String ATTR_CLASS    = "class";
    public static final String ATTR_DISABLED = "disabled";
    public static final String ATTR_ENABLED  = "enabled";

    private static final String            WEB_ELEMENT_TAG_NAME_INPUT = "input";
    private org.openqa.selenium.WebElement webElement;
    private By                             by;
    private WebDriver                      webDriver;
    private WebDriverWait                  webDriverWait;
    private Actions                        actions;
    private JavascriptExecutor             jsExecutor;

    /**
     * Instantiates a new web element.
     *
     * @param webDriverToolbox
     *            the web driver toolbox
     * @param by
     *            the by
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public WebElement(final WebDriverToolbox webDriverToolbox, final By by) throws CosengException {
        if (webDriverToolbox != null) {
            webDriver = webDriverToolbox.getWebDriver();
            webDriverWait = webDriverToolbox.getWebDriverWait();
            actions = webDriverToolbox.getActions();
            jsExecutor = webDriverToolbox.getJavascriptExecutor();
            if (webDriver == null || webDriverWait == null || actions == null
                    || jsExecutor == null) {
                throw new CosengException("webDriverToolbox corrupt; nothing to do");
            }
            this.by = by;
        } else {
            throw new CosengException("webDriverToolbox is null; was web driver started?");
        }
    }

    /**
     * Gets the web element.
     *
     * @return the org.openqa.selenium. web element
     * @since 2.0
     * @version.coseng
     */
    public org.openqa.selenium.WebElement get() {
        return webElement;
    }

    /**
     * Gets the by.
     *
     * @return the by
     * @since 2.0
     * @version.coseng
     */
    public By getBy() {
        return by;
    }

    /**
     * Sets the by.
     *
     * @param by
     *            the new by
     * @since 2.0
     * @version.coseng
     */
    public void setBy(By by) {
        this.by = by;
    }

    /**
     * Find web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public void find() {
        if (by != null) {
            webElement = webDriver.findElement(by);
        }
    }

    /**
     * Click web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public void click() {
        if (webElement != null) {
            actions.moveToElement(webElement).click().build().perform();
        }
    }

    /**
     * Clear web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public void clear() {
        if (webElement != null && webElement.getTagName() != null
                && webElement.getTagName().equals(WEB_ELEMENT_TAG_NAME_INPUT)) {
            webElement.clear();
        }
    }

    /**
     * Make visible.
     *
     * @since 2.0
     * @version.coseng
     */
    public void makeVisible() {
        if (webElement != null) {
            jsExecutor.executeScript(
                    "arguments[0].style.left='auto';arguments[0].style.visibility='visible';",
                    webElement);
        }
    }

    /**
     * Checks if is displayed.
     *
     * @return true, if is displayed
     * @since 2.0
     * @version.coseng
     */
    public boolean isDisplayed() {
        if (webElement != null) {
            return webElement.isDisplayed();
        }
        return false;
    }

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     * @since 2.0
     * @version.coseng
     */
    public boolean isEnabled() {
        if (webElement != null) {
            return webElement.isEnabled();
        }
        return false;
    }

    /**
     * Text contains.
     *
     * @param text
     *            the text
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean textContains(String text) {
        return textMatchBy(text, MatchBy.CONTAIN);
    }

    /**
     * Text equals.
     *
     * @param text
     *            the text
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean textEquals(String text) {
        return textMatchBy(text, MatchBy.EQUAL);
    }

    /**
     * Text empty.
     *
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean textEmpty() {
        return textMatchBy(null, MatchBy.EMPTY);
    }

    /**
     * Text match by.
     *
     * @param text
     *            the text
     * @param matchBy
     *            the match by
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    private boolean textMatchBy(String text, MatchBy matchBy) {
        boolean matched = false;
        if (webElement != null) {
            String elementText = webElement.getText();
            if (elementText != null) {
                if (text == null && MatchBy.EMPTY.equals(matchBy)) {
                    if (elementText.isEmpty()) {
                        matched = true;
                    }
                } else if (MatchBy.CONTAIN.equals(matchBy)) {
                    if (elementText.contains(text)) {
                        matched = true;
                    }
                } else {
                    // default matcher
                    if (elementText.equals(text)) {
                        matched = true;
                    }
                }
            }
            if (!MatchBy.EMPTY.equals(matchBy)) {
                Assert.assertTrue(matched,
                        "Expected web element [" + webElement.getTagName() + "] text ["
                                + elementText + "] to " + matchBy.toString().toLowerCase() + " ["
                                + text + "]");
            } else {
                Assert.assertTrue(matched,
                        "Expected web element [" + webElement.getTagName() + "] text to be "
                                + matchBy.toString().toLowerCase() + "; got [" + elementText + "]");
            }
        }
        return matched;
    }

    /**
     * Wait until visible.
     *
     * @since 2.0
     * @version.coseng
     */
    public void waitUntilVisible() {
        webDriverWait.until(ExpectedConditions.visibilityOf(webElement));
    }

    /**
     * Attribute contains.
     *
     * @param attribute
     *            the attribute
     * @param value
     *            the value
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean attributeContains(String attribute, String value) {
        return attributeMatchBy(attribute, value, MatchBy.CONTAIN);
    }

    /**
     * Attribute equals.
     *
     * @param attribute
     *            the attribute
     * @param value
     *            the value
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean attributeEquals(String attribute, String value) {
        return attributeMatchBy(attribute, value, MatchBy.EQUAL);
    }

    /**
     * Attribute match by.
     *
     * @param attribute
     *            the attribute
     * @param value
     *            the value
     * @param matchBy
     *            the match by
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    private boolean attributeMatchBy(String attribute, String value, MatchBy matchBy) {
        boolean matched = false;
        if (attribute != null && value != null && webElement != null
                && webElement.getAttribute(attribute) != null) {
            String attributeValue = webElement.getAttribute(attribute);
            if (MatchBy.CONTAIN.equals(matchBy)) {
                if (attributeValue.contains(value)) {
                    matched = true;
                }
            } else {
                // default matcher
                if (attributeValue.equals(value)) {
                    matched = true;
                }
            }
            Assert.assertTrue(matched,
                    "Expected web element [" + webElement.getTagName() + "] attribute value ["
                            + attributeValue + "] to " + matchBy.toString().toLowerCase() + " ["
                            + value + "]");
        }
        return matched;
    }

}
