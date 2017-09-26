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
package com.sios.stc.coseng.run;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.paulhammant.ngwebdriver.NgWebDriver;
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
    public static final String ATTR_INPUT_VALUE = "value";

    private static final String            TAG_NAME_INPUT    = "input";
    private static final String            TAG_NAME_TEXTAREA = "textarea";
    private static final String            BY_CSS_SELECTOR   = "By.cssSelector: ";
    private org.openqa.selenium.WebElement webElement;
    private By                             by;
    private WebDriver                      webDriver;
    private WebDriverWait                  webDriverWait;
    private NgWebDriver                    ngWebDriver;
    private Actions                        actions;
    private JavascriptExecutor             jsExecutor;
    private Test                           test;

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
        if (by != null) {
            setWebDrivers();
            this.by = by;
        }
    }

    /**
     * Instantiates a new web element.
     *
     * @param webElement
     *            the web element
     * @throws CosengException
     *             the coseng exception
     * @since 2.2
     * @version.coseng
     */
    public WebElement(org.openqa.selenium.WebElement webElement) throws CosengException {
        if (webElement != null) {
            setWebDrivers();
            this.webElement = webElement;
        }
    }

    /**
     * Sets the web drivers.
     *
     * @throws CosengException
     *             the coseng exception
     * @since 2.2
     * @version.coseng
     */
    private void setWebDrivers() throws CosengException {
        webDriver = CosengRunner.getWebDriver();
        webDriverWait = CosengRunner.getWebDriverWait();
        ngWebDriver = CosengRunner.getNgWebDriver();
        actions = CosengRunner.getActions();
        jsExecutor = CosengRunner.getJavascriptExecutor();
        test = CosengRunner.getTest();
        if (webDriver == null || webDriverWait == null || ngWebDriver == null || actions == null
                || jsExecutor == null || test == null) {
            throw new CosengException("Selenium tools corrupt; nothing to do");
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
        if (by != null) {
            this.by = by;
        }
    }

    /**
     * Find web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public boolean find() {
        if (webDriver != null && by != null) {
            try {
                if (test.isAngular2App()) {
                    ngWebDriver.waitForAngular2RequestsToFinish();
                }
                webElement = webDriver.findElement(by);
                return true;
            } catch (NoSuchElementException e) {
                // do nothing
            }
        }
        return false;
    }

    /**
     * Click web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public void click() {
        if (webElement != null) {
            if (test.isAngular2App()) {
                ngWebDriver.waitForAngular2RequestsToFinish();
            }
            actions.moveToElement(webElement).click().build().perform();
        }
    }

    /**
     * Gets the css selector.
     *
     * @return the css selector
     * @since 2.0
     * @version.coseng
     */
    public String getCssSelector() {
        String cssSelector = null;
        if (by != null) {
            if (by instanceof By.ByCssSelector) {
                cssSelector = StringUtils.substringAfter(by.toString(), BY_CSS_SELECTOR);
            }
        }
        return cssSelector;
    }

    /**
     * Clear web element.
     *
     * @since 2.0
     * @version.coseng
     */
    public void clear() {
        if (isInput()) {
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
            try {
                return webElement.isDisplayed();
            } catch (StaleElementReferenceException e1) {
                /* Web element removed from DOM */
            }
        }
        return false;
    }

    /**
     * Checks if is input.
     *
     * @return true, if is input
     * @since 2.1
     * @version.coseng
     */
    public boolean isInput() {
        if (webElement != null && webElement.getTagName() != null
                && (webElement.getTagName().equals(TAG_NAME_INPUT)
                        || webElement.getTagName().equals(TAG_NAME_TEXTAREA))) {
            return true;
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
            if (test.isAngular2App()) {
                ngWebDriver.waitForAngularRequestsToFinish();
            }
            /* Courtesy wait until text present; if timeout will be false */
            if (!isInput() && wait && text != null) {
                try {
                    webDriverWait.until((Function<? super WebDriver, Boolean>) ExpectedConditions
                            .textToBePresentInElement(webElement, text));
                } catch (TimeoutException e) {
                    // do nothing; will be false
                }
            }
            String elementText = getText();
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
            webDriverWait.until(
                    (Function<? super WebDriver, org.openqa.selenium.WebElement>) ExpectedConditions
                            .visibilityOf(webElement));
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
            webDriverWait.until((Function<? super WebDriver, Boolean>) ExpectedConditions
                    .invisibilityOfElementLocated(this.getBy()));
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
        sendKeys(string, null, -1, false);
    }

    /**
     * Send keys.
     *
     * @param string
     *            the string
     * @param pauseMs
     *            the pause ms
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(String string, long pauseMs) {
        sendKeys(string, null, pauseMs, false);
    }

    /**
     * Send keys.
     *
     * @param string
     *            the string
     * @param click
     *            the click
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(String string, boolean click) {
        sendKeys(string, null, -1, click);
    }

    /**
     * Send keys.
     *
     * @param string
     *            the string
     * @param click
     *            the click
     * @param pauseMs
     *            the pause ms
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(String string, boolean click, long pauseMs) {
        sendKeys(string, null, pauseMs, click);
    }

    /**
     * Send keys.
     *
     * @param key
     *            the key
     * @since 2.1
     * @version.coseng
     */
    public void sendKeys(Keys key) {
        sendKeys(null, key, -1, false);
    }

    /**
     * Send keys.
     *
     * @param key
     *            the key
     * @param pauseMs
     *            the pause ms
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(Keys key, long pauseMs) {
        sendKeys(null, key, pauseMs, false);
    }

    /**
     * Send keys.
     *
     * @param key
     *            the key
     * @param click
     *            the click
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(Keys key, boolean click) {
        sendKeys(null, key, -1, click);
    }

    /**
     * Send keys.
     *
     * @param key
     *            the key
     * @param click
     *            the click
     * @param pauseMs
     *            the pause ms
     * @since 3.0
     * @version.coseng
     */
    public void sendKeys(Keys key, boolean click, long pauseMs) {
        sendKeys(null, key, pauseMs, click);
    }

    /**
     * Send keys.
     *
     * @param string
     *            the string
     * @param key
     *            the key
     * @since 3.0
     * @version.coseng
     */
    @SuppressWarnings("deprecation")
    private void sendKeys(String string, Keys key, long pauseMs, boolean click) {
        if (actions != null && webElement != null) {
            CharSequence sendKey = null;
            if (string != null) {
                sendKey = string;
            } else if (key != null) {
                sendKey = key;
            }
            if (sendKey != null) {
                if (test.isAngular2App()) {
                    ngWebDriver.waitForAngular2RequestsToFinish();
                }
                if (pauseMs > 0) {
                    /*
                     * As of 2016-12-21 The Microsoft Edge and IE web driver are
                     * spoty at best for reliable key entry into an input field.
                     * Sometimes the whole expected value is entered. Other
                     * times random partial elements of the value are entered.
                     * 
                     * Note! I'm purposely using the deprecated pause() method
                     * as it works well and avoids other kludgy timing efforts
                     * (that didn't perform 100%). Attempts to use Javascript or
                     * other means did not prove fruitful. Suggest 500l for Edge
                     * and 275l for IE.
                     */
                    if (click) {
                        actions.moveToElement(webElement).click(webElement).pause(pauseMs)
                                .sendKeys(webElement, sendKey).build().perform();
                    } else {
                        actions.moveToElement(webElement).pause(pauseMs)
                                .sendKeys(webElement, sendKey).build().perform();
                    }
                } else {
                    if (click) {
                        actions.moveToElement(webElement).click(webElement)
                                .sendKeys(webElement, sendKey).build().perform();
                    } else {
                        actions.moveToElement(webElement).sendKeys(webElement, sendKey).build()
                                .perform();
                    }
                }
            }
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

    /**
     * Gets the text.
     *
     * @return the text
     * @since 3.0
     * @version.coseng
     */
    public String getText() {
        if (test.isAngular2App()) {
            ngWebDriver.waitForAngular2RequestsToFinish();
        }
        if (isInput()) {
            return webElement.getAttribute(ATTR_INPUT_VALUE);
        }
        return webElement.getText();
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     * @since 3.0
     * @version.coseng
     */
    public boolean isSelected() {
        return webElement.isSelected();
    }

}
