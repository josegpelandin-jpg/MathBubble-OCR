package com.mahetre.mathbubble;

public class MathParser {
    private final String s; private int p=0;
    public MathParser(String raw){
        s=raw.replace("×","*").replace("÷","/").replace("−","-")
             .replace("–","-").replace("X","*").replace("x","*")
             .replaceAll("\\s+","");
    }
    public double parse(){
        double v=expr();
        if(p!=s.length()) throw new RuntimeException("Unexpected input");
        return v;
    }
    private double expr(){
        double v=term();
        while(p<s.length()){
            char c=s.charAt(p);
            if(c=='+'){p++;v+=term();}
            else if(c=='-'){p++;v-=term();}
            else break;
        }
        return v;
    }
    private double term(){
        double v=power();
        while(p<s.length()){
            char c=s.charAt(p);
            if(c=='*'){p++;v*=power();}
            else if(c=='/'){p++;v/=power();}
            else break;
        }
        return v;
    }
    private double power(){
        double v=factor();
        if(p<s.length() && s.charAt(p)=='^'){p++;v=Math.pow(v,power());}
        return v;
    }
    private double factor(){
        if(p<s.length() && s.charAt(p)=='+'){p++;return factor();}
        if(p<s.length() && s.charAt(p)=='-'){p++;return -factor();}
        if(p<s.length() && s.charAt(p)=='('){
            p++; double v=expr();
            if(p>=s.length() || s.charAt(p)!=')') throw new RuntimeException();
            p++; return v;
        }
        int st=p;
        while(p<s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p)=='.')) p++;
        if(st==p) throw new RuntimeException();
        return Double.parseDouble(s.substring(st,p));
    }
}
