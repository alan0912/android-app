package bean;


public class Msg {
    public static final int TYPE_RECEIVED = 0;//表示这是一条收到的消息
    public static final int TYPE_SENT = 1;//表示这是一条发出的消息
    private String content;
    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
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
}
