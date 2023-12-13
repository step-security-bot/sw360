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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ThriftCompileTask extends DefaultTask {
    @TaskAction
    void compileThrift() {
        def thriftExtension = project.extensions.getByType(ThriftExtension)
        def sourceDirs = thriftExtension.sourceDirs.join(',')
        def outputDir = thriftExtension.outputDir

        if (thriftExtension.skipIfExists && new File(outputDir).exists()) {
            logger.info("Thrift files already generated. Skipping compilation.")
        } else {
            exec {
                commandLine 'thrift',
                        '-r',
                        '-out', outputDir,
                        '-I', sourceDirs
            }
        }
    }
}
