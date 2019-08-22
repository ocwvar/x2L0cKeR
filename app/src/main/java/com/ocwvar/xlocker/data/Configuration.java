package com.ocwvar.xlocker.data;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ocwvar.xlocker.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;

public class Configuration {

    /**
     * 配置目录路径
     */
    public static final String PATH_CONFIG_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/.xcl/";

    /**
     * 主配置文件路径
     */
    public static final String PATH_CONFIG_FILE_MAIN = PATH_CONFIG_FOLDER + ".config.json";

    /**
     * 忽略配置文件路径
     */
    public static final String PATH_IGNORE_FILE_MAIN = PATH_CONFIG_FOLDER + ".ignore.json";

    /**
     * 日志文件路径
     */
    public static final String PATH_LOG_FILE = PATH_CONFIG_FOLDER + "log.txt";


    //更新线程
    private Thread updateThread;

    //全局Context
    private Context applicationContext;

    public Configuration(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 开始更新配置文件数据
     */
    public void startLoadingTask() {
        if (this.updateThread != null && !this.updateThread.isInterrupted()) {
            return;
        }

        _createNewThread();
        this.updateThread.start();
    }

    /**
     * 停止更新配置文件
     */
    public void cancelLoading() {
        if (this.updateThread == null) {
            return;
        }

        this.updateThread.interrupt();
        this.updateThread = null;
    }

    /**
     * 创建新的更新线程
     */
    private void _createNewThread() {
        this.updateThread = new Thread(new Runnable() {

            /**
             * 文件读取缓冲大小
             */
            private final int BUFFER_SIZE = 512;

            /**
             * 配置文件对象
             */
            private final File CONFIG_FILE_OBJECT = new File(Configuration.PATH_CONFIG_FILE_MAIN);

            /**
             * 忽略配置文件对象
             */
            private final File IGNORE_FILE_OBJECT = new File(Configuration.PATH_IGNORE_FILE_MAIN);

            /**
             * 从文件读取文本出来
             *
             * @param file  要读取的文件
             * @param bufferSize    缓存尺寸
             *
             * @return 读取得到的文本，如果读取失败，则返回NULL
             */
            private @Nullable
            String file2Text(File file, int bufferSize) {
                try (
                        final FileInputStream inputStream = new FileInputStream(file);
                        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available())
                ) {
                    final byte[] buffer = new byte[bufferSize];
                    int readLength;

                    while ((readLength = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, readLength);
                    }
                    outputStream.flush();

                    return new String(outputStream.toByteArray(), Charset.forName("utf-8"));
                } catch (Exception e) {
                    return null;
                }
            }

            /**
             * 输出文件
             *
             * @param assetsPath    Assets文件路径
             * @param outputFileLocation    输出的文件路径
             * @param bufferSize 缓存尺寸
             *
             * @return 是否执行成功
             */
            private boolean outputFile(String assetsPath, File outputFileLocation, int bufferSize) {
                final byte[] buffer = new byte[bufferSize];
                int length;
                try (
                        final FileOutputStream outputStream = new FileOutputStream(outputFileLocation);
                        final InputStream inputStream = applicationContext.getAssets().open(assetsPath)
                ) {
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.flush();
                    return true;
                } catch (Exception e) {
                    _outputLog("释放文件:" + assetsPath + " 目标:" + outputFileLocation.getPath() + " 失败:" + e);
                    return false;
                }
            }

            /**
             * 解析配置应用列表
             *
             * @param loadedJsonObject  读取到的Json数据
             *
             * @return 列表数据，解析失败返回 NULL
             */
            private @Nullable
            LinkedHashMap<String, App> decodeAppListConfig(JsonObject loadedJsonObject) {
                try {
                    JsonObject jsonObject;
                    final LinkedHashMap<String, App> result = new LinkedHashMap<>();
                    for (final JsonElement jsonElement : loadedJsonObject.get("lock").getAsJsonArray()) {
                        jsonObject = jsonElement.getAsJsonObject();
                        result.put(
                                jsonObject.get("name").getAsString(),
                                new App(
                                        jsonObject.get("name").getAsString(),
                                        jsonObject.has("groupId") ? jsonObject.get("groupId").getAsInt() : 0
                                )
                        );
                    }
                    return result;
                } catch (Exception e) {
                    _outputLog("解析 lock字段 失败:" + e);
                    return null;
                }
            }

            /**
             * 解析配置规则组别列表
             *
             * @param loadedJsonObject  读取到的Json数据
             *
             * @return 列表数据，解析失败返回 NULL
             */
            private @Nullable
            LinkedHashMap<Integer, Group> decodeGroupListConfig(JsonObject loadedJsonObject) {
                try {
                    JsonObject jsonObject;
                    String[] tempStringArray;
                    final LinkedHashMap<Integer, Group> result = new LinkedHashMap<>();
                    for (final JsonElement jsonElement : loadedJsonObject.get("group").getAsJsonArray()) {
                        jsonObject = jsonElement.getAsJsonObject();
                        tempStringArray = jsonObject.get("start").getAsString().split(":");
                        final int[] startTime = new int[]{
                                Integer.parseInt(tempStringArray[0]),
                                Integer.parseInt(tempStringArray[1])
                        };

                        tempStringArray = jsonObject.get("end").getAsString().split(":");
                        final int[] endTime = new int[]{
                                Integer.parseInt(tempStringArray[0]),
                                Integer.parseInt(tempStringArray[1])
                        };

                        result.put(
                                jsonObject.get("id").getAsInt(),
                                new Group(
                                        jsonObject.get("id").getAsInt(),
                                        startTime,
                                        endTime
                                )
                        );
                    }
                    return result;
                } catch (Exception e) {
                    _outputLog("解析 group字段 失败:" + e);
                    return null;
                }
            }

            /**
             * 解析忽略应用配置列表
             *
             * @param loadedJsonObject  读取到的Json数据
             *
             * @return 列表数据，解析失败返回 NULL
             */
            private @Nullable
            IgnoreApp[] decodeIgnoreAppsList(JsonObject loadedJsonObject) {
                try {
                    final JsonArray jsonArray = loadedJsonObject.get("ignoreArray").getAsJsonArray();
                    final IgnoreApp[] ignoreAppsList = new IgnoreApp[jsonArray.size()];
                    JsonObject jsonObject;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        jsonObject = jsonArray.get(i).getAsJsonObject();
                        ignoreAppsList[i] = new IgnoreApp(
                                jsonObject.get("packageName").getAsString(),
                                jsonObject.get("type").getAsInt(),
                                jsonObject.get("description").getAsString()
                        );
                    }
                    return ignoreAppsList;
                } catch (Exception e) {
                    _outputLog("解析 ignore字段 失败:" + e);
                    return null;
                }
            }

            /**
             * 解析主配置对象
             *
             * @param loadedJsonObject  读取到的Json数据
             * @return 数据对象，解析失败返回 NULL
             */
            private @Nullable
            Config decodeConfigObject(JsonObject loadedJsonObject) {
                try {
                    return new Config(
                            true,
                            loadedJsonObject.get("updateInterval").getAsLong(),
                            LockType.values()[loadedJsonObject.get("lockType").getAsInt()],
                            QuitType.values()[loadedJsonObject.get("quitType").getAsInt()]
                    );
                } catch (Exception e) {
                    _outputLog("解析 Config 失败:" + e);
                    return null;
                }
            }

            /**
             * 解析主配置文件
             */
            private boolean decodeConfigFile() {

                //判断配置文件是否可以读取并有效
                if (!CONFIG_FILE_OBJECT.exists() || !CONFIG_FILE_OBJECT.canRead() || CONFIG_FILE_OBJECT.isDirectory() || CONFIG_FILE_OBJECT.length() <= 0) {
                    _outputLog("配置文件不存在或不可读");
                    return false;
                }

                //开始读取配置文件内容
                final String loadedText = file2Text(CONFIG_FILE_OBJECT, BUFFER_SIZE);
                final JsonObject loadedJsonObject;
                if (TextUtils.isEmpty(loadedText)) {
                    _outputLog("读取得到文本为空");
                    return false;
                } else {
                    try {
                        if (BuildConfig.DEBUG) {
                            _outputLog("读取到文本：" + loadedText);
                        }
                        loadedJsonObject = new JsonParser().parse(loadedText).getAsJsonObject();
                    } catch (Exception e) {
                        _outputLog("Json转换出现异常 " + e);
                        return false;
                    }
                }

                //判断是否需要进行解析
                final int hashCode = loadedText.hashCode();
                if (hashCode == LastConfig.get().getLastConfigHashCode()) {
                    _outputLog("本次哈希值与已保存的一致，不作解析与更新操作");
                    return true;
                }

                //这里解析三组数据
                final Config config = decodeConfigObject(loadedJsonObject);
                final LinkedHashMap<String, App> appArrayList = decodeAppListConfig(loadedJsonObject);
                final LinkedHashMap<Integer, Group> groupsArrayList = decodeGroupListConfig(loadedJsonObject);
                if (config == null || appArrayList == null || groupsArrayList == null) {
                    _outputLog("数据转换出现异常");
                    return false;
                }

                //更新所有配置信息
                LastConfig.get().setConfig(config);
                LastConfig.get().setAppList(appArrayList);
                LastConfig.get().setGroupList(groupsArrayList);
                LastConfig.get().setLastConfigHashCode(hashCode);
                return true;
            }

            /**
             * 解析忽略配置文件
             */
            private boolean decodeIgnoreFile() {
                if (!this.IGNORE_FILE_OBJECT.exists()) {
                    //文件不存在，则释放内置文件
                    if (!outputFile("output_files/ignore.json", this.IGNORE_FILE_OBJECT, BUFFER_SIZE)) {
                        return false;
                    }
                }

                final String loadedText = file2Text(this.IGNORE_FILE_OBJECT, BUFFER_SIZE);
                final JsonObject loadedJsonObject;
                if (TextUtils.isEmpty(loadedText)) {
                    _outputLog("读取得到文本为空");
                    return false;
                } else {
                    try {
                        if (BuildConfig.DEBUG) {
                            _outputLog("读取到文本：" + loadedText);
                        }
                        loadedJsonObject = new JsonParser().parse(loadedText).getAsJsonObject();
                    } catch (Exception e) {
                        _outputLog("Json转换出现异常 " + e);
                        return false;
                    }
                }

                //判断是否需要进行解析
                final int hashCode = loadedText.hashCode();
                if (hashCode == LastConfig.get().getLastIgnoreHashCode()) {
                    _outputLog("本次哈希值与已保存的一致，不作解析与更新操作");
                    return true;
                }

                final IgnoreApp[] ignoreAppsList = decodeIgnoreAppsList(loadedJsonObject);
                if (ignoreAppsList == null) {
                    _outputLog("数据转换出现异常");
                    return false;
                }

                LastConfig.get().setIgnoreList(ignoreAppsList);
                LastConfig.get().setLastIgnoreHashCode(hashCode);
                return true;
            }

            @Override
            public void run() {
                while (true) {
                    //任务被终止
                    if (updateThread == null || updateThread.isInterrupted()) {
                        _outputLog("读取任务被取消");
                        break;
                    }

                    //解析主配置文件
                    final boolean isConfigFileSuccess = decodeConfigFile();

                    //解析忽略配置文件
                    final boolean isIgnoreFileSuccess = decodeIgnoreFile();

                    if (isConfigFileSuccess && isIgnoreFileSuccess) {
                        _outputLog("新配置已生效");
                        try {
                            Thread.sleep(LastConfig.get().getConfig().getUpdateInterval());
                        } catch (InterruptedException ignore) {
                        }
                    } else {
                        break;
                    }
                }
            }
        });
    }

    /**
     * 输出日志
     */
    private void _outputLog(String msg) {
        Log.d("###ConfigUpdate###", msg);
    }
}
