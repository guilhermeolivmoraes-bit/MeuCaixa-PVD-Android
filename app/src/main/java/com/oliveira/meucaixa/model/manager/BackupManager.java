package com.oliveira.meucaixa.model.manager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupManager {

    private static final String DATABASE_NAME = "meu_caixa_database";

    public static boolean exportDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e("BackupManager", "Database file does not exist.");
                return false;
            }

            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MeuCaixaBackups");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(exportDir, "backup_" + timestamp + ".sqlite");

            copyFile(dbFile, backupFile);

            // Also copy the WAL and SHM files if they exist (Write-Ahead Logging)
            File dbWalFile = new File(dbFile.getPath() + "-wal");
            if (dbWalFile.exists()) {
                copyFile(dbWalFile, new File(exportDir, backupFile.getName() + "-wal"));
            }

            File dbShmFile = new File(dbFile.getPath() + "-shm");
            if (dbShmFile.exists()) {
                copyFile(dbShmFile, new File(exportDir, backupFile.getName() + "-shm"));
            }

            Log.d("BackupManager", "Backup created successfully at: " + backupFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("BackupManager", "Error exporting database", e);
            return false;
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream inStream = new FileInputStream(src);
             FileOutputStream outStream = new FileOutputStream(dst);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
}
