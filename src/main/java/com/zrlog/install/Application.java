package com.zrlog.install;

import com.google.gson.Gson;
import com.hibegin.common.util.IOUtil;
import com.hibegin.http.server.WebServerBuilder;
import com.hibegin.http.server.util.PathUtil;
import com.zrlog.install.business.service.InstallService;
import com.zrlog.install.business.vo.InstallConfigVO;
import com.zrlog.install.web.InstallConstants;
import com.zrlog.install.web.config.InstallServerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Application {

    static {
        System.getProperties().put("sws.run.mode", "dev");
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.getProperties().put("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
        PathUtil.setRootPath(System.getProperty("user.dir"));
        if (args.length >= 1) {
            File configFile = new File(args[0]);
            if (configFile.exists()) {
                String jsonStr = IOUtil.getStringInputStream(new FileInputStream(configFile));
                if (args.length >= 2) {
                    jsonStr = jsonStr.replace("${github_pat}", args[1]);
                }
                InstallConfigVO config = new Gson().fromJson(jsonStr, InstallConfigVO.class);
                InstallService installService = new InstallService(InstallConstants.installConfig, config);
                boolean install = installService.install();
                System.out.println("installed = " + install);
                System.exit(install ? 0 : 1);
                return;
            }
        }
        WebServerBuilder builder = new WebServerBuilder.Builder().config(new InstallServerConfig()).build();
        builder.start();
    }
}
