// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

/**
 * Represents a provider for application resources. All resources returned
 * via this provider should be class file resources, other resource kinds
 * are not yet supported.
 *
 * Note that the classes will only be created for resources provided by
 * resource providers on-demand when they are needed by the tool. If
 * never needed, the resource will never be loaded.
 */
public interface ResourceProvider {
  // TODO: Consider adding support for DEX resources.

  /**
   * Get the class resource associated with the descriptor, or null if
   * this provider does not have one.
   *
   * Method may be called several times for the same resource, and should
   * support concurrent calls from different threads.
   */
  Resource getResource(String descriptor);
}
