package com.daren.chen.im.core.banner;

import java.io.PrintStream;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.JimVersion;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-02 01:12
 */
public class JimBanner implements Banner, ImConst {

    private static final String BANNER = "\n" + " 8888888 888b     d888      \n" + " 888   8888b   d8888      \n"
        + " 888   88888b.d88888      \n" + " 888   888Y88888P888      \n" + " 888   888 Y888P 888      \n"
        + " 888   888  Y8P  888      \n" + " 888   888   \"   888      \n" + " 8888888 888       888  " + " ";

    private static final String JIM = " :: " + ImConst.JIM + " :: ";

    @Override
    public void printBanner(PrintStream printStream) {
        printStream.println(BANNER);
        String version = " (" + JimVersion.version + ")";
        printStream.println(JIM + version + "\n");
    }

}
