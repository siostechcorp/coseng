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
import org.openqa.selenium.TimeoutException;
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

    private static final String            TAG_NAME_INPUT = "input";
    private org.openqa.selenium.WebElement webElement;
    private By                             by;
    private WebDriver                      webDriver;
    private WebDriverWait                  webDriverWait;
    private Actions                        actions;
    private JavascriptExecutor             jsExecutor;

    /**
     * Instantiates a new web element.
     *
     * @param by
     *            the by
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public WebElement(By by) throws CosengException {
        webDriver = CosengRunner.getWebDriver();
        webDriverWait = CosengRunner.getWebDriverWait();
        actions = CosengRunner.getActions();
        jsExecutor = CosengRunner.getJavascriptExecutor();
        if (webDriver == null || webDriverWait == null || actions == null || jsExecutor == null) {
            throw new CosengException("Selenium tools corrupt; nothing to do");
        }
        this.by = by;
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
                && webElement.getTagName().equals(TAG_NAME_INPUT)) {
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
                    "arguments[0].style.left='auto';arguments[0].style.visibility='visible';arguments[0].style.display='block'",
                    webElement);
        }
    }

    /**
     * Make invisible.
     *
     * @since 2.1
     * @version.coseng
     */
    public void makeInvisible() {
        if (webElement != null) {
            jsExecutor.executeScript(
                    "arguments[0].style.left='initial';arguments[0].style.visibility='hidden';arguments[0].style.visibility='none'",
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
     * Wait until text contains.
     *
     * @param text
     *            the text
     * @return true, if successful
     * @since 2.1
     * @version.coseng
     */
    public boolean waitUntilTextContains(String text) {
        return textMatchBy(text, MatchBy.CONTAIN, true);
    }

    /**
     * Wait until text equals.
     *
     * @param text
     *            the text
     * @return true, if successful
     * @since 2.1
     * @version.coseng
     */
    public boolean waitUntilTextEquals(String text) {
        return textMatchBy(text, MatchBy.EQUAL, true);
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
        return textMatchBy(text, MatchBy.CONTAIN, false);
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
        return textMatchBy(text, MatchBy.EQUAL, false);
    }

    /**
     * Text empty.
     *
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean textEmpty() {
        return textMatchBy(null, MatchBy.EMPTY, false);
    }

    /**
     * Text match by.
     *
     * @param text
     *            the text
     * @param matchBy
     *            the match by
     * @param wait
     *            the wait
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    private boolean textMatchBy(String text, MatchBy matchBy, boolean wait) {
        boolean matched = false;
        if (webElement != null) {
            /* Courtesy wait until text present; if timeout will be false */
            if (wait && text != null) {
                try {
                    webDriverWait
                            .until(ExpectedConditions.textToBePresentInElement(webElement, text));
                } catch (TimeoutException e) {
                    // do nothing; will be false
                }
            }
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
        if (webElement != null) {
            webDriverWait.until(ExpectedConditions.visibilityOf(webElement));
        }
    }

    /**
     * Wait until invisible.
     *
     * @since 2.1
     * @version.coseng
     */
    public void waitUntilInvisible() {
        if (this.getBy() != null) {
            webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(this.getBy()));
        }
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

    /**
     * Send keys.
     *
     * @param string
     *            the string
     * @since 2.1
     * @version.coseng
     */
    public void sendKeys(String string) {
        if (string != null && actions != null && webElement != null) {
            actions.moveToElement(webElement).sendKeys(webElement, string).build().perform();
        }
    }

    /**
     * Move to.
     *
     * @since 2.1
     * @version.coseng
     */
    public void moveTo() {
        if (actions != null && webElement != null) {
            actions.moveToElement(webElement).build().perform();
        }
    }

    /**
     * Gets the rgba background color.
     *
     * @return the rgba background color
     * @since 2.1
     * @version.coseng
     */
    public String getRgbaBackgroundColor() {
        return this.get().getCssValue("background-color");
    }

}
