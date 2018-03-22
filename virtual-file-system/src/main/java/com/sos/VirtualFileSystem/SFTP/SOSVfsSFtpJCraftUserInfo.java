package com.sos.VirtualFileSystem.SFTP;

import java.util.Scanner;

import com.jcraft.jsch.*;

public class SOSVfsSFtpJCraftUserInfo implements UserInfo, UIKeyboardInteractive {

    private String passwd;
    private Scanner scanner = new Scanner(System.in);

    public String getPassword() {
        return passwd;
    }

    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
        System.out.print(prompt[0]);
        if (scanner.hasNext()) {
            passwd = scanner.next();
        }
        String[] response = new String[prompt.length];
        response[0] = passwd;
        // for(int i=0; i<prompt.length; i++){
        // response[i]=texts[i].getText();
        // }

        return response;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public boolean promptPassword(String message) {
        System.out.print("Enter Password: ");
        passwd = scanner.next();
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        System.out.print(message + "(yes|no) ");
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("yes");
    }

    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }

}