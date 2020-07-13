package org.techtown.mnist_sample;

import java.util.ArrayList;
import java.util.Collections;

public class EfficientOuput implements Comparable<EfficientOuput>{
    private String name;
    private float data;

    String[] outputNames = {"Acne", "Actinic", "Atopic", "Bullous", "Cellulitis", "Eczema", "Exanthems", "Herpes",
            "Hives", "Light Disease", "Lupus", "Contact Dermititis", "Psoriasis", "Scabies",
            "Systemic", "Tinea", "Vasculitis", "Warts"};

    public EfficientOuput(){
        this.name="";
        this.data=0;
    }

    public EfficientOuput(String num, float data){
        this.name = num;
        this.data = data;
    }

    public String getName(){return name;}
    public float getData(){return data;}


    @Override
    public int compareTo(EfficientOuput o) {
        if(this.data>o.data) return -1;
        else if(this.data == o.data) return 0;
        else return 1;
    }


    public String outputToString(float[][] op){
        ArrayList<EfficientOuput> eo = new ArrayList<>();

        for(int i=0;i<18;i++){
            eo.add(new EfficientOuput(outputNames[i], op[0][i]));
        }

        Collections.sort(eo);

        String result = "";
        for(int i=0;i<4;i++){
            float data = eo.get(i).getData();
            data = data*100;
            String strData = new String();
            if(data>=1) strData = String.format("%.0f", data);
            else if(data>=0.1) strData = String.format("%.1f", data);
            else if(data>=0.01) strData = String.format("%.2f", data);
            else strData = String.format("%.3f", data);
            result = result + eo.get(i).getName() + ": " +strData+"%, ";
        }
        float data = eo.get(4).getData();
        data = data*100;
        String strData;
        if(data>=1) strData = String.format("%.0f", data);
        else if(data>=0.1) strData = String.format("%.1f", data);
        else if(data>=0.01) strData = String.format("%.2f", data);
        else strData = String.format("%.3f", data);
        result = result + eo.get(4).getName() + ": " +strData+"%";

        return result;
    }

    public String outputToData(float[][] op){
        ArrayList<EfficientOuput> eo = new ArrayList<>();

        String[] outputNames = {"Acne", "Actinic", "Atopic", "Bullous", "Cellulitis", "Eczema", "Exanthems", "Herpes",
                "Hives", "Light Disease", "Lupus", "Contact Dermititis", "Psoriasis", "Scabies",
                "Systemic", "Tinea", "Vasculitis", "Warts"};


        for(int i=0;i<18;i++){
            eo.add(new EfficientOuput(outputNames[i], op[0][i]));
        }

        String name= outputNames[0];
        float confidence = op[0][0];

        for(int i=1;i<18;i++){
            if(confidence < op[0][i]){
                confidence = op[0][i];
                name = outputNames[i];
            }
        }

        confidence *= 100;

        String result = "Name: "+name + "\nConfidence: ";
        if(confidence>=1) result = result + String.format("%.0f", confidence);
        else if(confidence>=0.1) result = result + String.format("%.1f", confidence);
        else result = result + String.format("%.2f", confidence);

        return result;
    }
}
