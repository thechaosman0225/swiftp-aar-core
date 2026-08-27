// SPDX-License-Identifier: GPL-3.0-or-later

package be.ppareit.swiftp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import be.ppareit.swiftp.R;

public class ChrootPicker {

    public ChrootPicker() {
    }

    public interface OnTextEventListener {
        void OnEvent(String s);
    }

    public OnTextEventListener onTextEventListener;

    public void setOnTextEventListener(OnTextEventListener onTextEventListener) {
        this.onTextEventListener = onTextEventListener;
    }

    private boolean isShowingFolderPicker = false;

    /**
     * Core is headless: the old raw-filesystem browser (FolderPickerDialogBuilder) was
     * a gui-layer dialog and isn't included here. Under SAF, listFiles() on an arbitrary
     * path returns null anyway, so browsing showed nothing useful even upstream. This
     * always offers the granted folders and their root instead — the exact set of chroots
     * that can actually work, scoped storage or not.
     */
    public void showFolderPicker(String s, @Nullable Activity a, Context fragment /*Fragment use*/) {
        showAllowedFolderChoice(a != null ? a : fragment);
    }

    /**
     * The chroots that can work under SAF: each allowed folder, and the directory they are all
     * listed under when there is more than one. Anything else would look fine in the UI and then
     * serve nothing.
     */
    private void showAllowedFolderChoice(Context context) {
        if (isShowingFolderPicker) return;

        final List<String> choices = new ArrayList<>();
        final String root = AllowedFolders.index().defaultChroot();
        if (root != null && !AllowedFolders.paths().contains(root)) choices.add(root);
        choices.addAll(AllowedFolders.paths());

        if (choices.isEmpty()) {
            showToast(R.string.allowed_folders_none_chosen, context);
            return;
        }

        isShowingFolderPicker = true;
        final String[] items = choices.toArray(new String[0]);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.allowed_folders_title)
                .setItems(items, (d, which) -> {
                    if (onTextEventListener != null) onTextEventListener.OnEvent(items[which]);
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.setOnDismissListener(d -> isShowingFolderPicker = false);
        dialog.show();
    }

    private void showToast(int errorResId, Context context) {
        android.widget.Toast.makeText(context, errorResId, android.widget.Toast.LENGTH_LONG).show();
    }
}