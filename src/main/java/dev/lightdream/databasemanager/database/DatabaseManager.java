package dev.lightdream.databasemanager.database;

import dev.lightdream.databasemanager.DatabaseMain;
import dev.lightdream.databasemanager.dto.IDatabaseEntry;
import dev.lightdream.databasemanager.dto.SQLConfig;
import dev.lightdream.lambda.LambdaExecutor;
import dev.lightdream.logger.Logger;

import java.io.File;
import java.util.*;

public abstract class DatabaseManager implements IDatabaseManager {

    private final static String lineSeparator = ";line_separator;";
    private final static HashMap<Class<?>, LambdaExecutor.ReturnLambdaExecutor<?, Object>> serializeMap = new HashMap<>();
    private final static HashMap<Class<?>, LambdaExecutor.ReturnLambdaExecutor<?, Object>> deserializeMap = new HashMap<>();
    public final DatabaseMain main;
    public SQLConfig sqlConfig;
    public File dataFolder;

    /**
     * @param string The string to format
     * @return String appended with " at the beginning and end
     */
    @SuppressWarnings("unused")
    public static String formatString(String string) {
        return "\"" + string + "\"";
    }

    public DatabaseManager(DatabaseMain main) {
        this.main = main;
        this.sqlConfig = main.getSqlConfig();
        this.dataFolder = main.getDataFolder();

        registerSDPair(String.class,
                object -> "\"" + object
                        .replace("\"", "")
                        .replace("'", "") + "\"",
                Object::toString);

        registerSDPair(UUID.class, object -> "\"" + object.toString() + "\"", object -> UUID.fromString(object.toString()));

        registerSDPair(ArrayList.class, DatabaseManager::serializeList, DatabaseManager::deserializeList);

        registerSDPair(List.class, DatabaseManager::serializeList, DatabaseManager::deserializeList);

        registerSDPair(Long.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Long.parseLong(object.toString());
        });
        registerSDPair(long.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Long.parseLong(object.toString());
        });

        registerSDPair(Integer.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            if (object == "true") {
                return 1;
            }
            if (object == "false") {
                return 0;
            }
            return Integer.parseInt(object.toString());
        });
        registerSDPair(int.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Integer.parseInt(object.toString());
        });

        registerSDPair(Double.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Double.parseDouble(object.toString());
        });
        registerSDPair(double.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Double.parseDouble(object.toString());
        });

        registerSDPair(Float.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Float.parseFloat(object.toString());
        });
        registerSDPair(float.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Float.parseFloat(object.toString());
        });

        registerSDPair(Boolean.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Boolean.parseBoolean(object.toString());
        });
        registerSDPair(boolean.class, object -> object, object -> {
            if (object == null) {
                return null;
            }
            return Boolean.parseBoolean(object.toString());
        });


        registerDataType(ArrayList.class, "TEXT");
        registerDataType(List.class, "TEXT");
    }

    private static ArrayList<?> deserializeList(Object object) {
        if (object == null) {
            return null;
        }

        return LambdaExecutor.LambdaCatch.ReturnLambdaCatch.executeCatch(() -> {
            if (object.toString()
                    .equals("[]")) {
                return new ArrayList<>();
            }
            String[] datas = object.toString()
                    .split(lineSeparator);
            Class<?> clazz = Class.forName(datas[0]);
            ArrayList<Object> lst = new ArrayList<>();
            for (String data : Arrays.asList(datas)
                    .subList(1, datas.length)) {
                lst.add(getObject(clazz, data));
            }
            return lst;
        }, e -> {
            Logger.error("Malformed data for " + object);
            e.printStackTrace();
            return null;
        });
    }

    private static String serializeList(Object object) {
        @SuppressWarnings("unchecked") List<Object> lst = (List<Object>) object;
        StringBuilder o1 = new StringBuilder();
        lst.forEach(entry -> o1.append(formatQueryArgument(entry).replace("\"", ""))
                .append(lineSeparator));
        o1.append(lineSeparator);
        if (o1.toString()
                .equals(lineSeparator)) {
            return "\"[]\"";
        }
        StringBuilder output = new StringBuilder(lst.get(0)
                .getClass()
                .toString()
                .replace("class ", "")).append(lineSeparator)
                .append(o1);
        return ("\"" + output.append("\"")).replace(lineSeparator + lineSeparator, "");
    }

    public static String formatQueryArgument(Object object) {
        if (object == null) {
            return "NULL";
        }
        Class<?> clazz = object.getClass();
        Object output = null;
        if (serializeMap.get(clazz) != null) {
            output = serializeMap.get(clazz)
                    .execute(object);
        }

        if (output != null) {
            return output.toString();
        }

        return object.toString();
    }

    public static Object getObject(Class<?> clazz, Object object) {
        Object output = null;

        if (object == null) {
            return null;
        }

        if (deserializeMap.get(clazz) != null) {
            output = deserializeMap.get(clazz).execute(object);
        }

        if (output != null) {
            return output;
        }

        return object;
    }

    public String getDatabaseURL() {
        switch (sqlConfig.driverName) {
            case "MYSQL":
            case "MARIADB":
            case "POSTGRESQL":
                return "jdbc:" + sqlConfig.driverName.toLowerCase() + "://" + sqlConfig.host + ":" + sqlConfig.port + "/" + sqlConfig.database + "?useSSL=" + sqlConfig.useSSL + "&autoReconnect=true";
            case "SQLSERVER":
                return "jdbc:sqlserver://" + sqlConfig.host + ":" + sqlConfig.port + ";databaseName=" + sqlConfig.database;
            case "H2":
                return "jdbc:h2:file:" + sqlConfig.database;
            case "SQLITE":
                return "jdbc:sqlite:" + new File(dataFolder, sqlConfig.database + ".db");
            default:
                throw new UnsupportedOperationException("Unsupported driver (how did we get here?): " + sqlConfig.driverName);
        }
    }

    public String getDataType(Class<?> clazz) {
        String dbDataType = sqlConfig.driver(main).dataTypes.get(clazz);

        if (dbDataType != null) {
            return dbDataType;
        }

        Logger.error("DataType " + clazz.getSimpleName() + " is not a supported data type");
        return "";
    }

    @Override
    public void save(IDatabaseEntry object) {

    }

    @SuppressWarnings({"unused", "unchecked"})
    public <R> void registerSDPair(Class<R> clazz, LambdaExecutor.ReturnLambdaExecutor<?, R> serialize, LambdaExecutor.ReturnLambdaExecutor<R, Object> deserialize) {
        serializeMap.put(clazz, (LambdaExecutor.ReturnLambdaExecutor<?, Object>) serialize);
        deserializeMap.put(clazz, deserialize);
    }

    public void registerDataType(Class<?> clazz, String dataType) {
        main.getDriverConfig().registerDataType(clazz, dataType);
    }
}
