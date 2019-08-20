package com.ocwvar.xlocker.data;

import android.os.Environment;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ocwvar.xlocker.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
     * 日志文件路径
     */
    public static final String PATH_LOG_FILE = PATH_CONFIG_FOLDER + "log.txt";


    //更新线程
    private Thread updateThread;

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
             * 取消状态码
             *
             * 0 - 正常取消流程
             * 1 - 文件无效
             * 2 - 文件读取无效或无内容
             * 3 - 文件解析失败
             */
            private int cancelCode;

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
             * 解析配置应用列表
             *
             * @param loadedJsonObject  读取到的Json数据
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
                                        jsonObject.get("groupId").getAsInt()
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
             * @return 列表数据，解析失败返回 NULL
             */
            private @Nullable
            LinkedHashMap<Integer, Group> decodeGroupListConfig(JsonObject loadedJsonObject) {
                try {
                    JsonObject jsonObject;
                    final LinkedHashMap<Integer, Group> result = new LinkedHashMap<>();
                    for (final JsonElement jsonElement : loadedJsonObject.get("group").getAsJsonArray()) {
                        jsonObject = jsonElement.getAsJsonObject();
                        result.put(
                                jsonObject.get("id").getAsInt(),
                                new Group(
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("start").getAsString(),
                                        jsonObject.get("end").getAsString()
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

            @Override
            public void run() {

                //变量声明
                int hashCode;
                String loadedText;
                JsonObject loadedJsonObject;

                while (true) {
                    //任务被终止
                    if (updateThread == null || updateThread.isInterrupted()) {
                        _outputLog("读取任务被取消");
                        cancelCode = 0;
                        break;
                    }

                    //判断配置文件是否可以读取并有效
                    if (!CONFIG_FILE_OBJECT.exists() || !CONFIG_FILE_OBJECT.canRead() || CONFIG_FILE_OBJECT.isDirectory() || CONFIG_FILE_OBJECT.length() <= 0) {
                        _outputLog("配置文件不存在或不可读");
                        cancelCode = 1;
                        break;
                    }

                    //开始读取配置文件内容
                    loadedText = file2Text(CONFIG_FILE_OBJECT, BUFFER_SIZE);
                    if (TextUtils.isEmpty(loadedText)) {
                        _outputLog("读取得到文本为空");
                        cancelCode = 2;
                        break;
                    } else {
                        try {
                            loadedJsonObject = new JsonParser().parse(loadedText).getAsJsonObject();
                        } catch (Exception ignore) {
                            _outputLog("Json转换出现异常");
                            cancelCode = 3;
                            break;
                        }
                    }

                    //判断是否需要进行解析
                    hashCode = loadedText.hashCode();
                    if (hashCode == LastConfig.get().getLastConfigHashCode()) {
                        _outputLog("本次哈希值与已保存的一致，不作解析与更新操作");
                        continue;
                    }

                    //这里解析三组数据
                    final Config config = decodeConfigObject(loadedJsonObject);
                    final LinkedHashMap<String, App> appArrayList = decodeAppListConfig(loadedJsonObject);
                    final LinkedHashMap<Integer, Group> groupsArrayList = decodeGroupListConfig(loadedJsonObject);
                    if (config == null || appArrayList == null || groupsArrayList == null) {
                        cancelCode = 3;
                        break;
                    }

                    //更新所有配置信息
                    LastConfig.get().setConfig(config);
                    LastConfig.get().setAppList(appArrayList);
                    LastConfig.get().setGroupList(groupsArrayList);
                    LastConfig.get().setLastConfigHashCode(hashCode);

                    try {
                        Thread.sleep(LastConfig.get().getConfig().getUpdateInterval());
                    } catch (InterruptedException ignore) {
                    }
                }

                switch (cancelCode) {
                    case 0:
                        break;

                    //出错的情况下，则停用锁定服务
                    case 1:
                    case 2:
                    case 3:
                        LastConfig.get().setConfig(
                                new Config(
                                        false,
                                        BuildConfig.DEBUG ? (5L * 1000L) : (60L * 1000L),
                                        LockType.fingerprint,
                                        QuitType.Launcher
                                )
                        );
                        break;
                }
            }
        });
    }

    /**
     * 输出日志
     */
    private void _outputLog(String msg) {
        System.out.println("#ConfigUpdate# " + msg);
    }
}
