/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.common.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Operating system dependent utility methods.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public final class OsUtils {

    /**
     * Property that can be used to override the reported value from {@link #getCurrentUser()}. If not set then
     * &quot;user.name&quot; system property is used
     */
    public static final String CURRENT_USER_OVERRIDE_PROP = "org.apache.sshd.currentUser";

    /**
     * Property that can be used to override the reported value from {@link #getJavaVersion()}. If not set then
     * &quot;java.version&quot; system property is used
     */
    public static final String JAVA_VERSION_OVERRIDE_PROP = "org.apache.sshd.javaVersion";

    /**
     * Property that can be used to override the reported value from {@link #isWin32()}. If not set then
     * &quot;os.name&quot; system property is used
     */
    public static final String OS_TYPE_OVERRIDE_PROP = "org.apache.sshd.osType";

    /**
     * Property that can be used to override the reported value from {@link #isAndroid()}. If not set then
     * {@link #ANDROID_DETECTION_PROPERTIES} are used to determine its value. Otherwise, it must contain the string
     * &quot;android&quot; (case-insensitive)
     *
     * @see #ANDROID_PROPERTY_VALUE_MATCHER
     */
    public static final String ANDROID_MODE_OVERRIDE_PROP = "org.apache.sshd.androidMode";

    /**
     * Property that can be used to override the reported value from {@link #isDalvikMachine()}. If not set then
     * {@link #DALVIK_DETECTION_PROPERTIES} are used to determine its value. Otherwise, it must contain the string
     * &quot;dalvik&quot; (case-insensitive)
     */
    public static final String DALVIK_MACHINE_OVERRIDE_PROP = "org.apache.sshd.dalvikMachine";

    public static final String WINDOWS_SHELL_COMMAND_NAME = "cmd.exe";
    public static final String LINUX_SHELL_COMMAND_NAME = "/bin/sh";

    public static final String ROOT_USER = "root";

    public static final List<String> LINUX_COMMAND
            = Collections.unmodifiableList(Arrays.asList(LINUX_SHELL_COMMAND_NAME, "-i", "-l"));
    public static final List<String> WINDOWS_COMMAND
            = Collections.unmodifiableList(Collections.singletonList(WINDOWS_SHELL_COMMAND_NAME));

    /**
     * System properties consulted in order to detect {@link #isAndroid() Android O/S}.
     *
     * @see <A HREF="https://developer.android.com/reference/java/lang/System#getProperties()">Android Developer</A>
     */
    public static final List<String> ANDROID_DETECTION_PROPERTIES
            = Collections.unmodifiableList(
                    Arrays.asList(
                            "java.vendor",
                            "java.specification.vendor",
                            "java.vm.vendor",
                            "java.vm.specification.vendor"));

    public static final Predicate<String> ANDROID_PROPERTY_VALUE_MATCHER
            = v -> GenericUtils.trimToEmpty(v).toLowerCase().contains("android");

    /**
     * System properties consulted in order to detect {@link #isDalvikMachine() Dalvik machine}.
     *
     * @see <A HREF="https://developer.android.com/reference/java/lang/System#getProperties()">Android Developer</A>
     */
    public static final List<String> DALVIK_DETECTION_PROPERTIES
            = Collections.unmodifiableList(
                    Arrays.asList(
                            "java.specification.name",
                            "java.vm.name",
                            "java.vm.specification.name"));

    public static final Predicate<String> DALVIK_PROPERTY_VALUE_MATCHER
            = v -> GenericUtils.trimToEmpty(v).toLowerCase().contains("dalvik");

    private static final AtomicReference<String> CURRENT_USER_HOLDER = new AtomicReference<>(null);
    private static final AtomicReference<VersionInfo> JAVA_VERSION_HOLDER = new AtomicReference<>(null);
    private static final AtomicReference<String> OS_TYPE_HOLDER = new AtomicReference<>(null);

    private static final AtomicReference<Boolean> ANDROID_HOLDER = new AtomicReference<>(null);
    private static final AtomicReference<Boolean> DALVIK_HOLDER = new AtomicReference<>(null);

    private static final AtomicReference<Supplier<? extends Path>> CWD_PROVIDER_HOLDER = new AtomicReference<>();

    private OsUtils() {
        throw new UnsupportedOperationException("No instance allowed");
    }

    /**
     * @return {@code true} if currently running on Android. <U>Note:</U> {@link #isUNIX()} is also probably
     *         {@code true} as well, so special care must be taken in code that consults these values
     * @see    #ANDROID_DETECTION_PROPERTIES
     * @see    #ANDROID_MODE_OVERRIDE_PROP
     * @see    #ANDROID_PROPERTY_VALUE_MATCHER
     */
    public static boolean isAndroid() {
        return resolveAndroidSettingFlag(
                ANDROID_HOLDER, ANDROID_MODE_OVERRIDE_PROP, ANDROID_DETECTION_PROPERTIES, ANDROID_PROPERTY_VALUE_MATCHER);
    }

    /**
     * Override the value returned by {@link #isAndroid()} programmatically
     *
     * @param value Value to set if {@code null} then value is auto-detected
     */
    public static void setAndroid(Boolean value) {
        synchronized (ANDROID_HOLDER) {
            ANDROID_HOLDER.set(value);
        }
    }

    /**
     * @return {@code true} if currently running on a Dalvik machine. <U>Note:</U> {@link #isUNIX()} and/or
     *         {@link #isAndroid()} are also probably {@code true} as well, so special care must be taken in code that
     *         consults these values
     * @see    #DALVIK_DETECTION_PROPERTIES
     * @see    #DALVIK_MACHINE_OVERRIDE_PROP
     * @see    #DALVIK_PROPERTY_VALUE_MATCHER
     */
    public static boolean isDalvikMachine() {
        return resolveAndroidSettingFlag(
                DALVIK_HOLDER, DALVIK_MACHINE_OVERRIDE_PROP, DALVIK_DETECTION_PROPERTIES, DALVIK_PROPERTY_VALUE_MATCHER);
    }

    /**
     * Override the value returned by {@link #isDalvikMachine()} programmatically
     *
     * @param value Value to set if {@code null} then value is auto-detected
     */
    public static void setDalvikMachine(Boolean value) {
        synchronized (DALVIK_HOLDER) {
            DALVIK_HOLDER.set(value);
        }
    }

    /**
     * @return true if the host is a UNIX system (and not Windows). <U>Note:</U> this does <B>not</B> preclude
     *         {@link #isAndroid()} or {@link #isDalvikMachine()} from being {@code true} as well.
     */
    public static boolean isUNIX() {
        return !isWin32() && !isOSX();
    }

    /**
     * @return true if the host is a OSX (and not Windows or Unix).
     */
    public static boolean isOSX() {
        return getOS().contains("mac");
    }

    /**
     * @return true if the host is Windows (and not UNIX).
     * @see    #OS_TYPE_OVERRIDE_PROP
     * @see    #setOS(String)
     */
    public static boolean isWin32() {
        return getOS().contains("windows");
    }

    /**
     * Can be used to enforce Win32 or Linux report from {@link #isWin32()}, {@link #isOSX()} or {@link #isUNIX()}
     *
     * @param os The value to set - if {@code null} then O/S type is auto-detected
     * @see      #isWin32()
     * @see      #isOSX()
     * @see      #isUNIX()
     */
    public static void setOS(String os) {
        synchronized (OS_TYPE_HOLDER) {
            OS_TYPE_HOLDER.set(os);
        }
    }

    private static boolean resolveAndroidSettingFlag(
            AtomicReference<Boolean> flagHolder, String overrideProp,
            Collection<String> detectionProps, Predicate<? super String> detector) {
        synchronized (flagHolder) {
            Boolean value = flagHolder.get();
            if (value != null) {
                return value.booleanValue();
            }

            String propValue = System.getProperty(overrideProp);
            if (detector.test(propValue)) {
                flagHolder.set(Boolean.TRUE);
                return true;
            }

            for (String p : detectionProps) {
                String detectionPropValue = System.getProperty(p);
                if (detector.test(detectionPropValue)) {
                    flagHolder.set(Boolean.TRUE);
                    return true;
                }
            }

            flagHolder.set(Boolean.FALSE);
        }

        return false;
    }

    /**
     * @return The resolved O/S type string if not already set (lowercase)
     */
    private static String getOS() {
        String typeValue;
        synchronized (OS_TYPE_HOLDER) {
            typeValue = OS_TYPE_HOLDER.get();
            if (typeValue != null) { // is it the 1st time
                return typeValue;
            }

            String value = System.getProperty(OS_TYPE_OVERRIDE_PROP, System.getProperty("os.name"));
            typeValue = GenericUtils.trimToEmpty(value).toLowerCase();
            OS_TYPE_HOLDER.set(typeValue);
        }

        return typeValue;
    }

    public static String resolveDefaultInteractiveShellCommand() {
        return resolveDefaultInteractiveShellCommand(isWin32());
    }

    public static String resolveDefaultInteractiveShellCommand(boolean winOS) {
        return winOS ? WINDOWS_SHELL_COMMAND_NAME : LINUX_SHELL_COMMAND_NAME + " -i -l";
    }

    public static List<String> resolveDefaultInteractiveCommandElements() {
        return resolveDefaultInteractiveCommandElements(isWin32());
    }

    public static List<String> resolveDefaultInteractiveCommandElements(boolean winOS) {
        if (winOS) {
            return WINDOWS_COMMAND;
        } else {
            return LINUX_COMMAND;
        }
    }

    /**
     * @return The (C)urrent (W)orking (D)irectory {@link Path} - {@code null} if cannot resolve it. Resolution occurs
     *         as follows:
     *         <UL>
     *         <LI>Consult any currently registered {@link #setCurrentWorkingDirectoryResolver(Supplier) resolver}.</LI>
     *
     *         <LI>If no resolver registered, then &quot;user.dir&quot; system property is consulted.</LI>
     *         </UL>
     * @see    #setCurrentWorkingDirectoryResolver(Supplier)
     */
    public static Path getCurrentWorkingDirectory() {
        Supplier<? extends Path> cwdProvider;
        synchronized (CWD_PROVIDER_HOLDER) {
            cwdProvider = CWD_PROVIDER_HOLDER.get();
        }

        if (cwdProvider != null) {
            return cwdProvider.get();
        }

        String cwdLocal = System.getProperty("user.dir");
        return GenericUtils.isBlank(cwdLocal) ? null : Paths.get(cwdLocal);
    }

    /**
     * Allows the user to &quot;plug-in&quot; a resolver for the {@link #getCurrentWorkingDirectory()} method
     *
     * @param cwdProvider The {@link Supplier} of the (C)urrent (W)orking (D)irectory {@link Path} - if {@code null}
     *                    then &quot;user.dir&quot; system property is consulted
     */
    public static void setCurrentWorkingDirectoryResolver(Supplier<? extends Path> cwdProvider) {
        synchronized (CWD_PROVIDER_HOLDER) {
            CWD_PROVIDER_HOLDER.set(cwdProvider);
        }
    }

    /**
     * Get current user name
     *
     * @return Current user
     * @see    #CURRENT_USER_OVERRIDE_PROP
     */
    public static String getCurrentUser() {
        String username;
        synchronized (CURRENT_USER_HOLDER) {
            username = CURRENT_USER_HOLDER.get();
            if (username != null) { // have we already resolved it ?
                return username;
            }

            username = getCanonicalUser(System.getProperty(CURRENT_USER_OVERRIDE_PROP, System.getProperty("user.name")));
            ValidateUtils.hasContent(username, "No username available");
            CURRENT_USER_HOLDER.set(username);
        }

        return username;
    }

    /**
     * Remove {@code Windows} domain and/or group prefix as well as &quot;(User);&quot suffix
     *
     * @param  user The original username - ignored if {@code null}/empty
     * @return      The canonical user - unchanged if {@code Unix} O/S
     */
    public static String getCanonicalUser(String user) {
        if (GenericUtils.isEmpty(user)) {
            return user;
        }

        // Windows owner sometime has the domain and/or group prepended to it
        if (isWin32()) {
            int pos = user.lastIndexOf('\\');
            if (pos > 0) {
                user = user.substring(pos + 1);
            }

            pos = user.indexOf(' ');
            if (pos > 0) {
                user = user.substring(0, pos).trim();
            }
        }

        return user;
    }

    /**
     * Attempts to resolve canonical group name for {@code Windows}
     *
     * @param  group The original group name - used if not {@code null}/empty
     * @param  user  The owner name - sometimes it contains a group name
     * @return       The canonical group name
     */
    public static String resolveCanonicalGroup(String group, String user) {
        if (isUNIX()) {
            return group;
        }

        // we reach this code only for Windows
        if (GenericUtils.isEmpty(group)) {
            int pos = GenericUtils.isEmpty(user) ? -1 : user.lastIndexOf('\\');
            return (pos > 0) ? user.substring(0, pos) : group;
        }

        int pos = group.indexOf(' ');
        return (pos < 0) ? group : group.substring(0, pos).trim();
    }

    /**
     * Can be used to programmatically set the username reported by {@link #getCurrentUser()}
     *
     * @param username The username to set - if {@code null} then {@link #CURRENT_USER_OVERRIDE_PROP} will be consulted
     */
    public static void setCurrentUser(String username) {
        synchronized (CURRENT_USER_HOLDER) {
            CURRENT_USER_HOLDER.set(username);
        }
    }

    /**
     * Resolves the reported Java version by consulting {@link #JAVA_VERSION_OVERRIDE_PROP}. If not set, then
     * &quot;java.version&quot; property is used
     *
     * @return The resolved {@link VersionInfo} - never {@code null}
     * @see    #setJavaVersion(VersionInfo)
     */
    public static VersionInfo getJavaVersion() {
        VersionInfo version;
        synchronized (JAVA_VERSION_HOLDER) {
            version = JAVA_VERSION_HOLDER.get();
            if (version != null) { // first time ?
                return version;
            }

            String value = System.getProperty(JAVA_VERSION_OVERRIDE_PROP, System.getProperty("java.version"));
            // e.g.: 1.7.5_30
            value = ValidateUtils.checkNotNullAndNotEmpty(value, "No configured Java version value").replace('_', '.');
            // clean up any non-digits - in case something like 1.6.8_25-b323
            for (int index = 0; index < value.length(); index++) {
                char ch = value.charAt(index);
                if ((ch == '.') || ((ch >= '0') && (ch <= '9'))) {
                    continue;
                }

                value = value.substring(0, index);
                break;
            }

            version = ValidateUtils.checkNotNull(VersionInfo.parse(value), "No version parsed for %s", value);
            JAVA_VERSION_HOLDER.set(version);
        }

        return version;
    }

    /**
     * Set programmatically the reported Java version
     *
     * @param version The version - if {@code null} then it will be automatically resolved
     */
    public static void setJavaVersion(VersionInfo version) {
        synchronized (JAVA_VERSION_HOLDER) {
            JAVA_VERSION_HOLDER.set(version);
        }
    }

    /**
     * @param  path The original path
     * @return      A path that can be compared with another one where case sensitivity of the underlying O/S has been
     *              taken into account - never {@code null}
     */
    public static String getComparablePath(String path) {
        String p = (path == null) ? "" : path;
        return isWin32() ? p.toLowerCase() : p;
    }
}
