package com.wdg.pattern.structure.chain;

/**
 *  @Description 董事会类：具体处理者
 *  
 *  @author wudiguang
 *  @Date 2021/12/2
 */ 
public class Congress extends Approver {
    public Congress(String name) {
        super(name);
    }
    //具体请求处理方法
    public void processRequest(PurchaseRequest request) {
        System.out.println("召开董事会审批采购单：" + request.getNumber() + "，金额：" + request.getAmount() + "元，采购目的：" + request.getPurpose() + "。");        //处理请求
    }
}