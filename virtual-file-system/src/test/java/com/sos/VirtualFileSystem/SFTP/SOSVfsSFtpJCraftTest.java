package com.sos.VirtualFileSystem.SFTP;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;



public class SOSVfsSFtpJCraftTest extends SOSVfsSFtpTest{


	public SOSVfsSFtpJCraftTest() {
		//
	}


	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		dynamicClassNameSource = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft";
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		ftpClient.disconnect();
	}


	@Override
	@Test
	public void testConnect() throws Exception {
		super.testConnect();
	}
	
	@Override
	@Test
	public void testHttpProxyConnect() throws Exception {
		super.testHttpProxyConnect();
	}
	
	@Override
	@Test
	public void testConnectOpenSSH() throws Exception {
		super.testConnectOpenSSH();
	}


	@Override
	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
  @Ignore("Test set to Ignore for later examination")
	public void testConnectWithWrongPortNumber() throws Exception {
		super.testConnectWithWrongPortNumber();
	}

	@Override
	@Test
	public void testAuthenticate() throws Exception {
		super.testAuthenticate();
	}

	@Test
	public void testAuthenticateWithZlib() throws Exception {
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.use_zlib_compression.value(true);
		objSource.zlib_compression_level.value(2);
		super.testAuthenticate();
	}


	@Override
	@Test
	public void testMkdir() throws Exception {
		super.testMkdir();
	}

	@Override
	@Test
	public void testMkdirMultiple() throws Exception {
		super.testMkdirMultiple();
	}

	@Override
	@Test
	public void testRmdirString() {
		super.testRmdirString();
	}


	@Override
	@Test
	public void testNListString() {
		super.testNListString();
	}

	@Override
	@Test
	public void testIsNotHiddenFile() {
		super.testIsNotHiddenFile();
	}

	@Override
	@Test
	public void testNListStringBoolean() {
		super.testNListStringBoolean();
	}

	@Override
	@Test
	public void testNList() {
		super.testNList();
	}

	@Override
	@Test
	public void testNListBoolean() throws Exception {
		super.testNListBoolean();
	}

	@Override
	@Test
	public void testDirString() {
		super.testDirString();
	}

	@Override
	@Test
	public void testDirStringInt() {
		super.testDirStringInt();
	}

	@Override
	@Test
	public void testListNames() {
		super.testListNames();
	}

	@Override
	@Test
	public void testDir() {
		super.testDir();
	}

	@Override
	@Test
	public void testGetResponse() {
		super.testGetResponse();
	}

	@Override
	@Test
	public void testSize() {
		super.testSize();
	}

	@Override
	@Test
	public void testGetFileStringStringBoolean() {
		super.testGetFileStringStringBoolean();
	}

	@Override
	@Test
	public void testGetFileStringString() {
		super.testGetFileStringString();
	}

	@Override
	@Test
	public void testPut() {
		super.testPut();
	}

	@Override
	@Test
	public void testPutFileStringString() {
		super.testPutFileStringString();
	}

	@Override
	@Test
	public void testPutFileStringOutputStream() {
		super.testPutFileStringOutputStream();
	}

	@Override
	@Test
	public void testGetClient() {
		super.testGetClient();
	}

	@Override
	@Test
	public void testAppendFile() {
		super.testAppendFile();
	}

	@Override
	@Test
	public void testCd() throws Exception{
		super.testCd();
	}

	@Override
	@Test
	public void testChangeWorkingDirectory() throws Exception {
		super.testChangeWorkingDirectory();
	}

	@Override
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testDelete() throws Exception{
		super.testDelete();
	}

	@Override
	@Test
	public void testLogin() throws Exception{
		super.testLogin();
	}

	@Override
	@Test
	public void testDisconnect() throws Exception{
		super.testDisconnect();
	}

	@Override
	@Test
	public void testGetReplyString() throws Exception {
		super.testGetReplyString();
	}

	@Override
	@Test
	public void testIsConnected() throws Exception {
		super.testIsConnected();
	}

	@Override
	@Test
	public void testLogout() throws Exception{
		super.testLogout();
	}

	@Override
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testRename() throws Exception{
		super.testRename();
	}

	@Override
	@Test
	public void testGetHandler() throws Exception {
		super.testGetHandler();
	}

	@Override
	@Test
	public void testExecuteCommand() throws Exception {
		super.testExecuteCommand();
	}

	@Override
	@Test
	public void testCreateScriptFile() throws Exception{
		super.testCreateScriptFile();
	}

	@Override
	@Test
	public void testGetExitCode() throws Exception{
		super.testGetExitCode();
	}

	@Override
	@Test
	public void testGetExitSignal() throws Exception{
		super.testGetExitSignal();
	}

	@Override
	@Test
	public void testCloseConnection() throws Exception{
		super.testCloseConnection();
	}

	@Override
	@Test
	public void testConnect1() throws Exception{
		super.testConnect1();
	}

	@Override
	@Test
	public void testConnectSOSConnection2OptionsAlternate() throws Exception{
		super.testConnectSOSConnection2OptionsAlternate();
	}

	@Override
	@Test
	public void testConnectISOSConnectionOptions() throws Exception{
		super.testConnectISOSConnectionOptions();
	}

	@Override
	@Test
	public void testConnectStringInt() throws Exception{
		super.testConnectStringInt();
	}

	@Override
	@Test
	public void testCloseSession() throws Exception{
		super.testCloseSession();
	}

	@Override
	@Test
	public void testOpenSession() throws Exception{
		super.testOpenSession();
	}

	@Override
	@Test
	public void testTransferMode() throws Exception{
		super.testTransferMode();
	}

	@Override
	@Test
	public void testGetNewVirtualFile() throws Exception{
		super.testGetNewVirtualFile();
	}

	@Override
	@Test
	public void testMkdirSOSFolderName() throws Exception{
		super.testMkdirSOSFolderName();
	}

	@Override
	@Test
	public void testRmdirSOSFolderName() throws Exception{
		super.testRmdirSOSFolderName();
	}

	@Override
	@Test
	public void testGetConnection() throws Exception{
		super.testGetConnection();
	}

	@Override
	@Test
	public void testGetSession() throws Exception{
		super.testGetSession();
	}

	@Override
	@Test
	public void testDirSOSFolderName() throws Exception{
		super.testDirSOSFolderName();
	}

	@Override
	@Test
	public void testGetStdErr() throws Exception{
		super.testGetStdErr();
	}

	@Override
	@Test
	public void testGetStdOut() throws Exception{
		super.testGetStdOut();
	}

	@Override
	@Test
	public void testRemoteIsWindowsShell() throws Exception{
		super.testRemoteIsWindowsShell();
	}

	@Override
	@Test
	public void testSetJSJobUtilites() throws Exception{
		super.testSetJSJobUtilites();
	}

	@Override
	@Test
	public void testGetFileHandle() throws Exception{
		super.testGetFileHandle();
	}

	@Override
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testGetFilelist() throws Exception{
		super.testGetFilelist();
	}
}
