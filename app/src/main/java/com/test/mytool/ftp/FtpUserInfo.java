package com.test.mytool.ftp;

class FtpUserInfo {
	
	private String ip;	    //FTP服务器的IP地址
	
	private int port;	    //FTP服务器端口
	
	private String username;//登录用户名
	
	private String password;//登录密码
	
	private String local;   //本地文件或文件名
	
	private String remote;  //远程文件或路径
	
	public String getIp()
	{
		return ip;
	}
 
	public void setIp(String ip)
	{
		this.ip = ip;
	}
 
	public int getPort()
	{
		return port;
	}
 
	public void setPort(int port)
	{
		this.port = port;
	}
 
	public String getUsername()
	{
		return username;
	}
 
	public void setUsername(String username)
	{
		this.username = username;
	}
 
	public String getPassword()
	{
		return password;
	}
 
	public void setPassword(String password)
	{
		this.password = password;
	}
 
	public String getLocal()
	{
		return local;
	}
 
	public void setLocal(String local)
	{
		this.local = local;
	}
 
	public String getRemote()
	{
		return remote;
	}
 
	public void setRemote(String remote)
	{
		this.remote = remote;
	}
 
	public FtpUserInfo()
	{
		
	}
 
	public FtpUserInfo(String ip, int port, String username, String password, String local, String remote)
	{
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.local = local;
		this.remote = remote;
	}
	
}