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

import org.gradle.api.Plugin
import org.gradle.api.Project

class ThriftPlugin implements Plugin<Project> {
	void apply(Project project) {
		def thriftExtension = project.extensions.create("thrift", ThriftExtension)

        project.tasks.register("compileThrift", ThriftCompileTask) {
            dependsOn("cleanThrift")  // Ensure clean task is executed before compilation
        }

        project.tasks.register("cleanThrift", Delete) {
            delete(thriftExtension.outputDir)
        }
    }
}
