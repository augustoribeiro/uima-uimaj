/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.typesystem;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

import org.apache.uima.taeconfigurator.TAEConfiguratorPlugin;

import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class TypeSystemSelectionPlugin extends AbstractUIPlugin {
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public TypeSystemSelectionPlugin() {
    super();
    try {
      resourceBundle = ResourceBundle
                      .getBundle("org.apache.uima.typesystem.TypeSystemSelectionPluginResources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static TAEConfiguratorPlugin getDefault() {
    return TAEConfiguratorPlugin.getDefault();
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = TypeSystemSelectionPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }
}
