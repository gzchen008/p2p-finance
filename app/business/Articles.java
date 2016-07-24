package business;

/**
 * Created by libaozhong on 2015/6/17.
 */
public class Articles {
    private String content;

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    private int articleCount;
    private String title;
    private String description;
    private String picUrl;
    private String url;
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }




}
