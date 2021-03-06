package com.namtran.entity;

/**
 * Created by namtr on 23/08/2016.
 * Lưu thông tin của tài khoản ngân hàng
 * Gồm tên ngân hàng, số tiền trong tài khoản, tỉ giá, ngày, id
 * Dùng cho chức năng tài khoản ngân hàng
 */
public class BankItem {
    private String name;
    private String money;
    private String rate;
    private String date;
    private String id;

    public BankItem() {
        // Cần cho firebase đồng bộ dữ liệu
    }

    public BankItem(String name, String money, String rate, String date, String id) {
        this.name = name;
        this.money = money;
        this.rate = rate;
        this.date = date;
        this.id = id;
    }

    public BankItem(String name, String money, String rate, String date) {
        this.name = name;
        this.money = money;
        this.rate = rate;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getMoney() {
        return money;
    }

    public String getRate() {
        return rate;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
