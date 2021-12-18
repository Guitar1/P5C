package com.zzx.police.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zzx.police.ZZXConfig;
import com.zzx.police.data.Values;
import com.zzx.police.services.WebThread;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;


/**
 *
 */
public class FTPContinue {

    private static final String TAG = "FTPContinue: ";
    public FTPClient ftpClient = null;
    public static final String FTP_DEFAULT_IP   = "219.136.187.242";
    public static final String FTP_IP_OTHER = "218.64.169.250";
    public static final int FTP_DEFAULT_PORT    = 21;
    public static final String FTP_DEFAULT_USER = "thinkland";
    public static final String FTP_DEFAULT_PASS = "thinkland";
    public static final String FTP_USER_OTHER = "vsftpd";
    public static final String FTP_PASS_OTHER = "vsftpd";
    private String ftpHost = "";
    private int ftpPort = 21;
    private String ftpUserName = "";
    private String ftpPassword = "";
    private String fileName = "";

    private int finishCount = 0;
    private Context mContext;
    private String mRemoteDir = "";
    private FTPFile[] ftpFiles = null;
    public boolean mReleased = false;

    public FTPContinue(Context context, Handler handler, String hostIp, int hostPort, String userName, String password) {
        mContext    = context;
        ftpClient   = new FTPClient();
        ftpClient.setAutoNoopTimeout(5 * 1000);
        //MTK
        ftpClient.setPassive(true);
        //小米
//        ftpClient.setPassive(false);
        ftpHost = hostIp;
        ftpPort = hostPort;
        ftpUserName = userName;
        ftpPassword = password;
        connect();
	}

	private boolean connect() {
        Values.LOG_W(TAG, "connect.EPO_FTP_HOST " + ftpHost + "; EPO_FTP_PORT = " + ftpPort + "; EPO_FTP_USER_NAME = " + ftpUserName + "; EPO_FTP_PASSWORD = " + ftpPassword);

		try {
			ftpClient.connect(ftpHost, ftpPort);
			ftpClient.login(ftpUserName, ftpPassword);
		} catch (Exception e) {
			e.printStackTrace();
            try {
                ftpClient.setPassive(true);
                ftpClient.connect(ftpHost, ftpPort);
                ftpClient.login(ftpUserName, ftpPassword);
            } catch (Exception e1) {
                e1.printStackTrace();
                EventBusUtils.postEvent(Values.BUS_EVENT_FTP, Values.FTP_LOGIN_FAILED);
                return false;
            }
		}
        EventBusUtils.postEvent(Values.BUS_EVENT_FTP, Values.FTP_LOGIN_SUCCESS);
		return true;
	}

	public void download(String remoteDir, String remoteFileName, String localDirPath, ProgressBar progressBar) {
//        Values.LOG_V(TAG, "remoteDir = " + remoteDir + "; remoteFileName = " + remoteFileName + "; localDirPath = " + localDirPath);
        fileName = remoteFileName;
        finishCount = 0;
		try {
            if (!mRemoteDir.equals(remoteDir)) {
                ftpClient.changeDirectory(remoteDir);
                ftpFiles = ftpClient.list();
                mRemoteDir = remoteDir;
            }
			for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().contains(remoteFileName)) {
//                    Values.LOG_W(TAG, "fileName = " + ftpFile.getName() + "; size = " + ftpFile.getSize());
                    new FtpDownLoadRunnable(remoteDir + ftpFile.getName(), localDirPath + ftpFile.getName(), ftpFile, progressBar)
                        .download();
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}

    public void upload(String localFilePath, String remoteDir) {
        new CmdUpload(localFilePath, remoteDir).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
	
	public void disconnect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (ftpClient != null && ftpClient.isConnected()) {
                        ftpClient.abortCurrentDataTransfer(true);
                        ftpClient.logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (ftpClient != null) {
                        ftpClient.disconnect(true);
                        ftpClient = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
	}

    public void release() {
        mReleased = true;
        disconnect();
    }

	public void abortDateDisconnect() {
		try {
			ftpClient.abortCurrentDataTransfer(true);
//			if (ftpClient != null && ftpClient.isConnected()) {
//				 ftpClient.abortCurrentDataTransfer(true);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public boolean isConnected() {
        return ftpClient != null && ftpClient.isConnected();
    }

    class FtpDownLoadRunnable {
		private String remote = null;
		private String local = null;
		private FTPFile ftpFile = null;
        private ProgressBar progressBar = null;

		public FtpDownLoadRunnable(String remote, String localFile, FTPFile ftpFile, ProgressBar progressBar) {
			this.remote = remote;
			this.local = localFile;
			this.ftpFile = ftpFile;
		}

		public void download() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
			new CmdDownLoad(remote, local, ftpFile, progressBar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

    private class UploadFTPDataTransferListener implements FTPDataTransferListener {
        private long tmpTotalTransferred = 0;
        private long totalTransferred = 0;
        private long fileSize = -1;
        private String fileName = null;
        Intent downloadIntent = null;

        private UploadFTPDataTransferListener(String fileName, long fileSize) {
            if (fileSize <= 0) {
                throw new RuntimeException(
                        "the size of file must be larger than zero.");
            }
            this.fileName = fileName;
            this.fileSize = fileSize;
            downloadIntent = new Intent(Values.ACTION_FILE_UPLOAD_LENGTH);
        }

        @Override
        public void started() {

        }

        @Override
        public void transferred(int length) {
            tmpTotalTransferred += length;
            if (tmpTotalTransferred >= 1024 * 50 || fileSize - totalTransferred < 1024 * 50) {
                totalTransferred += tmpTotalTransferred;
                Values.LOG_I(TAG, fileName + ".length ========= " + totalTransferred);
                downloadIntent.putExtra(Values.UPLOAD_FILE_LENGTH, totalTransferred);
                downloadIntent.putExtra(Values.UPLOAD_FILE_NAME, fileName);
                mContext.sendBroadcast(downloadIntent);
                tmpTotalTransferred = 0;
            }
        }

        @Override
        public void completed() {
            Intent intent = new Intent();
            intent.setAction(Values.ACTION_FILE_UPLOAD_FINISH);
            intent.putExtra(Values.UPLOAD_FILE_NAME, fileName);
            mContext.sendBroadcast(intent);
            Values.LOG_I(TAG, "upload finish = " + fileName);
            EventBusUtils.postEvent(Values.BUS_EVENT_FTP_UPLOAD_FINISH, fileName);
        }

        @Override
        public void aborted() {

        }

        @Override
        public void failed() {
            Intent intent = new Intent();
            intent.setAction(Values.ACTION_FILE_UPLOAD_FAILED);
            mContext.sendBroadcast(intent);
        }
    }

	private class DownLoadFTPDataTransferListener implements FTPDataTransferListener {

		private int totalTransferred = 0;
		private long mFileSize = -1;
        private ProgressBar mProgressBar = null;
		
		public DownLoadFTPDataTransferListener(long fileSize, ProgressBar progressBar) {
			if (fileSize <= 0) {
				throw new RuntimeException("the size of file must be larger than zero.");
			}
            this.mProgressBar = progressBar;
			this.mFileSize = fileSize;
		}
		
		@Override
		public void aborted() {
			
		}
		
		@Override
		public void completed() {
            if (mProgressBar != null)
                setLoadProgress(mProgressBar.getMax());
        }

		@Override
		public void failed() {
		
		}
		
		@Override
		public void started() {
			
		}
		
		@Override
		public void transferred(int length) {
			totalTransferred += length;
			float percent = (float) totalTransferred / mFileSize;
            if (mProgressBar != null)
			    setLoadProgress((int) (percent * mProgressBar.getMax()));
		}

        public void setLoadProgress(int progress) {
            if (mProgressBar != null) {
                mProgressBar.setProgress(progress);
            }
        }
	}

	class CmdDownLoad extends AsyncTask<Void, Integer, Boolean> {
		private String remote   = null;
		private String local    = null;
		private FTPFile ftpFile = null;
        private ProgressBar progressBar;
		public CmdDownLoad(String remote, String local, FTPFile ftpFile, ProgressBar progressBar) {
			this.remote = remote;
			this.local  = local;
			this.ftpFile = ftpFile;
            this.progressBar = progressBar;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				File f = new File(local);
                if(f.exists()){
                    Values.LOG_E(TAG, "remote = " + remote + "; local = " + local + "; localSize = " + f.length() + "; remoteSize = " + ftpFile.getSize());
                    long localSize = f.length();
                    if (localSize >= ftpFile.getSize()) {
                        return true;
                    }
                    ftpClient.download(remote, new File(local), localSize, new DownLoadFTPDataTransferListener(ftpFile.getSize(), progressBar));
                } else {
                    Values.LOG_E(TAG, "remote = " + remote + "; local = " + local);
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    ftpClient.download(remote, f, 0);
                }
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
            Values.LOG_E(TAG, "remote = " + remote + ";download finish");
            Toast.makeText(mContext, remote + "download finish", Toast.LENGTH_SHORT).show();
		}
	}

    class CmdUpload extends AsyncTask<String, Integer, Boolean> {
        private String mRemoteDir   = null;
        private File mLocalFile;
        public CmdUpload(String localFileName, String remoteDir) {
            mLocalFile  = new File(localFileName);
            mRemoteDir  = remoteDir;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Values.LOG_I(TAG, "CmdUpload.doInBackground.ThreadId = " + Thread.currentThread().getId());
            try {
                ftpClient.createDirectory(mRemoteDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!mLocalFile.exists()) {
                    DatabaseUtils.deleteHistory(mContext, new String[] {mLocalFile.getName()});
                    return false;
                }
                ftpClient.changeDirectory(mRemoteDir);
                /*FTPFile[] ftpFileList = ftpClient.list();
                boolean needContinue = false;
                FTPFile ftpFileRemote = null;
                if (ftpFileList != null && ftpFileList.length > 0) {
                    for (FTPFile ftpFile : ftpFileList) {
                        if (ftpFile.getName().equals(mLocalFile.getName())) {
                            ftpFileRemote = ftpFile;
                            needContinue = true;
                            break;
                        }
                    }
                }
                if (needContinue) {
                    long size = ftpFileRemote.getSize();
                    ftpClient.upload(mLocalFile, size, new UploadFTPDataTransferListener(mLocalFile.getAbsolutePath(), mLocalFile.length()));
                } else {*/
                try {
                    String path = new File(mRemoteDir, mLocalFile.getName()).getAbsolutePath();
                    ftpClient.deleteFile(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Values.LOG_E(TAG, "CmdUpload.doInBackground upload fileName = " + mLocalFile.getName());
                ftpClient.upload(mLocalFile, new UploadFTPDataTransferListener(mLocalFile.getAbsolutePath(), mLocalFile.length()));
//                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            WebThread.FileStatus status = new WebThread.FileStatus(mLocalFile.getName(), mRemoteDir, success);
            EventBusUtils.postEvent(WebThread.BUS_HTTP_UPLOAD, status);
            super.onPostExecute(success);
        }
    }

}
