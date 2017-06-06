// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

/**
 * Represents a provider for application resources. All resources returned
 * via this provider should be class file resources and will be loaded on-demand
 * or not loaded at all.
 *
 * NOTE: currently only resources representing Java class files can be loaded
 * with lazy resource providers.
 */
public interface ResourceProvider {
  /** Get the class resource associated with the descriptor, or null. */
  Resource getResource(String descriptor);
}
