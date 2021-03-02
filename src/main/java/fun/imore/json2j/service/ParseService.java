package fun.imore.json2j.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Data
public class ParseService {
    private String packName = "fun.imore.json2j.service";
    private String filePath = "F:\\workspace\\github\\json2j\\src\\main\\java\\fun\\imore\\json2j\\service\\";

    public String parse(String json) {

        Object parse = JSON.parse(json);
        return null;
    }

    public static void response2j(String url) {

    }

    /**
     * 解析json字符串 生成实体
     *
     * @param json
     */
    public void jsonText2j(String json) throws IOException {
        Object parse = JSON.parse(json);
        json2j(parse, packName, "Entity");
    }

    public String json2j(Object json, String packName, String className) throws IOException {
        /**
         * Java文件的准备
         */
        StringBuilder stringBuilder = new StringBuilder();
        /**
         * 初始化包信息
         */
        stringBuilder.append("package ").append(packName).append(";\n");
        stringBuilder.append("\n");
        /**
         * 初始化导入信息
         */
        stringBuilder.append("import com.fasterxml.jackson.annotation.JsonSetter;\n");
        stringBuilder.append("import lombok.Data;\n");
        stringBuilder.append("import java.util.List;\n");
        stringBuilder.append("\n");
        /**
         * 初始化注解信息
         */
        stringBuilder.append("@Data\n");
        /**
         * 初始化类信息
         */
        stringBuilder.append("public class ").append(className).append(" {\n");


        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            if (jsonObject.keySet().size() == 0) {
                return "Object";
            }
            for (String key : jsonObject.keySet()) {
                /**
                 * 设置字段注解
                 */
                stringBuilder.append("    @JsonSetter(\"").append(key).append("\")\n");
                /**
                 * 设置字段作用域
                 */
                stringBuilder.append("    private ");
                Object value = jsonObject.get(key);
                String type = this.getType(value, key);

                stringBuilder.append(type).append(" ");

                //添加数据名称
                stringBuilder.append(key).append(";\n");
            }
        } else if (json instanceof JSONArray) {

        }

        stringBuilder.append("}\n");
        //写到文件
        InputStream in_withcode = new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        OutputStream out = new FileOutputStream(filePath + className + ".java");
        to(in_withcode, out, null);
        return className;
    }

    public String getType(Object value, String key) throws IOException {

        String type = "Object";
        if (value instanceof String) {
            /**
             * 设置字段类型
             */
            type = "String";
        } else if (value instanceof Integer) {
            type = "Integer";
        } else if (value instanceof Short) {
            type = "Short";
        } else if (value instanceof Byte) {
            type = "Byte";
        } else if (value instanceof Long) {
            type = "Long";
        } else if (value instanceof Boolean) {
            type = "Boolean";
        } else if (value instanceof JSONObject) {
            //需要嵌套
            /**
             * 1、生成Object对应的Java文件
             * 2、将生成的对象放入根Java文件
             */
            type = captureName(StrUtil.toCamelCase(key));
            type = json2j(value, packName, type);
        } else if (value instanceof JSONArray) {
            /**
             * 当对象是数组的时候
             * 1、空数组 []
             *
             */
            JSONArray jsonArray = (JSONArray) value;
            if (CollectionUtils.isEmpty(jsonArray)) {
                return "List<Object>";
            } else {
                String item = getType(jsonArray.get(0), key);
                type = "List<" + item + ">";
            }
        }
        return type;
    }

    public static void jsonObject2j(JSONObject json, StringBuilder stringBuilder) {

    }

    public static void jsonArray2j(JSONArray json) {

    }

    /**
     * 判断数据类型返回是否需要继续嵌套解析，就是是否为Java基础类型
     *
     * @param mateData
     * @return
     */
    private boolean isMateDate(Object mateData) {
//        return mateData.getClass().isPrimitive();
        return mateData instanceof JSONArray || mateData instanceof JSONObject;
    }

    private static void to(InputStream it, OutputStream os, Object size) {
        try {
            /**
             * 加个限速，在下载原图的时候加上进入条，增加一个下载等待时间；
             */
            //文件拷贝
            byte[] flush = new byte[1024];
            int len = 0;

            while (0 <= (len = it.read(flush))) {
                os.write(flush, 0, len);
//                Thread.sleep(500);
            }

            os.flush();
            //关闭流的注意 先打开的后关
            os.close();
            it.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //首字母大写
    public static String captureName(String name) {
        //     name = name.substring(0, 1).toUpperCase() + name.substring(1);
//        return  name;
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }
}
