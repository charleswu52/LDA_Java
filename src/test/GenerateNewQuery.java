package test;

import java.io.*;
import java.util.*;

/**
 * @author WuChao
 * @create 2021/7/9 上午9:29
 */
public class GenerateNewQuery {
    public static void main(String[] args)throws Exception {
        File file = new File("resources/query.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        List<String> stringSet = new ArrayList<>();
        String temp = null;
        while ((temp = reader.readLine()) != null) {
            String[] s = temp.split(" ");
            for (int i = 1; i < s.length; i++) {
                stringSet.add(s[i]);
            }
        }
        reader.close();
        System.out.println(stringSet);


        File newFile = new File( "resources/query2.txt");
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        // 获取该文件的缓冲输出流
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));
        // 写入信息
        for (String word : stringSet) {
            bufferedWriter.write(word+"\n");
        }
        bufferedWriter.flush();// 清空缓冲区
        bufferedWriter.close();// 关闭输出流


        System.out.println("finish!");
    }
}
