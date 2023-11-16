/*
 * Creator: Debanjan Chatterjee on 28/08/23, 3:54 pm Last modified: 28/08/23, 3:54 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.ruddermetricsreporterandroid.error;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

public interface ErrorClient {
    void notify(@NonNull Throwable exc);

    void notifyUnhandledException(@NonNull Throwable exc, Metadata metadata,
                                  @SeverityReason.SeverityReasonType String severityReason,
                                  @Nullable String attributeValue);

    @NonNull
    List<Breadcrumb> getBreadcrumbs();

    void addMetadata(@NonNull String section, @NonNull Map<String, ?> value);

    void addMetadata(@NonNull String section, @NonNull String key, @Nullable Object value);

    void clearMetadata(@NonNull String section);

    void clearMetadata(@NonNull String section, @NonNull String key);

    @Nullable
    Map<String, Object> getMetadata(@NonNull String section);

    @Nullable
    Object getMetadata(@NonNull String section, @NonNull String key);

    void leaveBreadcrumb(@NonNull String message);

    void leaveBreadcrumb(@NonNull String message,
                         @NonNull Map<String, Object> metadata,
                         @NonNull BreadcrumbType type);

    /**
     * Enables or disables recording of errors. However unless shut down, already recorded
     * events will reach server
     *
     * @param enable
     */
    void enable(boolean enable);


}
