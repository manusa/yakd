/*
 * Copyright 2020 Marc Nuri
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
 *
 */
package com.marcnuri.yakd.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * Page object that encapsulates the WebDriver plumbing shared by the Selenium integration tests:
 * an isolated browser tab per test, a tuned {@link FluentWait}, and the {@code data-testid} based
 * queries and actions for the shared resource list, detail and YAML-editor pages.
 *
 * <p>It holds no resource knowledge, so it is usable by any Selenium IT, not only those extending
 * {@link AbstractResourceIT}.
 */
public class ResourceUi {

  private static final By ROW = By.cssSelector("[data-testid='resource-list__row']");
  private static final By ROW_DELETE = By.cssSelector("[data-testid='resource-list__delete']");
  private static final By EDIT_SAVE = By.cssSelector("[data-testid='resource-edit__save']");

  private final WebDriver driver;
  private final URL url;
  private final Wait<WebDriver> wait;

  private ResourceUi(WebDriver driver, URL url) {
    this.driver = driver;
    this.url = url;
    this.wait = new FluentWait<>(driver)
      .withTimeout(Duration.ofSeconds(10))
      .pollingEvery(Duration.ofMillis(100))
      .ignoring(NoSuchElementException.class)
      .ignoring(StaleElementReferenceException.class);
  }

  /**
   * Opens a fresh, isolated browser tab (a clean Redux store and watch streams per test) and
   * returns a {@code ResourceUi} bound to it. Pair every call with {@link #closeTab()}.
   */
  public static ResourceUi openTab(WebDriver driver, URL url) {
    driver.switchTo().newWindow(WindowType.TAB);
    return new ResourceUi(driver, url);
  }

  /** Closes the current tab and switches back to the first remaining window. */
  public void closeTab() {
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  // --- navigation ---

  /** Navigates to an arbitrary application path (e.g. {@code ""} for the dashboard, {@code "search"}). */
  public void open(String path) {
    driver.navigate().to(url.toString() + path);
  }

  public void openList(String route) {
    open(route);
  }

  public void openDetail(String route, String uid) {
    open(route + "/" + uid);
  }

  public void openEditor(String route, String uid) {
    open(route + "/" + uid + "/edit");
  }

  // --- waiting ---

  /** Polls {@code condition} until it holds or the timeout elapses. */
  public void await(BooleanSupplier condition) {
    wait.until(d -> condition.getAsBoolean());
  }

  // --- list queries ---

  public boolean hasRow(String name) {
    return driver.findElements(ROW).stream().anyMatch(row -> row.getText().contains(name));
  }

  /** Whether the row matching {@code name} also shows {@code text} (e.g. an image or phase). */
  public boolean rowShows(String name, String text) {
    return driver.findElements(ROW).stream()
      .filter(row -> row.getText().contains(name))
      .anyMatch(row -> row.getText().contains(text));
  }

  // --- list actions ---

  public void deleteRow(String name) {
    driver.findElements(ROW).stream()
      .filter(row -> row.getText().contains(name))
      .findFirst().orElseThrow()
      .findElement(ROW_DELETE).click();
  }

  // --- detail ---

  public boolean pageContains(String text) {
    return driver.getPageSource().contains(text);
  }

  // --- YAML editor ---

  public boolean editorContains(String text) {
    return editorValue().contains(text);
  }

  public String editorValue() {
    final Object value = ((JavascriptExecutor) driver).executeScript(
      "const e = document.querySelector('.ace_editor');"
        + "return e && e.env && e.env.editor ? e.env.editor.getValue() : '';");
    return value == null ? "" : value.toString();
  }

  public void editorReplace(String from, String to) {
    ((JavascriptExecutor) driver).executeScript(
      "const ed = document.querySelector('.ace_editor').env.editor;"
        + "ed.setValue(ed.getValue().replace(arguments[0], arguments[1]), 1);", from, to);
  }

  public void save() {
    driver.findElement(EDIT_SAVE).click();
  }
}
