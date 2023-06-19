package net.hearnsoft.thwikicdsearchapi.bean;

import java.util.ArrayList;
import java.util.List;

public class DataBean {
    private int from;
    private int till;
    private int tota;
    private String symb;
    private ResuBean resu;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTill() {
        return till;
    }

    public void setTill(int till) {
        this.till = till;
    }

    public int getTota() {
        return tota;
    }

    public void setTota(int tota) {
        this.tota = tota;
    }

    public String getSymb() {
        return symb;
    }

    public void setSymb(String symb) {
        this.symb = symb;
    }

    public ResuBean getResu() {
        return resu;
    }

    public void setResu(ResuBean resu) {
        this.resu = resu;
    }

    public static class ResuBean {
        private List<String> page;
        private List<String> link;
        private List<String> text;
        private ArrayList<ArrayList<Object>> data;

        public List<String> getPage() {
            return page;
        }

        public void setPage(List<String> page) {
            this.page = page;
        }

        public List<String> getLink() {
            return link;
        }

        public void setLink(List<String> link) {
            this.link = link;
        }

        public List<String> getText() {
            return text;
        }

        public void setText(List<String> text) {
            this.text = text;
        }

        public ArrayList<ArrayList<Object>> getData() {
            return data;
        }

        public void setData(ArrayList<ArrayList<Object>> data) {
            this.data = data;
        }
    }
}
