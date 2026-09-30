package com.mahetre.mathbubble;

public class MathParser {
    private final String s;
    private int p=0;

    public MathParser(String raw){
        s=normalize(raw);
    }

    private static String normalize(String raw){
        String x=raw
            .replace("×","*").replace("✕","*").replace("·","*").replace("∙","*")
            .replace("÷","/").replace("−","-").replace("–","-").replace("—","-")
            .replace("X","*").replace("x","*").replace("√","sqrt")
            .replace("²","^2").replace("³","^3")
            .replace("½","(1/2)").replace("¼","(1/4)").replace("¾","(3/4)")
            .replace(",",".")
            .replaceAll("(?i)(=|\u2248).*?$", "")
            .replaceAll("[?¿]","")
            .replaceAll("\\s+","");
        return x;
    }

    public double parse(){
        double v=expr();
        if(p!=s.length()) throw new RuntimeException("Unexpected input at "+p);
        if(Double.isNaN(v)||Double.isInfinite(v)) throw new RuntimeException("Invalid result");
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
            else if(c=='/'){p++; double d=power(); if(Math.abs(d)<1e-15) throw new RuntimeException("Division by zero"); v/=d;}
            else if(startsFactor()) { v*=power(); } // multiplicación implícita: 2(3+4), 2sqrt(9)
            else break;
        }
        return v;
    }

    private boolean startsFactor(){
        if(p>=s.length()) return false;
        char c=s.charAt(p);
        return c=='(' || Character.isDigit(c) || c=='.' || s.startsWith("sqrt",p);
    }

    private double power(){
        double v=factor();
        if(p<s.length() && s.charAt(p)=='^'){p++;v=Math.pow(v,power());}
        return v;
    }

    private double factor(){
        if(p<s.length() && s.charAt(p)=='+'){p++;return factor();}
        if(p<s.length() && s.charAt(p)=='-'){p++;return -factor();}
        if(s.startsWith("sqrt",p)){
            p+=4; double v=factor();
            if(v<0) throw new RuntimeException("Negative sqrt");
            return applyPercent(Math.sqrt(v));
        }
        if(p<s.length() && s.charAt(p)=='('){
            p++; double v=expr();
            if(p>=s.length() || s.charAt(p)!=')') throw new RuntimeException("Missing )");
            p++; return applyPercent(v);
        }
        int st=p;
        while(p<s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p)=='.')) p++;
        if(st==p) throw new RuntimeException("Number expected");
        double v=Double.parseDouble(s.substring(st,p));
        return applyPercent(v);
    }

    private double applyPercent(double v){
        while(p<s.length() && s.charAt(p)=='%'){ p++; v/=100.0; }
        return v;
    }
}
