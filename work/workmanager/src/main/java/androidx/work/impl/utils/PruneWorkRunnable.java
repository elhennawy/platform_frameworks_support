/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.work.impl.utils;

import android.support.annotation.RestrictTo;

import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.model.WorkSpecDao;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A Runnable that prunes work in the background.  Pruned work meets the following criteria:
 * - Is finished (succeeded, failed, or cancelled)
 * - Has zero unfinished dependents
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PruneWorkRunnable implements Runnable {

    private final WorkManagerImpl mWorkManagerImpl;
    private final SettableFuture<Void> mFuture;

    public PruneWorkRunnable(WorkManagerImpl workManagerImpl) {
        mWorkManagerImpl = workManagerImpl;
        mFuture = SettableFuture.create();
    }

    public ListenableFuture<Void> getFuture() {
        return mFuture;
    }

    @Override
    public void run() {
        try {
            WorkDatabase workDatabase = mWorkManagerImpl.getWorkDatabase();
            WorkSpecDao workSpecDao = workDatabase.workSpecDao();
            workSpecDao.pruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast();
            mFuture.set(null);
        } catch (Throwable exception) {
            mFuture.setException(exception);
        }
    }
}
