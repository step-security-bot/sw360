/*
 * Copyright Helio Chissini de Castro 2023. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360

import java.util.List

class ThriftExtension {
    List<String> sourceDirs = ["src/main/thrift"]
    String outputDir = "build/generated-sources/thrift"
    boolean skipIfExists = true
}
