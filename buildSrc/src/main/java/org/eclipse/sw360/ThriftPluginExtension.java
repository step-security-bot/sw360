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

import java.util.Arrays;
import java.util.List;

public class ThriftPluginExtension {

    private List<String> sourceDirs = Arrays.asList("src/main/thrift");
    private String outputDir = "build/generated-sources/thrift";
    private boolean skipIfExists = true;

    public List<String> getSourceDirs() {
        return sourceDirs;
    }

    public void setSourceDirs(List<String> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public boolean getSkipIfExists() {
        return skipIfExists;
    }

    public void setSkipIfExists(boolean skipIfExists) {
        this.skipIfExists = skipIfExists;
    }

}
