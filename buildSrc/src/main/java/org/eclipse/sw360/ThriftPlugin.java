/*
 * Copyright Helio Chissini de Castro 2023. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;

public class ThriftPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ThriftPluginExtension extension = project.getExtensions().create("thrift", ThriftPluginExtension.class);

        project.getTasks().register("compileThrift", ThriftCompileTask.class, task -> {
            task.dependsOn("cleanThrift");  // Ensure clean task is executed before compilation
        });

        project.getTasks().register("cleanThrift", Delete.class, task -> {
            task.delete(extension.getOutputDir());
        });
    }

}
