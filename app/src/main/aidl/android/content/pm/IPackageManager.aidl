package android.content.pm;

import android.content.pm.IPackageDataObserver;

interface IPackageManager {
    void deleteApplicationCacheFilesAsUser(
        in String packageName,
        int userId,
        in IPackageDataObserver observer
    );

    void deleteApplicationCacheFiles(
        in String packageName,
        in IPackageDataObserver observer
    );
}
