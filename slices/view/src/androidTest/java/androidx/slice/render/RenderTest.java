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

package androidx.slice.render;

import static android.os.Build.VERSION.SDK_INT;

import static androidx.slice.render.SliceRenderer.SCREENSHOT_DIR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@LargeTest
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 19)
public class RenderTest {

    private final Context mContext = InstrumentationRegistry.getContext();

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testRender() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                latch.countDown();
            }
        };
        mContext.registerReceiver(receiver,
                new IntentFilter(SliceRenderActivity.ACTION_RENDER_DONE));
        mContext.startActivity(new Intent(mContext, SliceRenderActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        latch.await(30000, TimeUnit.MILLISECONDS);
        assertEquals(0, latch.getCount());
        String path = new File(mContext.getFilesDir(), SCREENSHOT_DIR).getAbsolutePath();
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "mv " + path + " " + "/sdcard/");
        } else {
            deleteDir(new File("/sdcard/" + SCREENSHOT_DIR));
            copyDirectory(new File(path), new File("/sdcard/" + SCREENSHOT_DIR));
        }
    }


    public static void copyDirectory(File sourceLocation, File targetLocation) throws Exception {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                assertTrue(targetLocation.mkdirs());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(
                        targetLocation, children[i]));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyFile(File sourceLocation, File targetLocation) throws Exception {
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
