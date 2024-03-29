package dev.lightdream.databasemanager;

import dev.lightdream.databasemanager.config.SQLConfig;
import dev.lightdream.logger.LoggableMain;
import dev.lightdream.logger.Logger;
import dev.lightdream.messagebuilder.MessageBuilderManager;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;

@SuppressWarnings("unused")
public class TestDatabaseMain implements DatabaseMain, LoggableMain {

    public static TestDatabaseMain instance;

    private @Getter SQLConfig sqlConfig;

    private @Getter Reflections reflections;

    private @Getter TestDatabaseManager databaseManager;

    public TestDatabaseMain() {
        instance = this;
        Logger.init(this);
        MessageBuilderManager.init();

        sqlConfig = new SQLConfig();
        sqlConfig.driverType = SQLConfig.DriverType.SQLITE;

        ClassLoader[] classLoaders = new ClassLoader[]{getClass().getClassLoader()};
        reflections = new Reflections(new ConfigurationBuilder()
                .setClassLoaders(classLoaders)
                .forPackages("dev.lightdream.databasemanager")
        );

        databaseManager = new TestDatabaseManager(this);
    }

    @Override
    public File getDataFolder() {
        return new File(System.getProperty("user.dir"));
    }

    @Override
    public boolean debugToConsole() {
        return true;
    }
}
