/**
 * Copyright (C) 2013 Phoenix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.falcon.convention;

import com.google.common.base.Strings;
import io.falcon.SkyException;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午3:32
 * To change this template use File | Settings | File Templates.
 */
public class GroupConventionUtils {
    private GroupConventionUtils() {}

    public static File configFolder(GroupConvention groupConvention) {
        File file = groupConvention.group().configFolder();
        String projectId = groupConvention.currentProject().id();

        file = Strings.isNullOrEmpty(projectId)
                ? file
                : new File(file, projectId);

        return buildDirectory(file);
    }

    public static File logFolder(GroupConvention groupConvention) {
        File file = groupConvention.group().logFolder();
        String projectId = groupConvention.currentProject().id();

        file = Strings.isNullOrEmpty(projectId)
                ? file
                : new File(file, projectId);

        return buildDirectory(file);
    }

    private static File buildDirectory(File file) {
        if (file.isDirectory())
            return file;

        if (file.exists())
            throw SkyException.raise(String.format("File %s has exist, but not directory.", file));

        if (!file.mkdirs())
            throw SkyException.raise(String.format("Failed to getLogger directory %s.", file));

        return file;
    }
}
