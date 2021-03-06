package com.wdg.pattern.structure.decorator;

/**
 *  @Description 滚动条装饰类：具体装饰类
 *  
 *  @author wudiguang
 *  @Date 2021/12/2
 */ 
public class ScrollBarDecorator extends ComponentDecorator{
    public ScrollBarDecorator(Component component) {
        super(component);
    }

    public void display(){
        this.setScrollBar();
        super.display();
    }

    public void setScrollBar(){
        System.out.println("为构件增加滚动条！");
    }
}
