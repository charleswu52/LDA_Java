package test;

import la.LevenshteinAutomaton;
import la.MDAG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WuChao
 * @since 2021/5/15 10:51
 */
public class Test {

    public static void main(String[] args)throws Exception {
        // 根据字典构建
        ArrayList<String> myArrayList = new ArrayList<>(
                Files.readAllLines(Paths.get("resources/query2.txt")));

        long startTime = System.currentTimeMillis();    //获取开始时间
        MDAG myMDAG = new MDAG(myArrayList);
        long endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("Build Success! Cost:" + (endTime - startTime) + " ms");

        // 读取测试文件搜索测试
        File file = new File("resources/fuzzy.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String testString;
        int maxEdit = 2;

        List<Integer> method_3_time = new ArrayList<>();

        while ((testString = reader.readLine()) != null) {
            System.out.println("======================================================");
            System.out.println("search token: " + testString + ";maxEdit= " + maxEdit);
            startTime = System.currentTimeMillis();
            LinkedList<String> resultList3 = LevenshteinAutomaton.iterativeFuzzySearch(maxEdit, testString, myMDAG);
            endTime = System.currentTimeMillis();
            System.out.println("Method3 iterativeFuzzySearch：");
            System.out.println("matched token size: " + resultList3.size());
            System.out.println("cost time: " + (endTime-startTime)+" ms");
            method_3_time.add((int) (endTime - startTime));

        }
        System.out.println("===================================");
        System.out.println("时间对比：");
        System.out.println("Method3 Average Time: " + ((double) method_3_time.stream().reduce(Integer::sum).get() / method_3_time.size()) + " ms");
        reader.close();
    }
}
