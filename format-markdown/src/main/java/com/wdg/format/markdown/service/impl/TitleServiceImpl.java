package com.wdg.format.markdown.service.impl;

import com.alibaba.fastjson.JSON;
import com.wdg.format.markdown.service.CoreService;
import com.wdg.format.markdown.service.TitleService;
import com.wdg.format.markdown.util.FileReadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  @Description 标题 Impl
 *  
 *  @author wudiguang
 *  @Date 2021/12/22
 */
@Service
@Slf4j
public class TitleServiceImpl implements TitleService {

    private static final Pattern pattern = Pattern.compile("#*");

    @Override
    public void formatTitle(String path) {
        // 将标题单独提取出来，判断是否服务规则，不符合则给出提示
        log.info(path);
        BufferedReader reader = FileReadUtil.readFileFromPath("kubernetes_network.md");
        formatTitleHierarchy(reader);
    }

    @Override
    public void formatTitleHierarchy(BufferedReader reader) {
        Stack<String> titleStack = new Stack<>();
        try {
            String lineText;
            String lastTitleStartFixed = null;
            while((lineText = reader.readLine()) != null){
                if(lineText.startsWith("#")){
                    lastTitleStartFixed = circuitProcessStack(titleStack, lineText, lastTitleStartFixed);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        log.info("[标题] 数据:{}", JSON.toJSONString(titleStack));
        log.info("---------------------------------------");
        clearOrderTitle(titleStack, null);
        log.info("[标题] 数据:{}", JSON.toJSONString(titleStack));
    }

    @Override
    public void clearOrderTitle(Stack<String> titleStack, String lastTitleStart) {
        if(titleStack.isEmpty()){
            return;
        }
        String title = titleStack.peek();
        String nowTitleStart = getTitleStart(title);
        if(null == lastTitleStart){
            log.info("[出栈] 第一个必须出栈, 当前标题级别:{}, 出栈标题:{}", nowTitleStart, title);
            lastTitleStart = getTitleStart(title);
        }else if(Objects.equals(nowTitleStart, lastTitleStart)){
            log.info("[出栈] 比上一个出栈的标题低一级, 上一个级别:{}, 当前标题级别:{}, 出栈标题:{}", String.format("%s%s", lastTitleStart, "#"), nowTitleStart, title);
        }else {
            log.error("[不符合规范] 上一个级别:{}, 当前标题级别:{}, 出栈标题:{}", String.format("%s%s", lastTitleStart, "#"), nowTitleStart, title);
        }
        assert lastTitleStart != null;
        titleStack.pop();
        clearOrderTitle(titleStack, lastTitleStart.substring(0, lastTitleStart.length() - 1));
    }

    @Override
    public String circuitProcessStack(Stack<String> titleStack, String lineText, String lastTitleStartFixed) {
        String nowTitleStart = getTitleStart(lineText);
        String nowTitleTure = lineText.substring(nowTitleStart.length());

        // 谨慎使用四级标题，尽量避免出现，保持层级的简单，防止出现过于复杂的章节
        if(nowTitleStart.length() > 3){
            log.error("[出现四级标题] 标题::{}, 级别:{}", lineText, nowTitleStart);
        }

        if(titleStack.isEmpty()){
            log.info("[入栈] 标题::{}, 级别:{}", lineText, nowTitleStart);
            titleStack.push(lineText);
        }else {
            // 获取栈顶title开头
            String lastTitle = titleStack.peek();
            String lastTitleStart = getTitleStart(lastTitle);
            assert lastTitleStart != null;
            String lastTitleTure = lastTitle.substring(lastTitleStart.length());
            // 如果当前标题级别比栈顶标题级别小，则栈顶标题出栈，当前标题入栈
            if(Objects.equals(String.format("%s%s", nowTitleStart, "#"), lastTitleStart)){
                log.info("[出栈] 当前标题级别比栈顶标题级别小, 当前标题:{}, 出栈标题:{}", lineText, lastTitle);
                // 标题要避免孤立编号（即同级标题只有一个）
                if(!Objects.equals(lastTitleStart, lastTitleStartFixed)){
                    log.error("[异常] 出现孤立编号, 孤立编号标题:{}", lastTitle);
                }
                titleStack.pop();
//                circuitProcessStack(titleStack, lineText);
            }else if(Objects.equals(nowTitleStart, lastTitleStart)){
                // 栈顶和当前标题同级则跳过
//                titleStack.pop();
                log.info("[跳过入栈和出栈] 栈顶和当前标题同级则出栈, 当前标题:{}, 栈顶标题:{}", lineText, lastTitle);
            }else {
                // 判断标题是否出现跳级
                if(!Objects.equals(String.format("%s%s", lastTitleStart, "#"), nowTitleStart)){
                    log.error("[入栈] 标题::{}, 当前级别:{}, 上一个标题级别:{}, 栈顶标题为:{}", lineText, nowTitleStart, lastTitleStart, lastTitle);
                }else {
                    log.info("[入栈] 标题::{}, 当前级别:{}, 上一个标题级别:{}, 栈顶标题为:{}", lineText, nowTitleStart, lastTitleStart, lastTitle);
                }
                // 下级标题不重复上一级标题的名字
                if(Objects.equals(nowTitleTure, lastTitleTure)){
                    log.error("[入栈] 出现标题名字同名 标题::{}, 当前级别:{}, 上一个标题级别:{}, 栈顶标题为:{}", lineText, nowTitleStart, lastTitleStart, lastTitle);
                }
                titleStack.push(lineText);
            }
            // 记录上一个标题级别
            lastTitleStartFixed = lastTitleStart;
        }
        log.info("[当前标题] 数据:{}", JSON.toJSONString(titleStack));
        return lastTitleStartFixed;
    }

    @Override
    public String getTitleStart(String title){
        Matcher lastMatcher = pattern.matcher(title);
        if(lastMatcher.find()){
            return lastMatcher.group();
        }
        return null;
    }
}
