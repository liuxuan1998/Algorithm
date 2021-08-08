package com.liuxuan.algorithm.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @ClassName com.liuxuan.algorithm.sort.QuickSort
 * @Author liuxuan
 * @Date 2021/8/1 18:40
 * @Descitption
 * @Version 1.0
 */
public class Sort {


    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(4);
        list.add(6);
        list.add(5);
        list.add(2);
        list.add(2);
        System.out.println(quickSort(list));

    }


    /**
     * 快排
     * @param data
     * @return
     */
    public static List<Integer> quickSort(List<Integer> data) {
        if (data == null || data.isEmpty() || data.size() == 1) {
            return data;
        }
        quickSort(data, 0, data.size() - 1);
        return data;
    }

    private static void quickSort(List<Integer> data, int right, int left) {

        if (right == left) {
            return;
        }

        //1、定义一个基准数
        int baseNum = left;
        int right1 = right;
        int left1 = left;
        //如果两个哨兵开始相遇 结束循环 说明找到了基准数应该到达得的地方
        while (right != left) {
            //2、从左边开始找  找到一个基准数小或者等于的数据
            while (left > right && data.get(left) >= data.get(baseNum)) {
                left--;
            }
            //3、从右边开始找 找到一个比基准数大的数据
            while (right < left && data.get(right) < data.get(baseNum)) {
                right++;
            }
            //4、将找到的两个数据交换位置
            replace(data, left, right);
        }
        // 将基准数交换到应该在的位置 ？？？
        //为什么是left+1 这和从那边开始找数据的方向有关
        replace(data, left + 1, baseNum);
        //将基准数处理完之后的部分两个和三个的情况是已经排序完成的状态
        if (left1 - right1 <= 2) {
            return;
        }
        quickSort(data, right1, left);
        quickSort(data, left + 2, left1);


    }


    private static void replace(List<Integer> data, int left, int right) {
        int temp = data.get(left);
        data.set(left, data.get(right));
        data.set(right, temp);
    }

}
