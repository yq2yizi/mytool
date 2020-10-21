package com.test.mytool.ftp;

import android.util.Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import com.scengine.wtms.utils.ftp.FTPStatus;


public class ThreadFTPUtils implements Runnable {

    String TAG = "RemoteDebug:ThreadFTPUtils";

    private FtpUserInfo mFtpUserInfo;

    private FTPClient ftpClient = new FTPClient();

    private FTPType ftpType;

    public FTPType getFtpType() {
        return ftpType;
    }

    public void setFtpType(FTPType ftpType) {
        this.ftpType = ftpType;
    }

    public static enum FTPType {

        UPLOAD(0), DOWNLOAD(1), RENAME(2), DELETE(3);

        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        FTPType(int type) {
            this.type = type;
        }
    }

    /**
     * ftp地址
     */
    private final static String DEFAULT_FTP_URL = "192.168.100.10";
    /**
     * ftp端口
     */
    private final static int DEFAULT_FTP_PORT = 21;
    /**
     * ftp默认用户名
     */
    private final static String DEFAULT_FTP_NAME = "cruiser";

    /**
     * ftp默认密码
     */
    private final static String DEFAULT_FTP_PASS = "aa";

    /**
     * 对象构造 设置将过程中使用到的命令输出到控制台
     */
    public ThreadFTPUtils(String ip,int port,String username,String password,String local,String remote,FTPType ftpType)
    {
        mFtpUserInfo =new FtpUserInfo(ip, port, username, password, local, remote);
        this.ftpType=ftpType;
        this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }

    /**
     * java编程中用于连接到FTP服务器
     *
     * @param hostname 主机名
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return 是否连接成功
     * @throws IOException
     */

    public boolean connect(String hostname, int port, String username, String password) throws IOException {

        ftpClient.connect(hostname, port);

        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

            if (ftpClient.login(username, password)) {
                return true;
            }
        }
        disconnect();
        return false;
    }

    /**
     * 删除远程FTP文件
     *
     * @param remote 远程文件路径
     * @return
     * @throws IOException
     */
    public FTPStatus delete(String remote) throws IOException {
        ftpClient.enterLocalPassiveMode();

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        FTPStatus result = null;

        FTPFile[] files = ftpClient.listFiles(remote);
        if (files.length == 1) {
            boolean status = ftpClient.deleteFile(remote);
            result = status ? FTPStatus.Delete_Remote_Success : FTPStatus.Delete_Remote_Faild;
        } else {
            result = FTPStatus.Not_Exist_File;
        }
        Log.d(TAG, "delete: FTP服务器文件删除标识：" + result);
        return result;
    }

    /**
     * 重命名远程FTP文件
     *
     * @param name   新远程文件名称(路径-必须保证在同一路径下)
     * @param remote 远程文件路径
     * @return 是否成功
     * @throws IOException
     */
    public FTPStatus rename(String name, String remote) throws IOException {
        ftpClient.enterLocalPassiveMode();

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        FTPStatus result = null;

        FTPFile[] files = ftpClient.listFiles(remote);
        if (files.length == 1) {
            boolean status = ftpClient.rename(remote, name);
            result = status ? FTPStatus.Remote_Rename_Success : FTPStatus.Remote_Rename_Faild;
        } else {
            result = FTPStatus.Not_Exist_File;
        }
        Log.d(TAG, "rename: " + "FTP服务器文件名更新标识：" + result);
        return result;
    }

    /**
     * 从FTP服务器上下载文件
     *
     * @param remote 远程文件路径
     * @param local  本地文件路径
     * @return 是否成功
     * @throws IOException
     */

    @SuppressWarnings("resource")
    public FTPStatus download(String remote, String local) throws IOException {

        ftpClient.enterLocalPassiveMode();

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        FTPStatus result;

        File f = new File(local);

        FTPFile[] files = ftpClient.listFiles(remote);

        if (files.length != 1) {
            Log.d(TAG, "download: 远程文件不唯一");
            return FTPStatus.File_Not_Unique;
        }

        long lRemoteSize = files[0].getSize();

        if (f.exists()) {
            OutputStream out = new FileOutputStream(f, true);
            Log.d(TAG, "download: 本地文件大小为:" + f.length());

            if (f.length() >= lRemoteSize) {

                Log.d(TAG, "download: 本地文件大小大于远程文件大小，下载中止");
                return FTPStatus.Remote_smaller_local;
            }

            ftpClient.setRestartOffset(f.length());

            boolean status = ftpClient.retrieveFile(remote, out);
            result = status ? FTPStatus.Download_From_Break_Success : FTPStatus.Download_From_Break_Faild;
            out.close();

        } else {
            OutputStream out = new FileOutputStream(f);
            boolean status = ftpClient.retrieveFile(remote, out);
            result = status ? FTPStatus.Download_From_Break_Success : FTPStatus.Download_From_Break_Faild;
            out.close();
        }

        return result;

    }

    /**
     * 上传文件到FTP服务器，支持断点续传
     *
     * @param local  本地文件名称，绝对路径
     * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext
     *               按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
     * @return 上传结果
     * @throws IOException
     */

    @SuppressWarnings("resource")
    public FTPStatus upload(String local, String remote) throws IOException {
        // 设置PassiveMode传输
        ftpClient.enterLocalPassiveMode();

        // 设置以二进制流的方式传输
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        FTPStatus result;

        // 对远程目录的处理
        String remoteFileName = remote;

        if (remote.contains("/")) {

            remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);

            String directory = remote.substring(0, remote.lastIndexOf("/") + 1);

            if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) {

                // 如果远程目录不存在，则递归创建远程服务器目录

                int start = 0;

                int end = 0;

                if (directory.startsWith("/")) {

                    start = 1;

                } else {

                    start = 0;

                }

                end = directory.indexOf("/", start);

                while (true) {

                    String subDirectory = remote.substring(start, end);

                    if (!ftpClient.changeWorkingDirectory(subDirectory)) {

                        if (ftpClient.makeDirectory(subDirectory)) {

                            ftpClient.changeWorkingDirectory(subDirectory);

                        } else {

                            Log.d(TAG, "upload: 创建目录失败");

                            return FTPStatus.Create_Directory_Fail;

                        }

                    }

                    start = end + 1;

                    end = directory.indexOf("/", start);

                    // 检查所有目录是否创建完毕

                    if (end <= start) {

                        break;

                    }

                }

            }

        }

        // 检查远程是否存在文件

        FTPFile[] files = ftpClient.listFiles(remoteFileName);

        if (files.length == 1) {

            long remoteSize = files[0].getSize();

            File f = new File(local);

            long localSize = f.length();

            if (remoteSize == localSize) {

                return FTPStatus.File_Exits;

            } else if (remoteSize > localSize) {

                return FTPStatus.Remote_Bigger_Local;

            }

            // 尝试移动文件内读取指针,实现断点续传

            InputStream is = new FileInputStream(f);

            if (is.skip(remoteSize) == remoteSize) {

                ftpClient.setRestartOffset(remoteSize);

                if (ftpClient.storeFile(remote, is)) {

                    return FTPStatus.Upload_From_Break_Success;

                }

            }

            // 如果断点续传没有成功，则删除服务器上文件，重新上传

            if (!ftpClient.deleteFile(remoteFileName)) {

                return FTPStatus.Delete_Remote_Faild;

            }

            is = new FileInputStream(f);

            if (ftpClient.storeFile(remote, is)) {

                result = FTPStatus.Upload_New_File_Success;

            } else {

                result = FTPStatus.Upload_New_File_Failed;

            }

            is.close();

        } else {

            InputStream is = new FileInputStream(local);

            if (ftpClient.storeFile(remoteFileName, is)) {

                result = FTPStatus.Upload_New_File_Success;

            } else {

                result = FTPStatus.Upload_New_File_Failed;

            }

            is.close();
        }

        return result;

    }

    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */

    public void disconnect() throws IOException {

        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }

    }


    @Override
    public void run() {
        boolean status = false;
        // 建立FTP连接
        try {
            ftpClient.connect(mFtpUserInfo.getIp(), mFtpUserInfo.getPort());

            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

                if (ftpClient.login(mFtpUserInfo.getUsername(), mFtpUserInfo.getPassword())) {
                    status = true;
                }
            } else {
                try {
                    disconnect();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        } catch (SocketException e1) {

            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // FTP连接成功后执行相应的操作
        if (status) {
            FTPStatus result = null;
            if (this.ftpType == FTPType.UPLOAD) {
                try {
                    result = this.upload(mFtpUserInfo.getLocal(), mFtpUserInfo.getRemote());// 上传文件
                } catch (IOException e) {
                    Log.d(TAG, "run: FTP上传文件异常：" + e.getMessage());
                }
            } else if (this.ftpType == FTPType.DOWNLOAD) {
                try {
                    result = this.download(mFtpUserInfo.getRemote(), mFtpUserInfo.getLocal());// 下载文件
                } catch (IOException e) {
                    Log.d(TAG, "run: FTP下载文件异常：" + e.getMessage());
                }
            } else if (this.ftpType == FTPType.RENAME) {
                try {
                    result = this.rename(mFtpUserInfo.getLocal(), mFtpUserInfo.getRemote());// 修改名称
                } catch (IOException e) {
                    Log.d(TAG, "run: FTP修改文件名称异常：" + e.getMessage());
                }
            } else if (this.ftpType == FTPType.DELETE) {
                try {
                    result = this.delete(mFtpUserInfo.getRemote());                    // 删除文件
                } catch (IOException e) {
                    Log.d(TAG, "run: FTP删除文件异常：" + e.getMessage());
                }
            }
            try {
                disconnect();
            } catch (IOException e) {

                Log.d(TAG, "run: FTP连接释放异常：" + e.getMessage());
            }
            Log.d(TAG, "run: FTP操作状态码:" + result);
        } else {
            Log.d(TAG, "run: FTP没有连接成功");
        }

    }

//    public static void main(String[] args) {
//        ThreadFTPUtils myFtp = new ThreadFTPUtils("192.168.1.200", 21, "duser", "HTPDuserXP32", "C:\\Users\\Administrator\\Desktop\\swing.drawer.jar", "/jars/boonya.jar", FTPType.UPLOAD);
//        Thread thread = new Thread(myFtp);
//        thread.start();
//
//    }
}
