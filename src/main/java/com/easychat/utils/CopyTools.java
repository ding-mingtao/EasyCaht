package com.easychat.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CopyTools
 * @Description: 复制工具类
 * @Author: 丁铭涛
 * @DateTime: 2025/4/4 13:19
 **/
public class CopyTools {
    public static <T,S> List<T> copyList(List<S> sList, Class<T> classz) {
        List<T> list = new ArrayList<T>();
        for (S s : sList) {
            T t = null;
            try{
                t = classz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }
    public static <T,S> T copy(S s, Class<T> classz) {
        T t = null;
        try{
            t = classz.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}
