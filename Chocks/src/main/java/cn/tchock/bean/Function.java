package cn.tchock.bean;

import cn.chock.bean.BaseBean;

/**
 * 功能实体类
 * @author timpkins
 */
public class Function extends BaseBean {
    private String head;
    private String name;
    private String num;

    public Function(String head, String name, String num) {
        this.head = head;
        this.name = name;
        this.num = num;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
