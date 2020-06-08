package bean;


import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;

public class Msg {
    public static final int TYPE_RECEIVED = 0;//表示这是一条收到的消息
    public static final int TYPE_SENT = 1;//表示这是一条发出的消息
    private String content;
    private int type;
    private Calendar mCal;
    private String msg_time;
    private String name;

    public Msg(String content, int type, String name) {
        this.content = content;
        this.type = type;
        this.msg_time = currentTime().toString();
        this.name = name;
    }

    public Msg(String content) {
        this.content = content;
    }


    public void setType(int type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }
    public String getContent() {
        return content;
    }

    public String getMsgTime(){
        return msg_time;
    }

    private CharSequence currentTime(){
        mCal = Calendar.getInstance();
        CharSequence c_time = DateFormat.format("kk:hh:mm", mCal.getTime());
        if(Integer.valueOf(c_time.toString().split(":")[0]).intValue()<12){
            c_time = c_time.toString().replaceFirst(c_time.toString().split(":")[0]+":","");
            return "上午 " + c_time;
        }else{
            c_time = c_time.toString().replaceFirst(c_time.toString().split(":")[0]+":","");
            return "下午 " + c_time;
        }
    }
    public String getName(){
        return name;
    }
}
